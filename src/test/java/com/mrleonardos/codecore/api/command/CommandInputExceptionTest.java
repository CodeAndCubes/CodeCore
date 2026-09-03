package com.mrleonardos.codecore.api.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandInputExceptionTest {

    @Test
    @DisplayName("ключ перевода и аргументы доезжают до сообщения")
    void keyAndArgumentsReachTheMessage() {
        CommandInputException failure = new CommandInputException(CommandMessages.OUT_OF_RANGE, "100", 1, 64);

        assertEquals(CommandMessages.OUT_OF_RANGE, failure.translationKey());
        assertArrayEquals(new Object[] { "100", 1, 64 }, failure.arguments());
        assertTrue(
            failure.getMessage()
                .contains(CommandMessages.OUT_OF_RANGE),
            failure.getMessage());
        assertTrue(
            failure.getMessage()
                .contains("100"),
            failure.getMessage());
        assertTrue(
            failure.getMessage()
                .contains("64"),
            failure.getMessage());
    }

    @Test
    @DisplayName("без аргументов сообщение это сам ключ")
    void keyAloneIsTheWholeMessage() {
        CommandInputException failure = new CommandInputException(CommandMessages.NO_PERMISSION);

        assertEquals(CommandMessages.NO_PERMISSION, failure.getMessage());
        assertEquals(0, failure.arguments().length);
    }

    @Test
    @DisplayName("массив аргументов наружу отдаётся копией")
    void argumentsAreCopiedOut() {
        CommandInputException failure = new CommandInputException(CommandMessages.NOT_A_NUMBER, "abc");

        failure.arguments()[0] = "подменили";

        assertArrayEquals(new Object[] { "abc" }, failure.arguments());
    }
}
