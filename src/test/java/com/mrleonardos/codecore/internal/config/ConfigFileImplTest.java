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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.config.Comment;
import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigFormat;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.config.Migration;
import com.mrleonardos.codecore.internal.LogCapture;

class ConfigFileImplTest {

    private static final Logger LOG = LogManager.getLogger(ConfigFileImplTest.class);
    private static final String MODID = "codecore";
    private static final String NAME = "settings";
    private static final String FILE = "core-settings.toml";

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("новый файл создаётся со значениями по умолчанию")
    void missingFileIsCreatedWithDefaults() throws IOException {
        Settings settings = open(spec().build()).get();

        assertEquals("привет", settings.greeting);
        assertTrue(text().contains("greeting = \"привет\""), text());
        assertTrue(text().contains(ConfigKeys.SCHEMA_VERSION), text());
    }

    @Test
    @DisplayName("собственный файл владельца называется его именем без добавки")
    void ownSettingsFileIsNamedAfterTheOwner() {
        ConfigFile<Settings> file = service().open(
            ConfigSpec.settings(MODID, Settings.class)
                .role(ConfigRoles.CORE)
                .build());

        assertEquals(
            configDirectory.resolve("code")
                .resolve("core")
                .resolve("core.toml"),
            file.path());
    }

    @Test
    @DisplayName("битый файл откладывается рядом, работа идёт со значениями по умолчанию")
    void brokenFileIsQuarantined() throws IOException {
        write("[unclosed\ngreeting = \"здорово\"\n");

        ConfigFile<Settings> file = open(spec().build());

        assertEquals("привет", file.get().greeting);
        assertTrue(Files.isRegularFile(broken()), "копия битого файла должна остаться");
        assertEquals("[unclosed\ngreeting = \"здорово\"\n", read(broken()));
    }

