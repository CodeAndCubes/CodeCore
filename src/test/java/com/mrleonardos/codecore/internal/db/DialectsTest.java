package com.mrleonardos.codecore.internal.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DialectsTest {

    @Test
    @DisplayName("движок берётся из схемы адреса, а mysql считается mariadb")
    void theSchemeDecidesTheDialect() {
        assertEquals(Dialects.MARIADB, Dialects.of("jdbc:mariadb://10.0.0.5:3306/mymods"));
        assertEquals(Dialects.MARIADB, Dialects.of("JDBC:MySQL://localhost/mymods"));
        assertEquals(Dialects.SQLITE, Dialects.of("jdbc:sqlite:world/codecore/logs.db"));
    }

    @Test
    @DisplayName("чужая схема остаётся без движка, и запись с ней не поднимется")
    void anotherSchemeIsRefused() {
        assertNull(Dialects.of("jdbc:postgresql://localhost/mymods"));
        assertNull(Dialects.of(""));
        assertNull(Dialects.of(null));
    }
}
