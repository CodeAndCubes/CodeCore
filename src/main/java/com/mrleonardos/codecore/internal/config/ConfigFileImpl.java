package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigSpec;

/**
 * Открытый файл настроек.
 *
 * <p>
 * Файл, который не удалось разобрать, откладывается рядом с суффиксом {@code .broken}, а работа
 * продолжается со значениями по умолчанию: сервер не должен падать из-за пропущенной запятой.
 *
 * <p>
 * Если цепочка миграций оборвалась на полпути, файл не переписывается вовсе, а рядом остаётся копия
 * {@code .broken}. Иначе обновление со схемы 1 сразу на 5 при одном объявленном шаге стёрло бы всё, чего
 * текущий класс не знает.
 */
public final class ConfigFileImpl<T> implements ConfigFile<T> {

    private final ConfigSpec<T> spec;
    private final ConfigPaths paths;
    private final Logger log;

    private T value;
    private Path path;
    private ConfigDocument document;

    ConfigFileImpl(ConfigSpec<T> spec, ConfigPaths paths, Logger log) {
        this.spec = spec;
        this.paths = paths;
        this.log = log;
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException(
                "Config " + describe() + " is not loaded yet; world state is only available while a world is loaded");
        }
        return value;
    }

    @Override
    public boolean loaded() {
        return value != null;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public void reload() {
        load();
    }

    @Override
    public void save() {
        if (value == null) {
            return;
        }
        if (document == null) {
            document = ConfigDocuments.empty(spec.format());
        }
        document.store(value, spec.type(), spec.schemaVersion());
        ConfigWriting.atomically(path, document::writeTo, describe(), log);
    }

    /** Прочитать файл или создать его со значениями по умолчанию. */
    void load() {
        path = paths.resolve(spec);
        if (!Files.isRegularFile(path)) {
            createFromDefaults();
            log.info("Created config {} at {}", describe(), path);
            return;
        }

        ConfigDocument read = read();
        if (read == null) {
            quarantine();
            ConfigHints.afterBadRead(spec.format(), log);
            createFromDefaults();
            return;
        }

        MigrationOutcome outcome = MigrationRunner
            .run(read.data(), spec.migrations(), spec.schemaVersion(), describe(), log);
        T parsed = parse(read);
        if (parsed == null) {
            quarantine();
            createFromDefaults();
            return;
        }

        document = read;
        value = parsed;
        reportUnknownKeys();
        if (outcome == MigrationOutcome.INCOMPLETE) {
            keepCopy();
            return;
        }
        if (outcome == MigrationOutcome.MIGRATED) {
            save();
        }
    }

    /** Забыть содержимое: используется, когда выгружается мир. */
    void unload() {
        value = null;
        document = null;
        path = null;
    }

    private void createFromDefaults() {
        document = ConfigDocuments.empty(spec.format());
        value = spec.defaults()
            .get();
        save();
    }

    private ConfigDocument read() {
        try {
            return ConfigDocuments.read(spec.format(), path);
        } catch (IOException | RuntimeException failure) {
            log.warn("Failed to read config {}: {}", describe(), failure.toString());
            return null;
        }
    }

    private T parse(ConfigDocument read) {
        try {
            T parsed = read.bind(spec.type());
            if (parsed == null) {
                return null;
            }
            spec.validator()
                .accept(parsed);
            return parsed;
        } catch (RuntimeException failure) {
            log.warn("Failed to parse config {}: {}", describe(), failure.toString());
            return null;
        }
    }

    private void reportUnknownKeys() {
        List<String> unknown = document.unknownKeys(spec.type());
        if (!unknown.isEmpty()) {
            log.info("Config {} has keys this mod does not read, they are left as they are: {}", describe(), unknown);
        }
    }

    private void quarantine() {
        Path broken = brokenPath();
        try {
            Files.move(path, broken, StandardCopyOption.REPLACE_EXISTING);
            log.warn("Config {} was moved to {} and replaced with defaults", describe(), broken);
        } catch (IOException failure) {
            log.error("Failed to set aside broken config {}: {}", describe(), failure.toString());
        }
    }

    private void keepCopy() {
        Path broken = brokenPath();
        try {
            Files.copy(path, broken, StandardCopyOption.REPLACE_EXISTING);
            log.warn(
                "Config {} stopped short of schema version {}; the file is left as it was and copied to {}",
                describe(),
                spec.schemaVersion(),
                broken);
        } catch (IOException failure) {
            log.error("Failed to copy unmigrated config {}: {}", describe(), failure.toString());
        }
    }

    private Path brokenPath() {
        return path.resolveSibling(
            path.getFileName()
                .toString() + ConfigKeys.BROKEN_SUFFIX);
    }

    private String describe() {
        return spec.role() + "/" + ConfigPaths.fileName(spec);
    }
}
