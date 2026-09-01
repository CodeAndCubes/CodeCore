package com.mrleonardos.codecore.internal.config;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.config.ConfigSpec;

/**
 * Хранит все открытые файлы настроек и следит за их жизненным циклом.
 *
 * <p>
 * Настройки сервера и клиента читаются сразу при открытии, а состояние мира — только когда мир загружен,
 * и выгружается вместе с ним. Поэтому мод может спокойно открыть такой файл на старте: значения появятся
 * тогда, когда появится мир.
 */
public final class JsonConfigService implements ConfigService {

    private final Map<String, JsonConfigFile<?>> files = new LinkedHashMap<>();
    private final Map<String, ConfigSpec<?>> specs = new LinkedHashMap<>();
    private final ConfigPaths paths;
    private final Logger log;

    public JsonConfigService(ConfigPaths paths, Logger log) {
        this.paths = paths;
        this.log = log;
    }

    @Override
    public Path directory(String modid) {
        return paths.modDirectory(modid);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ConfigFile<T> open(ConfigSpec<T> spec) {
        String key = key(spec);
        JsonConfigFile<?> existing = files.get(key);
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

        JsonConfigFile<T> file = new JsonConfigFile<>(spec, paths, log);
        files.put(key, file);
        specs.put(key, spec);
        if (!paths.needsWorld(spec) || paths.worldAvailable()) {
            file.load();
        }
        return file;
    }

    @Override
    public void reloadAll() {
        for (Map.Entry<String, JsonConfigFile<?>> entry : files.entrySet()) {
            ConfigSpec<?> spec = specs.get(entry.getKey());
            if (paths.needsWorld(spec) && !paths.worldAvailable()) {
                continue;
            }
            entry.getValue()
                .reload();
        }
        log.info("Reloaded {} config file(s)", files.size());
    }

    /** Мир загружен: подключить и прочитать всё, что к нему привязано. */
    public void attachWorld(Path worldDirectory) {
        paths.worldDirectory(worldDirectory);
        forEachWorldFile(JsonConfigFile::load);
    }

    /** Мир выгружается: сохранить состояние и забыть его до следующего мира. */
    public void detachWorld() {
        forEachWorldFile(file -> {
            file.save();
            file.unload();
        });
        paths.worldDirectory(null);
    }

    private void forEachWorldFile(Consumer<JsonConfigFile<?>> action) {
        for (Map.Entry<String, JsonConfigFile<?>> entry : files.entrySet()) {
            if (paths.needsWorld(specs.get(entry.getKey()))) {
                action.accept(entry.getValue());
            }
        }
    }

    private static String key(ConfigSpec<?> spec) {
        return spec.scope() + ":" + spec.modid() + "/" + spec.name();
    }
}
