package com.mrleonardos.codecore.api.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MigrationStepTest {

    @Test
    @DisplayName("шаг держит свою копию списка: правка исходного его не трогает")
    void theStepKeepsItsOwnCopy() {
        List<String> statements = new ArrayList<>();
        statements.add("create table a (id int)");
        MigrationStep step = MigrationStep.of(1, statements);

        statements.add("drop table a");

        assertEquals(
            1,
            step.statements()
                .size());
        assertThrows(
            UnsupportedOperationException.class,
            () -> step.statements()
                .add("drop table a"));
    }

    @Test
    @DisplayName("шаг без стейтментов и с неположительной версией не заводится")
    void anEmptyStepIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> MigrationStep.of(1));
        assertThrows(IllegalArgumentException.class, () -> MigrationStep.of(0, "select 1"));
        assertThrows(IllegalArgumentException.class, () -> MigrationStep.of(1, "select 1", "  "));
    }
}
