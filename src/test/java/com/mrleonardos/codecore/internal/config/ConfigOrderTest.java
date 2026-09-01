package com.mrleonardos.codecore.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigSpec;

class ConfigOrderTest {

    private static final Logger LOG = LogManager.getLogger(ConfigOrderTest.class);
    private static final String MODID = "codecore";
    private static final String NAME = "settings";
    private static final String FILE = "core-settings.toml";
    private static final Pattern SECTION = Pattern.compile("\\[channels\\.([^]]+)]");
    private static final List<String> CHANNELS = Arrays.asList("local", "global", "trade", "help", "staff");

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("порядок ключей карты переживает перечитывание и запись")
    void mapKeepsTheOrderOfTheFile() throws IOException {
        ConfigFile<Settings> file = open();
        for (String name : CHANNELS) {
            file.get().channels.put(name, new Channel());
        }
        file.save();
        assertEquals(CHANNELS, sections(), "первая запись идёт в порядке карты");

        file.reload();

        assertEquals(CHANNELS, new ArrayList<>(file.get().channels.keySet()), "разбор отдаёт ключи в порядке файла");

        file.save();
        assertEquals(CHANNELS, sections(), "повторная запись файл не тасует");
    }

    @Test
    @DisplayName("строка администратора остаётся над своей секцией")
    void humanCommentStaysAboveItsSection() throws IOException {
        ConfigFile<Settings> file = open();
        for (String name : CHANNELS) {
            file.get().channels.put(name, new Channel());
        }
        file.save();
        write(before("[channels.staff]", "# только для модераторов\n"));

        file.reload();
        file.save();

        String text = text();
        assertTrue(text.contains("# только для модераторов"), text);
        assertEquals(
            "# только для модераторов",
            lineBefore(text, "[channels.staff]"),
            "строка стоит над staff, а не над чужой секцией");
    }

    private ConfigFile<Settings> open() {
        return new ConfigServiceImpl(new ConfigPaths(configDirectory), LOG).open(
            ConfigSpec.of(MODID, NAME, Settings.class)
                .role(ConfigRoles.CORE)
                .build());
    }

    private List<String> sections() throws IOException {
        List<String> found = new ArrayList<>();
        Matcher matcher = SECTION.matcher(text());
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private static String lineBefore(String text, String anchor) {
        List<String> lines = Arrays.asList(text.split("\n", -1));
        int at = lines.indexOf(anchor);
        assertTrue(at > 0, text);
        return lines.get(at - 1)
            .trim();
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
        Files.write(path(), content.getBytes(StandardCharsets.UTF_8));
    }

    private String before(String anchor, String addition) throws IOException {
        String current = text();
        int at = current.indexOf(anchor);
        assertTrue(at >= 0, current);
        return current.substring(0, at) + addition + current.substring(at);
    }

    public static final class Settings {

        public String greeting = "привет";

        public Map<String, Channel> channels = new LinkedHashMap<>();
    }

    public static final class Channel {

        public boolean shown = true;
    }
}
