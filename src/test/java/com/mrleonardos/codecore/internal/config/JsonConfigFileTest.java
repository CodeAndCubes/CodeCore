package com.mrleonardos.codecore.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.config.Migration;

class JsonConfigFileTest {

    private static final Logger LOG = LogManager.getLogger(JsonConfigFileTest.class);
    private static final String MODID = "codecore";
    private static final String NAME = "settings";

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("новый файл создаётся со значениями по умолчанию")
    void missingFileIsCreatedWithDefaults() throws IOException {
        Settings settings = open(spec().build()).get();

        assertEquals("привет", settings.greeting);
        assertTrue(text().contains("\"greeting\""));
        assertTrue(text().contains(ConfigKeys.SCHEMA_VERSION));
    }

    @Test
    @DisplayName("битый файл откладывается рядом, работа идёт со значениями по умолчанию")
    void brokenFileIsQuarantined() throws IOException {
        write("{ это не json ");

        ConfigFile<Settings> file = open(spec().build());

        assertEquals("привет", file.get().greeting);
        assertTrue(Files.isRegularFile(broken()), "копия битого файла должна остаться");
        assertEquals("{ это не json ", new String(Files.readAllBytes(broken()), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("дошедшая до конца цепочка миграций переписывает файл")
    void completeMigrationRewritesTheFile() throws IOException {
        write("{\"schemaVersion\":1,\"oldGreeting\":\"здорово\"}");

        ConfigFile<Settings> file = open(
            spec().schemaVersion(2)
                .migration(renameGreeting())
                .build());

        assertEquals("здорово", file.get().greeting);
        assertTrue(text().contains("\"schemaVersion\": 2"), text());
        assertFalse(Files.exists(broken()), "потери данных не было, откладывать нечего");
    }

    @Test
    @DisplayName("оборванная цепочка миграций не переписывает файл и оставляет копию")
    void incompleteMigrationKeepsTheFile() throws IOException {
        String original = "{\"schemaVersion\":1,\"oldGreeting\":\"здорово\",\"awaitedByStepFour\":\"важное\"}";
        write(original);

        ConfigFile<Settings> file = open(
            spec().schemaVersion(5)
                .migration(renameGreeting())
                .build());

        assertNotNull(file.get());
        assertEquals(original, text(), "файл, не доведённый до целевой схемы, переписывать нельзя");
        assertEquals(original, new String(Files.readAllBytes(broken()), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("файл из будущей версии мода остаётся нетронутым")
    void newerFileIsLeftAlone() throws IOException {
        String original = "{\"schemaVersion\":9,\"greeting\":\"из будущего\"}";
        write(original);

        ConfigFile<Settings> file = open(spec().build());

        assertEquals("из будущего", file.get().greeting);
        assertEquals(original, text());
    }

    @Test
    @DisplayName("проверка чинит то, что пришло из json")
    void validatorRepairsTheParsedValue() throws IOException {
        write("{\"schemaVersion\":1,\"greeting\":null}");

        ConfigFile<Settings> file = open(
            spec().validator(value -> value.greeting = value.greeting == null ? "починено" : value.greeting)
                .build());

        assertEquals("починено", file.get().greeting);
    }

    @Test
    @DisplayName("сохранение атомарное: временного файла после него не остаётся")
    void savingLeavesNoTemporaryFile() throws IOException {
        ConfigFile<Settings> file = open(spec().build());
        file.get().greeting = "изменено";
        file.save();

        assertTrue(text().contains("изменено"));
        assertFalse(
            Files.exists(path().resolveSibling(NAME + ConfigKeys.FILE_EXTENSION + ConfigKeys.TEMPORARY_SUFFIX)));
    }

    @Test
    @DisplayName("повторное открытие того же файла другим классом отклонено")
    void reopeningWithAnotherTypeIsRefused() {
        JsonConfigService service = service();
        service.open(spec().build());

        assertThrows(
            IllegalStateException.class,
            () -> service.open(
                ConfigSpec.of(MODID, NAME, Other.class)
                    .scope(ConfigScope.SETTINGS)
                    .build()));
    }

    private ConfigSpec.Builder<Settings> spec() {
        return ConfigSpec.of(MODID, NAME, Settings.class)
            .scope(ConfigScope.SETTINGS);
    }

    private ConfigFile<Settings> open(ConfigSpec<Settings> spec) {
        return service().open(spec);
    }

    private JsonConfigService service() {
        return new JsonConfigService(new ConfigPaths(configDirectory), LOG);
    }

    private Path path() {
        return configDirectory.resolve(MODID)
            .resolve(NAME + ConfigKeys.FILE_EXTENSION);
    }

    private Path broken() {
        return path().resolveSibling(NAME + ConfigKeys.FILE_EXTENSION + ConfigKeys.BROKEN_SUFFIX);
    }

    private String text() throws IOException {
        return new String(Files.readAllBytes(path()), StandardCharsets.UTF_8);
    }

    private void write(String content) throws IOException {
        Files.createDirectories(path().getParent());
        Files.write(path(), content.getBytes(StandardCharsets.UTF_8));
    }

    private static Migration renameGreeting() {
        return new Migration() {

            @Override
            public int from() {
                return 1;
            }

            @Override
            public int to() {
                return 2;
            }

            @Override
            public void apply(JsonObject data) {
                data.add("greeting", data.remove("oldGreeting"));
            }
        };
    }

    public static final class Settings {

        public String greeting = "привет";
    }

    public static final class Other {

        public int number;
    }
}
