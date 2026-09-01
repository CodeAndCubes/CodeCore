package com.mrleonardos.codecore.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DurationsTest {

    @Test
    void parsesPlainAndSuffixedValues() {
        assertEquals(45, Durations.toSeconds("45"));
        assertEquals(45, Durations.toSeconds("45s"));
        assertEquals(600, Durations.toSeconds("10m"));
        assertEquals(7200, Durations.toSeconds("2h"));
        assertEquals(86400, Durations.toSeconds("1d"));
    }

    @Test
    void parsesCombinedValues() {
        assertEquals(9000, Durations.toSeconds("2h30m"));
        assertEquals(90061, Durations.toSeconds("1d1h1m1s"));
    }

    @Test
    void rejectsNonsense() {
        assertEquals(-1, Durations.toSeconds(""));
        assertEquals(-1, Durations.toSeconds(null));
        assertEquals(-1, Durations.toSeconds("soon"));
        assertEquals(-1, Durations.toSeconds("10x"));
        assertEquals(-1, Durations.toSeconds("m10"));
    }

    @Test
    @DisplayName("срок за пределом int отвергается, а не превращается в маленький")
    void tooLargeValuesAreRefused() {
        assertEquals(-1, Durations.toSeconds("49712d"), "49712 суток переполняли int и давали 1d 17h");
        assertEquals(-1, Durations.toSeconds("99421d"));
        assertEquals(-1, Durations.toSeconds("4294967296"), "накопление цифр переполняло int и давало ноль");
        assertEquals(-1, Durations.toSeconds("99999999999"));
        assertEquals(-1, Durations.toSeconds("1000000h"));
        assertEquals(-1, Durations.toSeconds("2000000000s2000000000s"));
    }

    @Test
    @DisplayName("предельно допустимый срок ещё разбирается")
    void theLargestAllowedValueStillParses() {
        assertEquals(Integer.MAX_VALUE, Durations.toSeconds("2147483647"));
        assertEquals(24854 * 86400, Durations.toSeconds("24854d"));
    }

    @Test
    void formatsBackIntoReadableText() {
        assertEquals("0s", Durations.format(0));
        assertEquals("45s", Durations.format(45));
        assertEquals("10m", Durations.format(600));
        assertEquals("2h 30m", Durations.format(9000));
        assertEquals("1d 1h 1m 1s", Durations.format(90061));
    }
}
