package com.mrleonardos.codecore.internal.config;

import java.nio.file.Path;

import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;

/**
 * Куда ложится файл настроек.
 *
 * <p>
 * Папка конфигов известна с запуска игры, а папка мира появляется только при его загрузке и меняется при
 * переходе в другой мир, поэтому она задаётся отдельно.
 */
public final class ConfigPaths {

    private final Path configDirectory;
    private Path worldDirectory;

    public ConfigPaths(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    /** Запомнить папку загруженного мира или {@code null}, если мир выгружен. */
    public void worldDirectory(Path directory) {
        this.worldDirectory = directory;
    }

    /** Доступна ли папка мира прямо сейчас. */
    public boolean worldAvailable() {
        return worldDirectory != null;
    }

    /** Папка настроек мода — основание для всего, что мод хранит рядом с конфигом. */
    public Path modDirectory(String modid) {
        return configDirectory.resolve(modid);
    }

    /** Нужен ли этому файлу загруженный мир. */
    public boolean needsWorld(ConfigSpec<?> spec) {
        return spec.scope() == ConfigScope.WORLD_STATE;
    }

    /**
     * Полный путь к файлу.
     *
     * @throws IllegalStateException если файл принадлежит миру, а мир не загружен
     */
    public Path resolve(ConfigSpec<?> spec) {
        String fileName = spec.name() + ConfigKeys.FILE_EXTENSION;
        switch (spec.scope()) {
            case SETTINGS:
                return configDirectory.resolve(spec.modid())
                    .resolve(fileName);
            case CLIENT:
                return configDirectory.resolve(spec.modid())
                    .resolve(ConfigKeys.CLIENT_DIRECTORY)
                    .resolve(fileName);
            case WORLD_STATE:
                if (worldDirectory == null) {
                    throw new IllegalStateException(
                        "World state config " + spec.modid()
                            + "/"
                            + spec.name()
                            + " requested while no world is loaded");
                }
                return worldDirectory.resolve(spec.modid())
                    .resolve(fileName);
            default:
                throw new IllegalArgumentException("Unknown config scope " + spec.scope());
        }
    }
}
