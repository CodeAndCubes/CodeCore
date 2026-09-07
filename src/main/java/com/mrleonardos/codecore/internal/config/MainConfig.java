package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mrleonardos.codecore.api.config.AuditSettings;
import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigFormat;
import com.mrleonardos.codecore.api.config.SectionSpec;
import com.mrleonardos.codecore.api.config.StorageSettings;

/**
 * Главный файл линейки {@code config/code/config.toml}.
 *
 * <p>
 * Собирается из секций, у каждой из которых один объявитель: мод приносит описание в фазе init, а ядро
 * в конце постинициализации пишет файл целиком. Секция мода, которого сейчас нет на сервере, остаётся в
 * файле нетронутой вместе со своими комментариями: снять мод на неделю и не потерять настройки важнее,
 * чем держать файл в чистоте.
 */
public final class MainConfig {

    /** Значение ключа роли, при котором роль занимает наш мод, если он стоит. */
    public static final String OWNER_AUTO = "auto";

    /** Значение ключа роли, при котором роль не занята никем. */
    public static final String OWNER_OFF = "off";

    /** Секция, в которой лежат владельцы ролей. */
    public static final String OWNERS_SECTION = "owners";

    private static final int SCHEMA_VERSION = 1;
    private static final String STORAGE_SECTION = "storage";
    private static final String AUDIT_SECTION = "audit";
    private static final String PROVIDER_KEY = "provider";
    private static final String AUTOSAVE_KEY = "autosaveSeconds";
    private static final String LOG_CHANGES_KEY = "logChanges";
    private static final String LOG_CHECKS_KEY = "logChecks";
    private static final String DESCRIPTION = ConfigKeys.LINEUP_DIRECTORY + "/" + ConfigKeys.MAIN_FILE;

    private static final String[] HEADER = { "Главный файл линейки Code*." };

    private static final String[] OWNERS_COMMENT = { "Кто держит роль целиком.",
        "\"auto\" наш мод, если он стоит; \"off\" роль не занята никем; иначе имя владельца.",
        "Известные имена сервер называет при старте и по /codecore adapters." };

    private static final String DATABASES_EXAMPLE = "\n" + "# Базы данных. Записей сколько нужно, каждая независимая.\n"
        + "# Драйвер админ кладёт в mods/ или libs/ сам, ядро грузит класс по имени.\n"
        + "# [[databases]]\n"
        + "# id = \"global\"\n"
        + "# label = \"global\"\n"
        + "# driverClass = \"org.mariadb.jdbc.Driver\"\n"
        + "# url = \"jdbc:mariadb://10.0.0.5:3306/mymods\"\n"
        + "# user = \"mymods\"\n"
        + "# password = \"secret\"\n"
        + "# poolSize = 8\n"
        + "# connectionTimeoutMs = 5000\n"
        + "# queryTimeoutMs = 10000\n"
        + "# idleTimeoutSeconds = 60\n"
        + "\n"
        + "# Какую запись брать, когда меток с одним именем несколько.\n"
        + "# [labelDefaults]\n"
        + "# server = \"local\"\n";

    private static final String DATABASES_KEY = "databases";
    private static final String LABEL_DEFAULTS_KEY = "labelDefaults";

    private final Map<String, SectionFile<?>> sections = new LinkedHashMap<>();
    private final ConfigPaths paths;
    private final Logger log;

    private TomlDocument document;
    private RootSection root = new RootSection();
    private StorageSection storage = new StorageSection();
    private AuditSection audit = new AuditSection();
    private List<String> roles = Collections.emptyList();
    private boolean sealed;
    private boolean fromFuture;

    public MainConfig(ConfigPaths paths, Logger log) {
        this.paths = paths;
        this.log = log;
    }

    /** Прочитать файл. Отсутствующий файл не создаётся до конца постинициализации. */
    public void load() {
        Path path = paths.mainFile();
        document = Files.isRegularFile(path) ? read(path) : TomlDocument.empty();
        int stored = document.data()
            .integer(ConfigKeys.SCHEMA_VERSION, SCHEMA_VERSION);
        fromFuture = stored > SCHEMA_VERSION;
        if (fromFuture) {
            log.warn(
                "Config {} has schema version {}, which is newer than supported {}; leaving the file untouched",
                DESCRIPTION,
                stored,
                SCHEMA_VERSION);
        }
        bindOwnSections();
        for (SectionFile<?> section : sections.values()) {
            section.bind(document);
        }
    }

    /** Объявить секцию и получить доступ к её значениям. */
    public <T> ConfigFile<T> section(SectionSpec<T> spec) {
        if (sealed) {
            throw new IllegalStateException(
                "Main config is already written, declare section " + spec.name() + " during your mod's init phase");
        }
        if (reserved(spec.name()) || sections.containsKey(spec.name())) {
            throw new IllegalStateException("Section " + spec.name() + " of the main config is already declared");
        }
        SectionFile<T> section = new SectionFile<>(spec, this);
        sections.put(spec.name(), section);
        section.bind(document);
        return section;
    }

