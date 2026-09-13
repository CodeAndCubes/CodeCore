package com.mrleonardos.codecore.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActionBarLineTest {

    @Test
    @DisplayName("без своего окна повтор равен сроку показа")
    void repeatFollowsTheShownTime() {
        ActionBarLine line = ActionBarLine.of("deny", "Тут нельзя")
            .forSeconds(5);

        assertEquals(5, line.seconds());
        assertEquals(5, line.repeatSeconds());
    }

    @Test
    @DisplayName("своё окно повтора не двигается сроком показа")
    void ownRepeatSurvivesTheShownTime() {
        ActionBarLine line = ActionBarLine.of("deny", "Тут нельзя")
            .repeatAfter(9)
            .forSeconds(2);

        assertEquals(2, line.seconds());
        assertEquals(9, line.repeatSeconds());
    }

    @Test
    @DisplayName("заводской срок три секунды")
    void defaultsAreFilled() {
        ActionBarLine line = ActionBarLine.of("limit", "Больше нельзя");

        assertEquals(ActionBarLine.DEFAULT_SECONDS, line.seconds());
        assertEquals(ActionBarLine.DEFAULT_SECONDS, line.repeatSeconds());
        assertEquals("limit", line.key());
        assertEquals("Больше нельзя", line.text());
    }

    @Test
    @DisplayName("строка без ключа и без текста не собирается")
    void emptyPartsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ActionBarLine.of("", "Тут нельзя"));
        assertThrows(IllegalArgumentException.class, () -> ActionBarLine.of(null, "Тут нельзя"));
        assertThrows(IllegalArgumentException.class, () -> ActionBarLine.of("deny", ""));
        assertThrows(IllegalArgumentException.class, () -> ActionBarLine.of("deny", null));
    }

    @Test
    @DisplayName("ноль секунд это ошибка, а не вечная строка")
    void zeroSecondsAreRejected() {
        ActionBarLine line = ActionBarLine.of("deny", "Тут нельзя");

        assertThrows(IllegalArgumentException.class, () -> line.forSeconds(0));
        assertThrows(IllegalArgumentException.class, () -> line.repeatAfter(-1));
    }
}