    @Test
    @DisplayName("имя секции кириллицей: файл отложен, а в логе сказано про кавычки")
    void cyrillicSectionNameGetsAHint() throws IOException {
        write("[groups.Ярмарка]\nnodes = []\n");
        LogCapture capture = LogCapture.attach(LOG);

        try {
            ConfigFile<Settings> file = open(spec().build());

            assertEquals("привет", file.get().greeting, "работа идёт со значениями по умолчанию");
            assertTrue(Files.isRegularFile(broken()), "битый файл отложен");
            assertTrue(
                capture.text()
                    .contains(broken().toString()),
                capture.text());
            assertTrue(
                capture.text()
                    .contains(ConfigHints.quotedNames()),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("дошедшая до конца цепочка миграций переписывает файл")
    void completeMigrationRewritesTheFile() throws IOException {
        write("schemaVersion = 1\noldGreeting = \"здорово\"\n");

        ConfigFile<Settings> file = open(
            spec().schemaVersion(2)
                .migration(renameGreeting())
                .build());

        assertEquals("здорово", file.get().greeting);
        assertTrue(text().contains("schemaVersion = 2"), text());
        assertFalse(Files.exists(broken()), "потери данных не было, откладывать нечего");
    }

    @Test
    @DisplayName("оборванная цепочка миграций не переписывает файл и оставляет копию")
    void incompleteMigrationKeepsTheFile() throws IOException {
        String original = "schemaVersion = 1\noldGreeting = \"здорово\"\nawaitedByStepFour = \"важное\"\n";
        write(original);

        ConfigFile<Settings> file = open(
            spec().schemaVersion(5)
                .migration(renameGreeting())
                .build());

        assertNotNull(file.get());
        assertEquals(original, text(), "файл, не доведённый до целевой схемы, переписывать нельзя");
        assertEquals(original, read(broken()));
    }

    @Test
    @DisplayName("файл из будущей версии мода остаётся нетронутым")
    void newerFileIsLeftAlone() throws IOException {
        String original = "schemaVersion = 9\ngreeting = \"из будущего\"\n";
        write(original);

        ConfigFile<Settings> file = open(spec().build());

        assertEquals("из будущего", file.get().greeting);
        assertEquals(original, text());
    }

    @Test
    @DisplayName("проверка чинит то, что пришло из файла")
    void validatorRepairsTheParsedValue() throws IOException {
        write("schemaVersion = 1\ngreeting = \"\"\n");

        ConfigFile<Settings> file = open(
            spec().validator(value -> value.greeting = value.greeting.isEmpty() ? "починено" : value.greeting)
                .build());

        assertEquals("починено", file.get().greeting);
        assertFalse(Files.exists(broken()), "починенный файл в карантин не уезжает");
    }

    @Test
    @DisplayName("по значениям от поставщика умолчаний проверка не гоняется")
    void validatorIsNotRunOnDefaults() {
        ConfigFile<Settings> file = open(
            spec().defaults(Settings::new)
                .validator(value -> value.greeting = "проверка была")
                .build());

        assertEquals("привет", file.get().greeting);
    }

    @Test
    @DisplayName("сохранение атомарное: временного файла после него не остаётся")
    void savingLeavesNoTemporaryFile() throws IOException {
        ConfigFile<Settings> file = open(spec().build());
        file.get().greeting = "изменено";
        file.save();

        assertTrue(text().contains("изменено"));
        assertFalse(Files.exists(path().resolveSibling(FILE + ConfigKeys.TEMPORARY_SUFFIX)));
    }

    @Test
    @DisplayName("сорвавшаяся запись не оставляет временный файл рядом с настройками")
    void failedWriteLeavesNoTemporaryFile() throws IOException {
        Path file = path();
        LogCapture capture = LogCapture.attach(LOG);

        try {
            ConfigWriting.atomically(file, writer -> {
                writer.write("schemaVersion = 1\n");
                throw new IOException("место кончилось");
            }, "core/" + FILE, LOG);

            assertFalse(Files.exists(file.resolveSibling(FILE + ConfigKeys.TEMPORARY_SUFFIX)), "tmp убран");
            assertFalse(Files.exists(file), "недописанный файл не занимает место настоящего");
            assertTrue(
                capture.text()
                    .contains("Failed to save config core/" + FILE),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("повторное открытие того же файла другим классом отклонено")
    void reopeningWithAnotherTypeIsRefused() {
        ConfigServiceImpl service = service();
        service.open(spec().build());

        assertThrows(
            IllegalStateException.class,
            () -> service.open(
                ConfigSpec.of(MODID, NAME, Other.class)
                    .role(ConfigRoles.CORE)
                    .build()));
    }

    @Test
    @DisplayName("машинные данные пишутся в json без комментариев")
    void machineDataStaysJson() throws IOException {
        ConfigServiceImpl service = service();
        ConfigFile<Settings> file = service.open(
            ConfigSpec.of(MODID, NAME, Settings.class)
                .role(ConfigRoles.CORE)
                .format(ConfigFormat.JSON)
                .build());

        Path json = configDirectory.resolve("code")
            .resolve("core")
            .resolve("core-settings.json");
        assertEquals(json, file.path());

        String written = read(json);
        assertTrue(written.contains("\"greeting\""), written);
        assertFalse(written.contains("#"), "в json комментариям места нет");
    }

    @Test
    @DisplayName("json с корневым массивом откладывается как битый")
    void jsonArrayIsQuarantined() throws IOException {
        Path json = configDirectory.resolve("code")
            .resolve("core")
            .resolve("core-settings.json");
        Files.createDirectories(json.getParent());
        Files.write(json, "[1, 2, 3]".getBytes(StandardCharsets.UTF_8));

        ConfigFile<Settings> file = service().open(
            ConfigSpec.of(MODID, NAME, Settings.class)
                .role(ConfigRoles.CORE)
                .format(ConfigFormat.JSON)
                .build());

        assertEquals("привет", file.get().greeting);
        assertTrue(Files.isRegularFile(json.resolveSibling("core-settings.json" + ConfigKeys.BROKEN_SUFFIX)));
    }

    @Test
    @DisplayName("новое поле мода доезжает в уже существующий файл")
    void newFieldReachesAnExistingFile() throws IOException {
        write("schemaVersion = 1\ngreeting = \"здорово\"\n");

        ConfigFile<Settings> file = open(spec().build());

        assertEquals("здорово", file.get().greeting, "прежнее значение остаётся");
        assertTrue(text().contains("tiles = 4"), text());
        assertTrue(text().contains("Как здороваемся"), text());
    }

    @Test
    @DisplayName("дополненный файл назван в логе, чтобы админ узнал о прибавке")
    void toppingUpIsAnnounced() throws IOException {
        write("schemaVersion = 1\ngreeting = \"здорово\"\n");

        LogCapture capture = LogCapture.attach(LOG);
        try {
            open(spec().build());

            assertTrue(
                capture.text()
                    .contains("topped up"),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("файл, в котором всё на месте, второй запуск не трогает")
    void completeFileIsNotRewritten() throws IOException {
        open(spec().build());
        String written = text();

        LogCapture capture = LogCapture.attach(LOG);
        try {
            open(spec().build());

            assertEquals(written, text(), "повторное открытие ничего не двигает");
            assertFalse(
                capture.text()
                    .contains("topped up"),
                capture.text());
        } finally {
            capture.detach();
        }
    }

    @Test
    @DisplayName("удалённый человеком ключ возвращается, а изменённое им значение остаётся")
    void deletedKeyComesBackAndChangedValueSurvives() throws IOException {
        write("schemaVersion = 1\ngreeting = \"моё\"\n");

        Settings settings = open(spec().build()).get();

        assertEquals("моё", settings.greeting, "изменённое человеком значение не перетёрто");
        assertEquals(4, settings.tiles, "удалённый ключ работает на заводском значении");
        assertTrue(text().contains("greeting = \"моё\""), text());
        assertTrue(text().contains("tiles = 4"), text());
    }

    @Test
    @DisplayName("файл из будущей версии не дополняется, хотя поля мода в нём нет")
    void newerFileIsNotToppedUp() throws IOException {
        String original = "schemaVersion = 9\ngreeting = \"из будущего\"\n";
        write(original);

        open(spec().build());

        assertEquals(original, text(), "мод новее нашего сам знает, что писать в свой файл");
    }

    @Test
    @DisplayName("битый файл уезжает в карантин, а не дополняется")
    void brokenFileIsNotToppedUp() throws IOException {
        write("[unclosed\ngreeting = \"здорово\"\n");

        open(spec().build());

        assertEquals("[unclosed\ngreeting = \"здорово\"\n", read(broken()), "битое отложено как было");
        assertTrue(text().contains("greeting = \"привет\""), text());
    }

    @Test
    @DisplayName("оборванная цепочка миграций не дополняется: шаги ждут своих полей")
    void incompleteMigrationIsNotToppedUp() throws IOException {
        String original = "schemaVersion = 1\noldGreeting = \"здорово\"\nawaitedByStepFour = \"важное\"\n";
        write(original);

        open(
            spec().schemaVersion(5)
                .migration(renameGreeting())
                .build());

        assertEquals(original, text(), "файл с оборванной цепочкой не трогают вовсе");
    }

    @Test
    @DisplayName("строка человека над полем мода уступает описанию поля, над своим ключом остаётся")
    void commentOwnershipIsSplitByWhoOwnsTheKey() throws IOException {
        write("schemaVersion = 1\n" + "# моя строка\n" + "greeting = \"моё\"\n" + "# моя над чужим\n" + "myOwn = 1\n");

        open(spec().build());

        assertTrue(text().contains("greeting = \"моё\""), text());
        assertTrue(text().contains("tiles = 4"), text());
        assertTrue(text().contains("Как здороваемся"), "описание поля мода на месте: " + text());
        assertFalse(text().contains("# моя строка"), "строка над полем мода уступила описанию: " + text());
        assertTrue(text().contains("# моя над чужим"), "строка над своим ключом остаётся: " + text());
        assertTrue(text().contains("myOwn = 1"), text());
    }

    @Test
    @DisplayName("над полем без описания строка человека тоже остаётся: моду там сказать нечего")
    void commentOverAnUndocumentedFieldSurvives() throws IOException {
        write("# моя над tiles\n" + "tiles = 7\n" + "greeting = \"моё\"\n");

        Settings settings = open(spec().build()).get();

        assertEquals(7, settings.tiles);
        assertTrue(text().contains(ConfigKeys.SCHEMA_VERSION), "дозапись сработала: " + text());
        assertTrue(text().contains("# моя над tiles"), text());
    }

    @Test
    @DisplayName("правило про комментарии стоит в шапке любого файла и не двоится при повторной записи")
    void theCommentRuleStandsInEveryHeaderOnce() throws IOException {
        ConfigFile<Settings> file = open(spec().build());
        String created = text();

        assertTrue(created.contains("Свою заметку кладите над своим ключом"), created);

        file.save();
        file.save();

        assertEquals(created, text(), "повторная запись шапку не двоит");
        assertEquals(1, countOf(text(), "Свою заметку кладите над своим ключом"), text());
    }

    @Test
    @DisplayName("в машинный json правило не попадает: комментариев там нет вовсе")
    void theRuleStaysOutOfMachineJson() throws IOException {
        ConfigFile<Settings> file = service().open(
            ConfigSpec.of(MODID, NAME, Settings.class)
                .role(ConfigRoles.CORE)
                .format(ConfigFormat.JSON)
                .build());

        String json = read(file.path());

        assertFalse(json.contains("Свою заметку"), json);
        assertFalse(json.contains("#"), json);
    }

    @Test
    @DisplayName("чужой файл читается как есть, ядро в него ничего не дописывает")
    void aForeignFileIsReadWithoutBeingWritten() throws IOException {
        write("greeting = \"чужое\"\n");
        String before = text();

        ConfigFile<Settings> file = open(
            spec().foreign()
                .build());

        assertEquals("чужое", file.get().greeting);
        assertEquals(before, text(), "мод читает файл соседа, а не правит его");
    }

    @Test
    @DisplayName("отсутствующий чужой файл не создаётся")
    void aMissingForeignFileIsNotCreated() {
        ConfigFile<Settings> file = open(
            spec().foreign()
                .build());

        assertFalse(file.loaded());
        assertFalse(Files.isRegularFile(path()), "заводить чужой файл за соседа не наше дело");
    }

    @Test
    @DisplayName("нечитаемый чужой файл остаётся на месте")
    void anUnreadableForeignFileStaysWhereItIs() throws IOException {
        write("greeting = \n");
        String before = text();

        ConfigFile<Settings> file = open(
            spec().foreign()
                .build());

        assertFalse(file.loaded());
        assertEquals(before, text());
        assertFalse(Files.exists(broken()), "чужой файл не отодвигается в .broken");
    }

    @Test
    @DisplayName("чужой файл пишется явным сохранением")
    void aForeignFileIsWrittenOnlyWhenAsked() throws IOException {
        write("greeting = \"чужое\"\n");
        ConfigFile<Settings> file = open(
            spec().foreign()
                .build());

        file.get().greeting = "наше";
        file.save();

        assertTrue(text().contains("greeting = \"наше\""), text());
    }

    private static int countOf(String text, String piece) {
        int found = 0;
        int at = text.indexOf(piece);
        while (at >= 0) {
            found++;
            at = text.indexOf(piece, at + piece.length());
        }
        return found;
    }

    private ConfigSpec.Builder<Settings> spec() {
        return ConfigSpec.of(MODID, NAME, Settings.class)
            .role(ConfigRoles.CORE)
            .scope(ConfigScope.SETTINGS);
    }

    @Test
    @DisplayName("пустая карта пишется заголовком секции, и админ дописывает в неё руками")
    void anEmptyMapIsWrittenAsATableHeader() throws IOException {
        open(spec().build());

        assertTrue(text().contains("[entries]"), text());
        assertFalse(text().contains("entries = {}"), "встроенную таблицу нельзя дополнить секцией ниже");

        write(text() + "\n[entries.first]\nnumber = 7\n");
        Settings settings = open(spec().build()).get();

        assertEquals(7, settings.entries.get("first").number, "дописанная руками секция должна прочитаться");
        assertFalse(Files.exists(broken()), "файл не должен был уехать в .broken");
    }

    private ConfigFile<Settings> open(ConfigSpec<Settings> spec) {
        return service().open(spec);
    }

    private ConfigServiceImpl service() {
        return new ConfigServiceImpl(new ConfigPaths(configDirectory), LOG);
    }

    private Path path() {
        return configDirectory.resolve("code")
            .resolve("core")
            .resolve(FILE);
    }

    private Path broken() {
        return path().resolveSibling(FILE + ConfigKeys.BROKEN_SUFFIX);
    }

    private String text() throws IOException {
        return read(path());
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
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
            public void apply(ConfigData data) {
                data.set("greeting", data.get("oldGreeting"));
                data.remove("oldGreeting");
            }
        };
    }

    public static final class Settings {

        @Comment("Как здороваемся с игроком.")
        public String greeting = "привет";

        public int tiles = 4;

        @Comment("Записи, которые заводит админ.")
        public Map<String, Other> entries = new LinkedHashMap<>();
    }

    public static final class Other {

        public int number;
    }
}