    /** Конец постинициализации: набор секций и перечень ролей известны, файл пишется целиком. */
    public void seal(List<String> declaredRoles) {
        roles = new ArrayList<>(declaredRoles);
        sealed = true;
        write();
    }

    public ConfigData data() {
        return document.data();
    }

    public Path path() {
        return paths.mainFile();
    }

    public String serverId() {
        return root.serverId;
    }

    /** Язык, на котором сервер собирает строки для игроков. */
    public String language() {
        return root.language;
    }

    public StorageSettings storage(String role) {
        ConfigData override = override(STORAGE_SECTION, role);
        if (override == null) {
            return new StorageSettings(storage.provider, storage.autosaveSeconds);
        }
        return new StorageSettings(
            override.string(PROVIDER_KEY, storage.provider),
            override.integer(AUTOSAVE_KEY, storage.autosaveSeconds));
    }

    public AuditSettings audit(String role) {
        ConfigData override = override(AUDIT_SECTION, role);
        if (override == null) {
            return new AuditSettings(audit.logChanges, audit.logChecks);
        }
        return new AuditSettings(
            override.flag(LOG_CHANGES_KEY, audit.logChanges),
            override.flag(LOG_CHECKS_KEY, audit.logChecks));
    }

    /** Перечитать файл и заново разобрать все объявленные секции. */
    public void reload() {
        load();
    }

    void write() {
        if (fromFuture) {
            return;
        }
        document.store(root, RootSection.class, SCHEMA_VERSION);
        TomlBinder.comment(document.config(), ConfigKeys.SCHEMA_VERSION, ConfigHeader.with(HEADER));
        if (sealed) {
            writeOwners();
        }
        document
            .storeSection(STORAGE_SECTION, storage, StorageSection.class, TomlBinder.commentOf(StorageSection.class));
        document.storeSection(AUDIT_SECTION, audit, AuditSection.class, TomlBinder.commentOf(AuditSection.class));
        for (SectionFile<?> section : sections.values()) {
            section.store(document);
        }
        ConfigWriting.atomically(paths.mainFile(), this::render, DESCRIPTION, log);
    }

    private void render(Writer writer) throws IOException {
        document.writeTo(writer);
        ConfigData data = document.data();
        if (!data.has(DATABASES_KEY) && !data.has(LABEL_DEFAULTS_KEY)) {
            writer.write(DATABASES_EXAMPLE);
        }
    }

    private void writeOwners() {
        CommentedConfig previous = document.existingTable(OWNERS_SECTION);
        CommentedConfig table = document.newTable();
        for (String role : roles) {
            Object chosen = previous == null ? null : previous.getRaw(Collections.singletonList(role));
            table.set(Collections.singletonList(role), chosen == null ? OWNER_AUTO : chosen);
            TomlBinder.carry(previous, table, role);
        }
        if (previous != null) {
            for (UnmodifiableConfig.Entry entry : previous.entrySet()) {
                if (!roles.contains(entry.getKey())) {
                    Object own = entry.getRawValue();
                    table.set(Collections.singletonList(entry.getKey()), own);
                    TomlBinder.carry(previous, table, entry.getKey());
                }
            }
        }
        document.putTable(OWNERS_SECTION, table);
        TomlBinder.comment(document.config(), OWNERS_SECTION, OWNERS_COMMENT);
    }

    private void bindOwnSections() {
        root = bound(document.bind(RootSection.class), RootSection::new);
        root.normalize();
        storage = bound(document.bindSection(STORAGE_SECTION, StorageSection.class), StorageSection::new);
        storage.normalize();
        audit = bound(document.bindSection(AUDIT_SECTION, AuditSection.class), AuditSection::new);
    }

    private ConfigData override(String section, String role) {
        ConfigData table = document.data()
            .table(section);
        return table == null ? null : table.table(role);
    }

    private TomlDocument read(Path path) {
        try {
            return TomlDocument.read(path);
        } catch (IOException | RuntimeException failure) {
            Path broken = path.resolveSibling(
                path.getFileName()
                    .toString() + ConfigKeys.BROKEN_SUFFIX);
            log.warn("Failed to read config {}: {}", DESCRIPTION, failure.toString());
            try {
                Files.move(path, broken, StandardCopyOption.REPLACE_EXISTING);
                log.warn("Config {} was moved to {} and replaced with defaults", DESCRIPTION, broken);
            } catch (IOException move) {
                log.error("Failed to set aside broken config {}: {}", DESCRIPTION, move.toString());
            }
            ConfigHints.afterBadRead(ConfigFormat.TOML, log);
            return TomlDocument.empty();
        }
    }

    private static boolean reserved(String name) {
        return OWNERS_SECTION.equals(name) || STORAGE_SECTION.equals(name) || AUDIT_SECTION.equals(name);
    }

    private static <T> T bound(T parsed, Supplier<T> fallback) {
        return parsed == null ? fallback.get() : parsed;
    }
}
