package com.mrleonardos.codecore.internal.config;

import java.nio.file.Path;

import com.mrleonardos.codecore.api.config.ConfigRoles;
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

    /** Папка игры с конфигами: в ней лежит и папка линейки, и папки прежней раскладки. */
    public Path configDirectory() {
        return configDirectory;
    }

    /** Папка линейки: {@code config/code}. */
    public Path lineup() {
        return configDirectory.resolve(ConfigKeys.LINEUP_DIRECTORY);
    }

    /** Главный файл линейки. */
    public Path mainFile() {
        return lineup().resolve(ConfigKeys.MAIN_FILE);
    }

    /** Папка области ответственности: {@code config/code/<роль>}. */
    public Path roleDirectory(String role) {
        return lineup().resolve(ConfigRoles.check(role));
    }

    /** Нужен ли этому файлу загруженный мир. */
    public boolean needsWorld(ConfigSpec<?> spec) {
        return spec.scope() == ConfigScope.WORLD_STATE;
    }

    /** Имя файла: {@code <владелец>.toml} или {@code <владелец>-<имя>.<расширение>}. */
    public static String fileName(ConfigSpec<?> spec) {
        String owner = ConfigOwners.of(spec.modid());
        String suffix = spec.name()
            .isEmpty() ? "" : ConfigKeys.NAME_SEPARATOR + spec.name();
        return owner + suffix
            + spec.format()
                .extension();
    }

    /**
     * Полный путь к файлу.
     *
     * @throws IllegalStateException если файл принадлежит миру, а мир не загружен
     */
    public Path resolve(ConfigSpec<?> spec) {
        String role = ConfigRoles.check(spec.role());
        String fileName = fileName(spec);
        switch (spec.scope()) {
            case SETTINGS:
                return lineup().resolve(role)
                    .resolve(fileName);
            case CLIENT:
                return lineup().resolve(role)
                    .resolve(ConfigKeys.CLIENT_DIRECTORY)
                    .resolve(fileName);
            case WORLD_STATE:
                if (worldDirectory == null) {
                    throw new IllegalStateException(
                        "World state config " + role + "/" + fileName + " requested while no world is loaded");
                }
                return worldDirectory.resolve(ConfigKeys.LINEUP_DIRECTORY)
                    .resolve(role)
                    .resolve(fileName);
            default:
                throw new IllegalArgumentException("Unknown config scope " + spec.scope());
        }
    }
}
