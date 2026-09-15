package com.mrleonardos.codecore.platform.present;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.internal.text.Texts;

/**
 * Сборка одной строки: у каждого куска свой цвет и свой стиль с кликом.
 *
 * <p>
 * Сервер здесь не нужен: куски строки это обычные классы игры, а переводы читаются из ресурсов мода.
 */
class RichLineTest {

    @Test
    @DisplayName("куски несут цвет своей роли из палитры")
    void piecesCarryThemeColors() {
        IChatComponent line = RichLine.of()
            .label("codecore.present.pages.back")
            .muted(": ")
            .value("Steve")
            .build();

        List<IChatComponent> pieces = line.getSiblings();
        assertEquals(3, pieces.size());
        assertEquals(
            "Back",
            pieces.get(0)
                .getUnformattedText());
        assertEquals(
            EnumChatFormatting.GRAY,
            pieces.get(0)
                .getChatStyle()
                .getColor());
        assertEquals(
            EnumChatFormatting.DARK_GRAY,
            pieces.get(1)
                .getChatStyle()
                .getColor());
        assertEquals(
            EnumChatFormatting.WHITE,
            pieces.get(2)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("явный цвет на куске сильнее палитры")
    void explicitColorBeatsTheme() {
        IChatComponent line = RichLine.of()
            .text("слово", EnumChatFormatting.RED)
            .build();

        assertEquals(
            EnumChatFormatting.RED,
            line.getSiblings()
                .get(0)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("палитра переопределяется точечно, заменой одного цвета")
    void themeColorIsReplaceable() {
        IChatComponent line = RichLine.of()
            .label("codecore.present.pages.back")
            .build(PresentTheme.LINEUP.label(EnumChatFormatting.BLUE));

        assertEquals(
            EnumChatFormatting.BLUE,
            line.getSiblings()
                .get(0)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("текст без цвета остаётся без цвета")
    void plainTextStaysUncolored() {
        IChatComponent line = RichLine.of()
            .text("просто текст")
            .build();

        assertNull(
            line.getSiblings()
                .get(0)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("клик последнего куска исполняет команду")
    void runCommandClicksLastPiece() {
        IChatComponent line = RichLine.of()
            .value("Телепорт")
            .run("/home spawn")
            .build();

        ClickEvent click = line.getSiblings()
            .get(0)
            .getChatStyle()
            .getChatClickEvent();
        assertEquals(ClickEvent.Action.RUN_COMMAND, click.getAction());
        assertEquals("/home spawn", click.getValue());
    }

    @Test
    @DisplayName("клик может подставлять команду в строку ввода")
    void suggestCommandClicksLastPiece() {
        ClickEvent click = RichLine.of()
            .value("Шаблон")
            .suggest("/pay ")
            .build()
            .getSiblings()
            .get(0)
            .getChatStyle()
            .getChatClickEvent();

        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, click.getAction());
        assertEquals("/pay ", click.getValue());
    }

    @Test
    @DisplayName("наведение показывает переведенный готовый текст")
    void hoverShowsReadyText() {
        IChatComponent line = RichLine.of()
            .accent("Spawn")
            .hover("codecore.present.pages.page", 2, 7)
            .build();

        assertEquals(
            "Page 2 of 7",
            line.getSiblings()
                .get(0)
                .getChatStyle()
                .getChatHoverEvent()
                .getValue()
                .getUnformattedText());
    }

    @Test
    @DisplayName("кнопка получает клик целиком, вместе со скобками")
    void buttonClicksSpanBrackets() {
        IChatComponent line = RichLine.of()
            .button("Back", "/rg list 1")
            .build();

        List<IChatComponent> pieces = line.getSiblings();
        assertEquals(3, pieces.size());
        for (IChatComponent piece : pieces) {
            assertEquals(
                ClickEvent.Action.RUN_COMMAND,
                piece.getChatStyle()
                    .getChatClickEvent()
                    .getAction());
        }
        assertEquals(
            "/rg list 1",
            pieces.get(1)
                .getChatStyle()
                .getChatClickEvent()
                .getValue());
        assertEquals(
            EnumChatFormatting.DARK_GRAY,
            pieces.get(0)
                .getChatStyle()
                .getColor());
        assertEquals(
            EnumChatFormatting.YELLOW,
            pieces.get(1)
                .getChatStyle()
                .getColor());
        assertEquals("[Back]", joinPlain(pieces));
    }

    @Test
    @DisplayName("подсказка после кнопки ложится на всю кнопку")
    void hoverAfterButtonCoversBrackets() {
        IChatComponent line = RichLine.of()
            .button("Back", "/rg list 1")
            .hover("codecore.present.pages.page", 1, 7)
            .build();

        for (IChatComponent piece : line.getSiblings()) {
            assertEquals(
                "Page 1 of 7",
                piece.getChatStyle()
                    .getChatHoverEvent()
                    .getValue()
                    .getUnformattedText());
        }
    }

    @Test
    @DisplayName("жирный и курсив ложатся на последний кусок")
    void boldAndItalicLandOnLastPiece() {
        IChatComponent line = RichLine.of()
            .value("первый")
            .value("второй")
            .bold()
            .italic()
            .build();

        List<IChatComponent> pieces = line.getSiblings();
        assertFalse(
            pieces.get(0)
                .getChatStyle()
                .getBold());
        assertTrue(
            pieces.get(1)
                .getChatStyle()
                .getBold());
        assertTrue(
            pieces.get(1)
                .getChatStyle()
                .getItalic());
    }

    @Test
    @DisplayName("стиль без куска это ошибка, а не молчаливая потеря")
    void stylingWithoutPieceThrows() {
        assertThrows(
            IllegalStateException.class,
            () -> RichLine.of()
                .run("/home"));
        assertThrows(
            IllegalStateException.class,
            () -> RichLine.of()
                .hover("codecore.present.pages.page", 1, 2));
    }

    @Test
    @DisplayName("плоский текст склеивает куски без кодов цвета")
    void plainJoinsPieces() {
        String plain = RichLine.of()
            .label("codecore.present.pages.back")
            .muted(": ")
            .value("Steve")
            .plain();

        assertEquals("Back: Steve", plain);
    }

    @Test
    @DisplayName("пустая строка собирается пустой")
    void emptyLineBuildsEmpty() {
        IChatComponent line = RichLine.of()
            .build();

        assertTrue(
            line.getSiblings()
                .isEmpty());
        assertEquals("", line.getUnformattedText());
    }

    @Test
    @DisplayName("метки переводятся выбранным языком линейки")
    void labelsFollowChosenLanguage() {
        Texts.language("ru_RU");
        try {
            assertEquals(
                "Назад",
                RichLine.of()
                    .label("codecore.present.pages.back")
                    .plain());
        } finally {
            Texts.language(Texts.DEFAULT_LANGUAGE);
        }
    }

    private static String joinPlain(List<IChatComponent> pieces) {
        StringBuilder plain = new StringBuilder();
        for (IChatComponent piece : pieces) {
            plain.append(piece.getUnformattedText());
        }
        return plain.toString();
    }
}
