package com.mrleonardos.codecore.platform.present;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Карточка: произвольный список строк плюс помощники типовых блоков.
 *
 * <p>
 * Формы нет: заголовок, поля и кнопки лишь добавляют строки, а между ними конструктор волен ставить
 * свои. Проверяется сборка без сервера, перевод меток читается из ресурсов мода.
 */
class RichCardTest {

    @Test
    @DisplayName("заголовок это имя сущности цветом акцента, жирным")
    void headingUsesAccentColor() {
        IChatComponent heading = RichCard.of("дом \"Spawn\"")
            .build()
            .get(0);

        IChatComponent name = heading.getSiblings()
            .get(0);
        assertEquals("дом \"Spawn\"", name.getUnformattedText());
        assertEquals(
            EnumChatFormatting.GOLD,
            name.getChatStyle()
                .getColor());
        assertTrue(
            name.getChatStyle()
                .getBold());
    }

    @Test
    @DisplayName("акцент палитры подменяется модом")
    void accentIsReplaceable() {
        IChatComponent heading = RichCard.of("Регион spawn", PresentTheme.LINEUP.accent(EnumChatFormatting.GREEN))
            .build()
            .get(0);

        assertEquals(
            EnumChatFormatting.GREEN,
            heading.getSiblings()
                .get(0)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("поле выравнивает метку, двоеточие и значение по палитре")
    void fieldAlignsColors() {
        IChatComponent line = RichCard.of("Заголовок")
            .field("codecore.present.pages.back", "Steve")
            .build()
            .get(1);

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
            ": ",
            pieces.get(1)
                .getUnformattedText());
        assertEquals(
            EnumChatFormatting.DARK_GRAY,
            pieces.get(1)
                .getChatStyle()
                .getColor());
        assertEquals(
            "Steve",
            pieces.get(2)
                .getUnformattedText());
        assertEquals(
            EnumChatFormatting.WHITE,
            pieces.get(2)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("кнопки копятся одной строкой внизу и разделены промежутком")
    void buttonsShareOneLine() {
        RichCard card = RichCard.of("Заголовок")
            .field("codecore.present.pages.back", "Steve")
            .button("codecore.present.pages.next", "/rg list 2")
            .button("codecore.present.pages.back", "/rg list 1");

        List<IChatComponent> lines = card.build();
        assertEquals(3, lines.size());
        List<IChatComponent> footer = lines.get(2)
            .getSiblings();
        assertEquals("[Next]   [Back]", plain(footer));
        ClickEvent second = footer.get(5)
            .getChatStyle()
            .getChatClickEvent();
        assertEquals(ClickEvent.Action.RUN_COMMAND, second.getAction());
        assertEquals("/rg list 1", second.getValue());
    }

    @Test
    @DisplayName("кнопка-подсказка подставляет команду в строку ввода")
    void suggestionButtonSuggests() {
        List<IChatComponent> footer = RichCard.of("Заголовок")
            .suggestion("codecore.present.pages.next", "/rg info ")
            .build()
            .get(1)
            .getSiblings();

        assertEquals(
            ClickEvent.Action.SUGGEST_COMMAND,
            footer.get(1)
                .getChatStyle()
                .getChatClickEvent()
                .getAction());
    }

    @Test
    @DisplayName("кнопка с подсказкой несёт клик и подсказку целиком, со скобками")
    void hoverButtonCarriesBothEventsOnWholeButton() {
        List<IChatComponent> footer = RichCard.of("Заголовок")
            .button("codecore.present.pages.back", "/rg list 1", "codecore.present.pages.next")
            .suggestion("codecore.present.pages.next", "/rg info ", "codecore.present.pages.back")
            .build()
            .get(1)
            .getSiblings();

        assertEquals(7, footer.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(
                ClickEvent.Action.RUN_COMMAND,
                footer.get(i)
                    .getChatStyle()
                    .getChatClickEvent()
                    .getAction());
            assertEquals(
                "Next",
                footer.get(i)
                    .getChatStyle()
                    .getChatHoverEvent()
                    .getValue()
                    .getUnformattedText());
        }
        for (int i = 4; i < 7; i++) {
            assertEquals(
                ClickEvent.Action.SUGGEST_COMMAND,
                footer.get(i)
                    .getChatStyle()
                    .getChatClickEvent()
                    .getAction());
            assertEquals(
                "Back",
                footer.get(i)
                    .getChatStyle()
                    .getChatHoverEvent()
                    .getValue()
                    .getUnformattedText());
        }
    }

    @Test
    @DisplayName("черта стоит отдельной строкой")
    void separatorIsItsOwnLine() {
        List<IChatComponent> lines = RichCard.of("Заголовок")
            .field("codecore.present.pages.back", "Steve")
            .separator()
            .field("codecore.present.pages.next", "/warp shop")
            .build();

        assertEquals(4, lines.size());
        List<IChatComponent> rule = lines.get(2)
            .getSiblings();
        assertEquals(1, rule.size());
        assertEquals(
            EnumChatFormatting.DARK_GRAY,
            rule.get(0)
                .getChatStyle()
                .getColor());
        assertTrue(
            !rule.get(0)
                .getUnformattedText()
                .isEmpty());
    }

    @Test
    @DisplayName("плоские строки карточки это те же строки без стилей")
    void plainLinesMatchLines() {
        List<String> plain = RichCard.of("Заголовок")
            .field("codecore.present.pages.back", "Steve")
            .button("codecore.present.pages.next", "/rg list 2")
            .plainLines();

        assertEquals(3, plain.size());
        assertEquals("Заголовок", plain.get(0));
        assertEquals("Back: Steve", plain.get(1));
        assertEquals("[Next]", plain.get(2));
    }

    private static String plain(List<IChatComponent> pieces) {
        StringBuilder text = new StringBuilder();
        for (IChatComponent piece : pieces) {
            text.append(piece.getUnformattedText());
        }
        return text.toString();
    }
}
