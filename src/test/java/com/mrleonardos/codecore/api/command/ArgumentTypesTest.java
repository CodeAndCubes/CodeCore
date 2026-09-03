package com.mrleonardos.codecore.api.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Разбор аргумента проверяется без запуска игры.
 *
 * <p>
 * Раньше он бросал {@code net.minecraft.command.CommandException}, и такой тест грузил классы игры ради
 * одной проверки текста ошибки.
 */
class ArgumentTypesTest {

    @Test
    @DisplayName("не число: ключ и введённое доезжают до исключения")
    void notANumberCarriesTheInput() {
        CommandInputException failure = assertThrows(
            CommandInputException.class,
            () -> ArgumentTypes.integer()
                .parse("abc"));

        assertEquals(CommandMessages.NOT_A_NUMBER, failure.translationKey());
        assertArrayEquals(new Object[] { "abc" }, failure.arguments());
    }

    @Test
    @DisplayName("число вне границ называет обе границы")
    void outOfRangeCarriesBothBounds() {
        CommandInputException failure = assertThrows(
            CommandInputException.class,
            () -> ArgumentTypes.integer(1, 64)
                .parse("100"));

        assertEquals(CommandMessages.OUT_OF_RANGE, failure.translationKey());
        assertArrayEquals(new Object[] { "100", 1, 64 }, failure.arguments());
    }

    @Test
    @DisplayName("срок длиннее, чем считает int, не переполняется")
    void oversizedDurationIsRefused() {
        assertEquals(
            9000,
            (int) ArgumentTypes.duration()
                .parse("2h30m"));

        CommandInputException failure = assertThrows(
            CommandInputException.class,
            () -> ArgumentTypes.duration()
                .parse("49712d"));

        assertEquals(CommandMessages.INVALID_DURATION, failure.translationKey());
    }

    @Test
    @DisplayName("значение вне перечисления отклонено")
    void unknownEnumValueIsRefused() {
        assertEquals(
            SenderKind.RCON,
            ArgumentTypes.enumOf(SenderKind.class)
                .parse("rcon"));

        assertThrows(
            CommandInputException.class,
            () -> ArgumentTypes.enumOf(SenderKind.class)
                .parse("нет такого"));
    }

    @Test
    @DisplayName("перечисление подсказывает значения в нижнем регистре по новой подписи")
    void enumSuggestsItsValues() {
        assertEquals(
            Arrays.asList("console", "command_block"),
            ArgumentTypes.enumOf(SenderKind.class)
                .suggestions(FakeSender.console(), "co"));
    }
}
