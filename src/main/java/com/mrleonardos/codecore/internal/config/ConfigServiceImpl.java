package com.mrleonardos.codecore.internal.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.AuditSettings;
import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.config.SectionSpec;
import com.mrleonardos.codecore.api.config.StorageSettings;
import com.mrleonardos.codecore.internal.text.Texts;

/**
 * Хранит все открытые файлы настроек и следит за их жизненным циклом.
 *
 * <p>
 * Настройки сервера и клиента читаются сразу при открытии, а состояние мира только когда мир загружен,
 * и выгружается вместе с ним. Поэтому мод может спокойно открыть такой файл на старте: значения появятся
 * тогда, когда появится мир.
 */
public final class ConfigServiceImpl implements ConfigService {

    private final Map<String, ConfigFileImpl<?>> files = new LinkedHashMap<>();
    private final Map<String, ConfigSpec<?>> specs = new LinkedHashMap<>();
    private final ConfigPaths paths;
    private final MainConfig main;
    private final Logger log;

    public ConfigServiceImpl(ConfigPaths paths, Logger log) {
        this.paths = paths;
        this.log = log;
        this.main = new MainConfig(paths, log);
        this.main.load();
        Texts.language(main.language());
    }

    @Override
    public Path directory(String role) {
        return paths.roleDirectory(role);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ConfigFile<T> open(ConfigSpec<T> spec) {
        String key = key(spec);
        ConfigFileImpl<?> existing = files.get(key);
        if (existing != null) {
            ConfigSpec<?> existingSpec = specs.get(key);
            if (existingSpec.type() != spec.type()) {
                throw new IllegalStateException(
                    "Config " + key
                        + " is already open as "
                        + existingSpec.type()
                            .getName());
            }
            return (ConfigFile<T>) existing;
        }

        ConfigFileImpl<T> file = new ConfigFileImpl<>(spec, paths, log);
        files.put(key, file);
        specs.put(key, spec);
        if (!paths.needsWorld(spec) || paths.worldAvailable()) {
            file.load();
        }
        return file;
    }

    @Override
    public <T> ConfigFile<T> section(SectionSpec<T> spec) {
        return main.section(spec);
    }

    @Override
    public ConfigData main() {
        return main.data();
    }

    @Override
    public String serverId() {
        return main.serverId();
    }

    @Override
    public StorageSettings storage(String role) {
        return main.storage(role);
    }

    @Override
    public AuditSettings audit(String role) {
        return main.audit(role);
    }

    @Override
    public void reloadAll() {
        main.reload();
        Texts.language(main.language());
        int reloaded = 0;
        for (Map.Entry<String, ConfigFileImpl<?>> entry : files.entrySet()) {
            ConfigSpec<?> spec = specs.get(entry.getKey());
            if (paths.needsWorld(spec) && !paths.worldAvailable()) {
                continue;
            }
            entry.getValue()
                .reload();
            reloaded++;
        }
        log.info("Reloaded {} config file(s) and the main config", reloaded);
    }

    /** Конец постинициализации: набор секций и ролей больше не меняется, главный файл пишется. */
    public void seal(List<String> roles) {
        main.seal(roles);
    }

    /** Мир загружен: подключить и прочитать всё, что к нему привязано. */
    public void attachWorld(Path worldDirectory) {
        paths.worldDirectory(worldDirectory);
        forEachWorldFile(ConfigFileImpl::load);
    }

    /** Мир выгружается: сохранить состояние и забыть его до следующего мира. */
    public void detachWorld() {
        forEachWorldFile(file -> {
            file.save();
            file.unload();
        });
        paths.worldDirectory(null);
    }

    private void forEachWorldFile(Consumer<ConfigFileImpl<?>> action) {
        for (Map.Entry<String, ConfigFileImpl<?>> entry : files.entrySet()) {
            if (paths.needsWorld(specs.get(entry.getKey()))) {
                action.accept(entry.getValue());
            }
        }
    }

    private static String key(ConfigSpec<?> spec) {
        return spec.scope() + ":" + spec.role() + "/" + ConfigPaths.fileName(spec);
    }
}
