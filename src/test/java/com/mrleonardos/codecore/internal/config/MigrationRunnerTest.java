package com.mrleonardos.codecore.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.config.ConfigData;
import com.mrleonardos.codecore.api.config.Migration;

class MigrationRunnerTest {

    private static final Logger LOG = LogManager.getLogger(MigrationRunnerTest.class);
    private static final String FILE = "permissions/core-groups.toml";

    @Test
    void runsChainInOrder() {
        ConfigData data = data();
        data.set(ConfigKeys.SCHEMA_VERSION, 1L);
        data.set("greeting", "hello");

        MigrationOutcome outcome = MigrationRunner.run(data, chain(), 3, FILE, LOG);

        assertEquals(MigrationOutcome.MIGRATED, outcome);
        assertEquals(3, data.integer(ConfigKeys.SCHEMA_VERSION, 0));
        assertEquals("hello", data.string("welcome", ""), "первый шаг переименовал ключ");
        assertTrue(data.flag("polite", false), "второй шаг добавил новый ключ");
        assertFalse(data.has("greeting"));
    }

    @Test
    void fileWithoutVersionIsTreatedAsFirst() {
        ConfigData data = data();
        data.set("greeting", "hi");

        MigrationRunner.run(data, chain(), 2, FILE, LOG);

        assertEquals(2, data.integer(ConfigKeys.SCHEMA_VERSION, 0));
        assertEquals("hi", data.string("welcome", ""));
    }

    @Test
    void currentVersionIsLeftAlone() {
        ConfigData data = data();
        data.set(ConfigKeys.SCHEMA_VERSION, 3L);

        assertEquals(MigrationOutcome.UNCHANGED, MigrationRunner.run(data, chain(), 3, FILE, LOG));
    }

    @Test
    void newerFileIsNotTouched() {
        ConfigData data = data();
        data.set(ConfigKeys.SCHEMA_VERSION, 9L);
        data.set("fromTheFuture", true);

        assertEquals(MigrationOutcome.UNCHANGED, MigrationRunner.run(data, chain(), 3, FILE, LOG));
        assertEquals(9, data.integer(ConfigKeys.SCHEMA_VERSION, 0), "чужие настройки должны остаться целыми");
    }

    @Test
    void gapInChainStopsAtLastReachableVersion() {
        ConfigData data = data();
        data.set(ConfigKeys.SCHEMA_VERSION, 1L);
        data.set("greeting", "hey");

        List<Migration> withGap = Collections.singletonList(renameGreeting());

        assertEquals(MigrationOutcome.INCOMPLETE, MigrationRunner.run(data, withGap, 5, FILE, LOG));
        assertEquals(2, data.integer(ConfigKeys.SCHEMA_VERSION, 0));
        assertEquals("hey", data.string("welcome", ""));
    }

    @Test
    void theSameChainRunsOnJsonData() {
        ConfigData data = JsonDocument.empty()
            .data();
        data.set(ConfigKeys.SCHEMA_VERSION, 1L);
        data.set("greeting", "привет");

        assertEquals(MigrationOutcome.MIGRATED, MigrationRunner.run(data, chain(), 3, FILE, LOG));
        assertEquals("привет", data.string("welcome", ""));
        assertTrue(data.flag("polite", false));
    }

    private static ConfigData data() {
        return TomlDocument.empty()
            .data();
    }

    private static List<Migration> chain() {
        return Arrays.asList(addPolite(), renameGreeting());
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
                data.set("welcome", data.get("greeting"));
                data.remove("greeting");
            }
        };
    }

    private static Migration addPolite() {
        return new Migration() {

            @Override
            public int from() {
                return 2;
            }

            @Override
            public int to() {
                return 3;
            }

            @Override
            public void apply(ConfigData data) {
                data.set("polite", true);
            }
        };
    }
}
