package com.mrleonardos.codecore.internal.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActionBarOverlayTest {

    @Test
    @DisplayName("срок из пакета зажимается в границы от секунды до минуты")
    void secondsFromThePacketAreClamped() {
        assertEquals(1, ActionBarOverlay.boundedSeconds(-5));
        assertEquals(1, ActionBarOverlay.boundedSeconds(0));
        assertEquals(1, ActionBarOverlay.boundedSeconds(1));
        assertEquals(30, ActionBarOverlay.boundedSeconds(30));
        assertEquals(60, ActionBarOverlay.boundedSeconds(60));
        assertEquals(60, ActionBarOverlay.boundedSeconds(61));
        assertEquals(60, ActionBarOverlay.boundedSeconds(Integer.MAX_VALUE));
    }
}
