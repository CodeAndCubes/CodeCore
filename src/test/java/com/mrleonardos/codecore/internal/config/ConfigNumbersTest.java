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

class ConfigNumbersTest {

    private static final Logger LOG = LogManager.getLogger(ConfigNumbersTest.class);
    private static final String MODID = "codecore";
    private static final String NAME = "settings";
    private static final String FILE = "core-settings.toml";
    private static final float THIRD = 1.0F / 3.0F;

    @TempDir
    Path configDirectory;

    @Test
    @DisplayName("float пишется тем же числом, что стоит в коде")
    void floatKeepsItsShortestForm() throws IOException {
        open().save();

        assertEquals("0.55", value("backgroundOpacity"));
        assertEquals("0.1", value("tenth"));
        assertEquals("0.33333334", value("third"));
        assertEquals("1.0", value("whole"));
    }

    @Test
    @DisplayName("double остаётся собой, целые не превращаются в дробные")
    void otherNumbersAreUntouched() throws IOException {
        open().save();

        assertEquals("0.1", value("precise"));
        assertEquals("7", value("count"));
        assertEquals("9000000000", value("huge"));
    }

    @Test
    @DisplayName("float внутри карты и внутри списка пишется так же")
    void floatInsideMapAndListKeepsItsForm() throws IOException {
        ConfigFile<Settings> file = open();
        file.get().slots.put("first", new Slot());
        file.get().steps.addAll(Arrays.asList(0.55F, 0.1F, THIRD));
        file.save();

        assertEquals("[0.55, 0.1, 0.33333334]", value("steps"));
        assertEquals("0.55", value("volume"));
    }

    @Test
    @DisplayName("круговой прогон не двигает ни значение, ни строку в файле")
    void roundTripChangesNothing() throws IOException {
        ConfigFile<Settings> file = open();
        file.get().slots.put("first", new Slot());
        file.get().steps.addAll(Arrays.asList(0.55F, 0.1F, THIRD));
        file.save();
        String written = text();

        file.reload();

        assertEquals(0.55F, file.get().backgroundOpacity);
        assertEquals(0.1F, file.get().tenth);
        assertEquals(THIRD, file.get().third);
        assertEquals(0.55F, file.get().slots.get("first").volume);
        assertEquals(Arrays.asList(0.55F, 0.1F, THIRD), file.get().steps);

        file.save();

        assertEquals(written, text());
    }

    private ConfigFile<Settings> open() {
        return new ConfigServiceImpl(new ConfigPaths(configDirectory), LOG).open(
            ConfigSpec.of(MODID, NAME, Settings.class)
                .role(ConfigRoles.CORE)
                .build());
    }

    private String value(String key) throws IOException {
        Matcher line = Pattern.compile("^" + Pattern.quote(key) + " = (.+)$", Pattern.MULTILINE)
            .matcher(text());
        assertTrue(line.find(), text());
        return line.group(1);
    }

    private Path path() {
        return configDirectory.resolve("code")
            .resolve("core")
            .resolve(FILE);
    }

    private String text() throws IOException {
        return new String(Files.readAllBytes(path()), StandardCharsets.UTF_8);
    }

    public static final class Settings {

        public float backgroundOpacity = 0.55F;

        public float tenth = 0.1F;

        public float third = THIRD;

        public float whole = 1.0F;

        public double precise = 0.1;

        public int count = 7;

        public long huge = 9000000000L;

        public List<Float> steps = new ArrayList<>();

        public Map<String, Slot> slots = new LinkedHashMap<>();
    }

    public static final class Slot {

        public float volume = 0.55F;
    }
}
