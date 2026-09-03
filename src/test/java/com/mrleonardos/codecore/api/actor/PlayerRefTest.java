package com.mrleonardos.codecore.api.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerRefTest {

    private static final UUID STEVE = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Test
    @DisplayName("ссылка помнит идентификатор и ник")
    void keepsBothFields() {
        PlayerRef ref = PlayerRef.of(STEVE, "Steve");

        assertEquals(STEVE, ref.id());
        assertEquals("Steve", ref.name());
    }

    @Test
    @DisplayName("переименовавшийся игрок остаётся тем же игроком")
    void equalityGoesByIdentifier() {
        PlayerRef before = PlayerRef.of(STEVE, "Steve");
        PlayerRef after = PlayerRef.of(STEVE, "Steve2");

        assertEquals(before, after);
        assertEquals(before.hashCode(), after.hashCode());

        Set<PlayerRef> recipients = new LinkedHashSet<>();
        recipients.add(before);
        recipients.add(after);
        assertEquals(1, recipients.size(), "один игрок не попадает в список получателей дважды");
    }

    @Test
    @DisplayName("разные игроки не равны")
    void differentPlayersDiffer() {
        assertNotEquals(PlayerRef.of(STEVE, "Steve"), PlayerRef.of(UUID.randomUUID(), "Steve"));
    }

    @Test
    @DisplayName("в печати видно и ник, и идентификатор")
    void printsNameAndIdentifier() {
        String text = PlayerRef.of(STEVE, "Steve")
            .toString();

        assertTrue(text.contains("Steve"), text);
        assertTrue(text.contains(STEVE.toString()), text);
    }

    @Test
    @DisplayName("без ника печатается один идентификатор")
    void printsIdentifierWhenNameIsEmpty() {
        assertEquals(
            STEVE.toString(),
            PlayerRef.of(STEVE, null)
                .toString());
    }
}
