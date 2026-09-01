package com.mrleonardos.codecore.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigFormat;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;

class ConfigPathsTest {

    private static final Logger LOG = LogManager.getLogger(ConfigPathsTest.class);

    @TempDir
    Path configDirectory;

    @TempDir
    Path worldDirectory;

    @Test
    @DisplayName("владелец получается из modid отбрасыванием приставки code")
    void ownerComesFromModid() {
        assertEquals("core", ConfigOwners.of("codecore"));
        assertEquals("perms", ConfigOwners.of("codeperms"));
        assertEquals("economy", ConfigOwners.of("codeeconomy"));
        assertEquals("essentials", ConfigOwners.of("codeessentials"));
        assertEquals("chat", ConfigOwners.of("codechat"));
        assertEquals("myperms", ConfigOwners.of("myperms"), "чужой modid идёт в имя целиком");
        assertEquals("code", ConfigOwners.of("code"));
    }

    @Test
    @DisplayName("файл настроек мода лежит в папке своей роли")
    void settingsFileGoesToItsRole() {
        ConfigSpec<Settings> spec = ConfigSpec.settings("codeperms", Settings.class)
            .role(ConfigRoles.PERMISSIONS)
            .build();

        assertEquals(lineup("permissions", "perms.toml"), paths().resolve(spec));
    }

    @Test
    @DisplayName("два владельца одной роли кладут файлы рядом под разными именами")
    void twoOwnersShareTheRoleDirectory() {
        ConfigSpec<Settings> core = ConfigSpec.of("codecore", "groups", Settings.class)
            .role(ConfigRoles.PERMISSIONS)
            .build();
        ConfigSpec<Settings> perms = ConfigSpec.of("codeperms", "groups", Settings.class)
            .role(ConfigRoles.PERMISSIONS)
            .build();

        assertEquals(lineup("permissions", "core-groups.toml"), paths().resolve(core));
        assertEquals(lineup("permissions", "perms-groups.toml"), paths().resolve(perms));
    }

    @Test
    @DisplayName("клиентские предпочтения лежат в подпапке client")
    void clientPreferencesGoToTheClientDirectory() {
        ConfigSpec<Settings> spec = ConfigSpec.of("codechat", "client", Settings.class)
            .role(ConfigRoles.CHAT)
            .scope(ConfigScope.CLIENT)
            .build();

        assertEquals(
            lineup("chat").resolve("client")
                .resolve("chat-client.toml"),
            paths().resolve(spec));
    }

    @Test
    @DisplayName("состояние мира лежит рядом с сохранением")
    void worldStateGoesNextToTheSave() {
        ConfigPaths paths = paths();
        paths.worldDirectory(worldDirectory);
        ConfigSpec<Settings> spec = ConfigSpec.of("codeessentials", "players", Settings.class)
            .role(ConfigRoles.ESSENTIALS)
            .scope(ConfigScope.WORLD_STATE)
            .format(ConfigFormat.JSON)
            .build();

        assertEquals(
            worldDirectory.resolve("code")
                .resolve("essentials")
                .resolve("essentials-players.json"),
            paths.resolve(spec));
    }

    @Test
    @DisplayName("имя роли не из алфавита отклоняется до обращения к диску")
    void badRoleNameIsRefusedBeforeTouchingTheDisk() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ConfigSpec.settings("codeperms", Settings.class)
                .role("permissions/extra"));
        assertThrows(
            IllegalArgumentException.class,
            () -> ConfigSpec.settings("codeperms", Settings.class)
                .role("Permissions"));
        assertThrows(
            IllegalArgumentException.class,
            () -> ConfigSpec.settings("codeperms", Settings.class)
                .role(""));

        assertFalse(Files.exists(configDirectory.resolve("code")), "каталог создаваться не должен");
    }

    @Test
    @DisplayName("роль обязательна: без неё файл некуда класть")
    void roleIsRequired() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ConfigSpec.settings("codeperms", Settings.class)
                .build());
    }

    @Test
    @DisplayName("состояние мира без загруженного мира спрашивать нельзя")
    void worldStateNeedsALoadedWorld() {
        ConfigServiceImpl service = new ConfigServiceImpl(paths(), LOG);
        ConfigFile<Settings> file = service.open(
            ConfigSpec.of("codeessentials", "players", Settings.class)
                .role(ConfigRoles.ESSENTIALS)
                .scope(ConfigScope.WORLD_STATE)
                .format(ConfigFormat.JSON)
                .build());

        assertFalse(file.loaded());
        assertThrows(IllegalStateException.class, file::get);
    }

    @Test
    @DisplayName("папка роли отдаётся для того, что не настройки")
    void roleDirectoryIsAvailableForEverythingElse() {
        assertEquals(lineup("core"), new ConfigServiceImpl(paths(), LOG).directory(ConfigRoles.CORE));
    }

    @Test
    @DisplayName("папки прежней раскладки находятся и называются")
    void previousLayoutIsFound() throws Exception {
        assertTrue(
            LegacyLayout.find(configDirectory)
                .isEmpty(),
            "на чистой установке говорить не о чем");

        Files.createDirectories(configDirectory.resolve("codeperms"));
        Files.createDirectories(configDirectory.resolve("codechat"));

        assertEquals(
            "[codeperms, codechat]",
            LegacyLayout.find(configDirectory)
                .toString());
    }

    private ConfigPaths paths() {
        return new ConfigPaths(configDirectory);
    }

    private Path lineup(String role) {
        return configDirectory.resolve("code")
            .resolve(role);
    }

    private Path lineup(String role, String file) {
        return lineup(role).resolve(file);
    }

    public static final class Settings {

        public String greeting = "привет";
    }
}
