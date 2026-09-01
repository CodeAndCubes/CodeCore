package com.mrleonardos.codecore.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigSpec;

class ConfigCommentsTest {

    private static final Logger LOG = LogManager.getLogger(ConfigCommentsTest.class);
    private static final String MODID = "codecore";
    private static final String NAME = "settings";
    private static final String FILE = "core-settings.toml";

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("описание поля и заголовок секции доезжают до файла")
    void annotationsReachTheFile() throws IOException {
        open(Settings.class);

        assertTrue(text().contains("# Как здороваемся с игроком."), text());
        assertTrue(text().contains("# Что показывать в углу экрана."), text());
        assertTrue(text().contains("# Сколько плиток видно сразу."), text());
        assertTrue(text().indexOf("# Что показывать в углу экрана.") < text().indexOf("[corner]"), text());
    }

    @Test
    @DisplayName("описание класса-корня становится шапкой файла")
    void rootCommentBecomesTheHeader() throws IOException {
        ConfigFile<Titled> file = open(Titled.class);

        assertTrue(text().startsWith("# Файл настроек ядра.\n# Правьте на здоровье."), text());

        file.save();

        assertEquals(1, count(text(), "# Файл настроек ядра."), "повторная запись шапку не удваивает");
    }

    @Test
    @DisplayName("правка описания в моде переписывает старую строку, а не добавляет вторую")
    void changedAnnotationReplacesTheOldLine() throws IOException {
        open(Settings.class);
        assertTrue(text().contains("# Как здороваемся с игроком."));

        ConfigFile<Renamed> second = open(Renamed.class);
        second.save();

        assertTrue(text().contains("# Здесь лежит приветствие."), text());
        assertFalse(text().contains("# Как здороваемся с игроком."), text());
    }

    @Test
    @DisplayName("комментарий человека и его собственный ключ переживают запись")
    void humanCommentAndForeignKeySurvive() throws IOException {
        open(Settings.class);
        write(after("greeting = \"привет\"", "\n# заметка админа\nmyOwnNote = \"не трогать\""));

        ConfigFile<Settings> file = open(Settings.class);
        file.get().greeting = "изменено";
        file.save();

        assertTrue(text().contains("# заметка админа"), text());
        assertTrue(text().contains("myOwnNote = \"не трогать\""), text());
        assertTrue(text().contains("greeting = \"изменено\""), text());
    }

    @Test
    @DisplayName("ключ, которого нет в классе настроек, назван при загрузке")
    void unknownKeysAreListed() throws IOException {
        Files.createDirectories(path().getParent());
        write("schemaVersion = 1\ngreetnig = \"опечатка\"\n[corner]\ntiels = 9\n");

        TomlDocument document = TomlDocument.read(path());

        assertEquals(
            "[greetnig, corner.tiels]",
            document.unknownKeys(Settings.class)
                .toString());
    }

    @Test
    @DisplayName("запись карты удаляет то, что мод из неё убрал, и оставляет чужое внутри записи")
    void mapEntriesAreReplacedButTheirForeignKeysStay() throws IOException {
        ConfigFile<Settings> file = open(Settings.class);
        file.get().slots.put("first", new Slot());
        file.get().slots.put("second", new Slot());
        file.save();
        write(after("[slots.first]", "\nownField = 7"));

        ConfigFile<Settings> reopened = open(Settings.class);
        reopened.get().slots.remove("second");
        reopened.save();

        assertTrue(text().contains("[slots.first]"), text());
        assertFalse(text().contains("[slots.second]"), text());
        assertTrue(text().contains("ownField = 7"), "чужой ключ внутри записи остаётся");
    }

    private <T> ConfigFile<T> open(Class<T> type) {
        return new ConfigServiceImpl(new ConfigPaths(configDirectory), LOG).open(
            ConfigSpec.of(MODID, NAME, type)
                .role(ConfigRoles.CORE)
                .build());
    }

    private Path path() {
        return configDirectory.resolve("code")
            .resolve("core")
            .resolve(FILE);
    }

    private String text() throws IOException {
        return new String(Files.readAllBytes(path()), StandardCharsets.UTF_8);
    }

    private void write(String content) throws IOException {
        Files.createDirectories(path().getParent());
        Files.write(path(), content.getBytes(StandardCharsets.UTF_8));
    }

    private static int count(String text, String part) {
        int found = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
            found++;
        }
        return found;
    }

    private String after(String anchor, String addition) throws IOException {
        String current = text();
        int at = current.indexOf(anchor);
        assertTrue(at >= 0, current);
        return current.substring(0, at + anchor.length()) + addition + current.substring(at + anchor.length());
    }

    public static final class Settings {

        @Comment("Как здороваемся с игроком.")
        public String greeting = "привет";

        @Comment("Что показывать в углу экрана.")
        public Corner corner = new Corner();

        public Map<String, Slot> slots = new LinkedHashMap<>();
    }

    @Comment({ "Файл настроек ядра.", "Правьте на здоровье." })
    public static final class Titled {

        public String greeting = "привет";
    }

    public static final class Renamed {

        @Comment("Здесь лежит приветствие.")
        public String greeting = "привет";
    }

    public static final class Corner {

        @Comment("Сколько плиток видно сразу.")
        public int tiles = 4;
    }

    public static final class Slot {

        public boolean shown = true;
    }
}
