package com.mrleonardos.codecore.api.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Подпись автора правки собирается из вида отправителя и его позиции.
 *
 * <p>
 * Тест намеренно не грузит ни одного класса игры: ровно это и должно стать возможным после переезда с
 * {@code ICommandSender}. Раньше такую подпись собирали через {@code CommandBlockLogic},
 * {@code RConConsoleSource} и {@code ChunkCoordinates}.
 */
class CommandSenderTest {

    private static final UUID STEVE = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Test
    @DisplayName("командный блок подписывается координатами")
    void commandBlockSignsWithItsPosition() {
        assertEquals("commandblock@100,64,-30", actorOf(FakeSender.commandBlock(-1, 100, 64, -30)));
    }

    @Test
    @DisplayName("консоль подписывается словом, позиции у неё нет")
    void consoleHasNoPosition() {
        CommandSender sender = FakeSender.console();

        assertEquals("console", actorOf(sender));
        assertFalse(
            sender.position()
                .isPresent());
        assertFalse(
            sender.player()
                .isPresent());
    }

    @Test
    @DisplayName("игрок подписывается ником, ссылка на него на месте")
    void playerSignsWithItsName() {
        CommandSender sender = FakeSender.player(STEVE, "Steve");

        assertEquals("Steve", actorOf(sender));
        assertEquals(
            STEVE,
            sender.player()
                .get()
                .id());
        assertTrue(
            sender.position()
                .isPresent());
    }

    @Test
    @DisplayName("RCON отличим от консоли, хотя позиции нет у обоих")
    void rconIsNotTheConsole() {
        CommandSender sender = FakeSender.rcon();

        assertEquals("rcon", actorOf(sender));
        assertFalse(
            sender.position()
                .isPresent());
    }

    @Test
    @DisplayName("измерение отправителя видно отдельно от координат")
    void dimensionTravelsWithThePosition() {
        SenderPosition position = FakeSender.commandBlock(-1, 100, 64, -30)
            .position()
            .get();

        assertEquals(-1, position.dimension());
        assertEquals(new SenderPosition(-1, 100, 64, -30), position);
    }

    @Test
    @DisplayName("ответы уходят ключами перевода")
    void repliesGoAsTranslationKeys() {
        FakeSender sender = FakeSender.console();

        sender.reply(CommandMessages.USAGE, "/eco");
        sender.replyError(CommandMessages.NO_PERMISSION);

        assertEquals(Arrays.asList(CommandMessages.USAGE, "error " + CommandMessages.NO_PERMISSION), sender.said);
    }

    /**
     * Ровно то, что делает {@code SenderSubjects.actorOf} у соседних модов.
     *
     * <p>
     * Координаты берутся из {@code x()}, {@code y()} и {@code z()}, а не из {@code toString()}: подпись
     * уходит в журнал на диск и обязана пережить любую правку отладочной записи.
     */
    private static String actorOf(CommandSender sender) {
        switch (sender.kind()) {
            case PLAYER:
                return sender.name();
            case COMMAND_BLOCK:
                return "commandblock@" + sender.position()
                    .map(at -> at.x() + "," + at.y() + "," + at.z())
                    .orElse("?");
            case RCON:
                return "rcon";
            default:
                return "console";
        }
    }
}
