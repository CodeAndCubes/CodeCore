package com.mrleonardos.codecore.internal.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.actor.PlayerRef;
import com.mrleonardos.codecore.api.service.ActionBarLine;

class ActionBarImplTest {

    private static final PlayerRef ALICE = PlayerRef.of(UUID.randomUUID(), "Alice");
    private static final PlayerRef BOB = PlayerRef.of(UUID.randomUUID(), "Bob");
    private static final int MIN_WINDOW = 3;

    private final List<String> sent = new ArrayList<>();
    private long now = 1_000_000L;

    @Test
    @DisplayName("сорок отказов подряд превращаются в один показ")
    void repeatsCollapseIntoOne() {
        ActionBarImpl service = service();

        for (int attempt = 0; attempt < 40; attempt++) {
            service.show(ALICE, ActionBarLine.of("deny", "Тут нельзя"));
            now += 250L;
        }

        assertEquals(4, sent.size());
    }

    @Test
    @DisplayName("окно меньше настроенного подтягивается к настроенному")
    void tooSmallWindowGrowsToTheConfigured() {
        ActionBarImpl service = service();

        service.show(
            ALICE,
            ActionBarLine.of("deny", "Тут нельзя")
                .repeatAfter(1));
        now += 1_500L;
        service.show(
            ALICE,
            ActionBarLine.of("deny", "Тут нельзя")
                .repeatAfter(1));

        assertEquals(1, sent.size());

        now += 2_000L;
        service.show(
            ALICE,
            ActionBarLine.of("deny", "Тут нельзя")
                .repeatAfter(1));

        assertEquals(2, sent.size());
    }

    @Test
    @DisplayName("окно больше настроенного остаётся своим")
    void biggerWindowStaysAsAsked() {
        ActionBarImpl service = service();

        service.show(
            ALICE,
            ActionBarLine.of("deny", "Тут нельзя")
                .repeatAfter(30));
        now += 10_000L;
        service.show(
            ALICE,
            ActionBarLine.of("deny", "Тут нельзя")
                .repeatAfter(30));

        assertEquals(1, sent.size());
    }

    @Test
    @DisplayName("вторая причина показывается сразу за первой")
    void anotherKeyGoesThroughAtOnce() {
        ActionBarImpl service = service();

        service.show(ALICE, ActionBarLine.of("deny", "Тут нельзя"));
        service.show(ALICE, ActionBarLine.of("limit", "Больше нельзя"));

        assertEquals(2, sent.size());
        assertEquals("Alice: Больше нельзя", sent.get(1));
    }

    @Test
    @DisplayName("тишина у одного игрока не глушит другого")
    void playersAreCountedApart() {
        ActionBarImpl service = service();

        service.show(ALICE, ActionBarLine.of("deny", "Тут нельзя"));
        service.show(BOB, ActionBarLine.of("deny", "Тут нельзя"));

        assertEquals(2, sent.size());
    }

    @Test
    @DisplayName("память не растёт от десяти тысяч разных ключей")
    void memoryStaysBounded() {
        ActionBarImpl service = service();

        for (int attempt = 0; attempt < 10_000; attempt++) {
            service.show(ALICE, ActionBarLine.of("key" + attempt, "Тут нельзя"));
            now += 100L;
        }

        assertEquals(10_000, sent.size());
        assertTrue(service.remembered() < 300, "осталось записей: " + service.remembered());
    }

    private ActionBarImpl service() {
        return new ActionBarImpl(
            () -> MIN_WINDOW,
            () -> now,
            (player, text, seconds) -> sent.add(player.name() + ": " + text));
    }
}
