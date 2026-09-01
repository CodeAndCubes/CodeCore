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

import com.google.gson.JsonObject;
import com.mrleonardos.codecore.api.config.Migration;

class MigrationRunnerTest {

    private static final Logger LOG = LogManager.getLogger(MigrationRunnerTest.class);
    private static final String FILE = "codecore/test";

    @Test
    void runsChainInOrder() {
        JsonObject data = new JsonObject();
        data.addProperty(ConfigKeys.SCHEMA_VERSION, 1);
        data.addProperty("greeting", "hello");

        MigrationOutcome outcome = MigrationRunner.run(data, chain(), 3, FILE, LOG);

        assertEquals(MigrationOutcome.MIGRATED, outcome);
        assertEquals(
            3,
            data.get(ConfigKeys.SCHEMA_VERSION)
                .getAsInt());
        assertEquals(
            "hello",
            data.get("welcome")
                .getAsString(),
            "первый шаг переименовал поле");
        assertTrue(
            data.get("polite")
                .getAsBoolean(),
            "второй шаг добавил новое поле");
        assertFalse(data.has("greeting"));
    }

    @Test
    void fileWithoutVersionIsTreatedAsFirst() {
        JsonObject data = new JsonObject();
        data.addProperty("greeting", "hi");

        MigrationRunner.run(data, chain(), 2, FILE, LOG);

        assertEquals(
            2,
            data.get(ConfigKeys.SCHEMA_VERSION)
                .getAsInt());
        assertEquals(
            "hi",
            data.get("welcome")
                .getAsString());
    }

    @Test
    void currentVersionIsLeftAlone() {
        JsonObject data = new JsonObject();
        data.addProperty(ConfigKeys.SCHEMA_VERSION, 3);

        assertEquals(MigrationOutcome.UNCHANGED, MigrationRunner.run(data, chain(), 3, FILE, LOG));
    }

    @Test
    void newerFileIsNotTouched() {
        JsonObject data = new JsonObject();
        data.addProperty(ConfigKeys.SCHEMA_VERSION, 9);
        data.addProperty("fromTheFuture", true);

        assertEquals(MigrationOutcome.UNCHANGED, MigrationRunner.run(data, chain(), 3, FILE, LOG));
        assertEquals(
            9,
            data.get(ConfigKeys.SCHEMA_VERSION)
                .getAsInt(),
            "чужие настройки должны остаться целыми");
    }

    @Test
    void gapInChainStopsAtLastReachableVersion() {
        JsonObject data = new JsonObject();
        data.addProperty(ConfigKeys.SCHEMA_VERSION, 1);
        data.addProperty("greeting", "hey");

        List<Migration> withGap = Collections.singletonList(renameGreeting());

        assertEquals(MigrationOutcome.INCOMPLETE, MigrationRunner.run(data, withGap, 5, FILE, LOG));
        assertEquals(
            2,
            data.get(ConfigKeys.SCHEMA_VERSION)
                .getAsInt());
        assertEquals(
            "hey",
            data.get("welcome")
                .getAsString());
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
            public void apply(JsonObject data) {
                data.add("welcome", data.remove("greeting"));
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
            public void apply(JsonObject data) {
                data.addProperty("polite", true);
            }
        };
    }
}
