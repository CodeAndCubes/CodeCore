package com.mrleonardos.codecore.platform.present;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.internal.text.Texts;

/**
 * Нарезка списка страницами: границы, футер листания и подстановка номера в шаблон команды.
 *
 * <p>
 * Строками списка стоят обычные собранные строки, сервер не поднимается. Перевод служебных слов
 * читается из ресурсов мода, выбранный язык возвращается на место после каждой проверки.
 */
class PagesTest {

    @Test
    @DisplayName("страница нулевого размера это ошибка")
    void zeroPageSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> Pages.of(rows(1), 0));
        assertThrows(IllegalArgumentException.class, () -> Pages.of(rows(1), -2));
    }

    @Test
    @DisplayName("число страниц: пустой список, ровно на страницу и с хвостом")
    void pageCountCoversEdges() {
        assertEquals(
            0,
            Pages.of(rows(0), 3)
                .pageCount());
        assertEquals(
            1,
            Pages.of(rows(3), 3)
                .pageCount());
        assertEquals(
            2,
            Pages.of(rows(4), 3)
                .pageCount());
        assertEquals(
            3,
            Pages.of(rows(5), 2)
                .pageCount());
        assertTrue(
            Pages.of(rows(0), 3)
                .isEmpty());
        assertFalse(
            Pages.of(rows(1), 3)
                .isEmpty());
    }

    @Test
    @DisplayName("шапка несёт заголовок, точку и общее число записей")
    void headerCarriesTitleAndTotal() {
        IChatComponent header = Pages.of(rows(5), 2)
            .card(1, "Регионы", "/rg list %d")
            .build()
            .get(0);

        List<IChatComponent> pieces = header.getSiblings();
        assertEquals(3, pieces.size());
        assertEquals(
            "Регионы",
            pieces.get(0)
                .getUnformattedText());
        assertEquals(
            EnumChatFormatting.AQUA,
            pieces.get(0)
                .getChatStyle()
                .getColor());
        assertEquals(
            " · ",
            pieces.get(1)
                .getUnformattedText());
        assertEquals(
            "total 5",
            pieces.get(2)
                .getUnformattedText());
        assertEquals(
            EnumChatFormatting.GRAY,
            pieces.get(2)
                .getChatStyle()
                .getColor());
    }

    @Test
    @DisplayName("страница несёт только свои строки")
    void pageCarriesItsOwnRows() {
        List<IChatComponent> lines = Pages.of(rows(5), 2)
            .card(1, "Регионы", "/rg list %d")
            .build();

        assertEquals(4, lines.size());
        assertEquals(
            "row 1",
            lines.get(1)
                .getUnformattedText());
        assertEquals(
            "row 2",
            lines.get(2)
                .getUnformattedText());

        List<IChatComponent> second = Pages.of(rows(5), 2)
            .card(2, "Регионы", "/rg list %d")
            .build();
        assertEquals(
            "row 3",
            second.get(1)
                .getUnformattedText());
        assertEquals(
            "row 4",
            second.get(2)
                .getUnformattedText());
    }

    @Test
    @DisplayName("первая страница без «Назад»")
    void firstPageHasNoBack() {
        List<IChatComponent> footer = footer(5, 2, 1);

        assertEquals("1/3   Next ›", plain(footer));
        assertFalse(hasClick(footer, "/rg list 1"));
        assertTrue(hasClick(footer, "/rg list 2"));
    }

    @Test
    @DisplayName("средняя страница листается в обе стороны")
    void middlePageTurnsBothWays() {
        List<IChatComponent> footer = footer(5, 2, 2);

        assertEquals("‹ Back   2/3   Next ›", plain(footer));
        assertTrue(hasClick(footer, "/rg list 1"));
        assertTrue(hasClick(footer, "/rg list 3"));
    }

    @Test
    @DisplayName("последняя страница без «Далее»")
    void lastPageHasNoNext() {
        List<IChatComponent> footer = footer(5, 2, 3);

        assertEquals("‹ Back   3/3", plain(footer));
        assertTrue(hasClick(footer, "/rg list 2"));
        assertFalse(hasClick(footer, "/rg list 4"));
    }

    @Test
    @DisplayName("подсказка кнопки называет страницу, куда она ведёт")
    void turnHoverNamesTargetPage() {
        List<IChatComponent> footer = footer(5, 2, 2);

        for (IChatComponent piece : footer) {
            if (piece.getChatStyle()
                .getChatClickEvent() != null) {
                assertTrue(
                    piece.getChatStyle()
                        .getChatHoverEvent() != null);
            }
        }
        assertEquals(
            "Page 1 of 3",
            footer.get(0)
                .getChatStyle()
                .getChatHoverEvent()
                .getValue()
                .getUnformattedText());
        assertEquals(
            "Page 3 of 3",
            footer.get(5)
                .getChatStyle()
                .getChatHoverEvent()
                .getValue()
                .getUnformattedText());
    }

    @Test
    @DisplayName("единственная страница без футера, у списка с хвостом футер есть")
    void singlePageHasNoFooter() {
        assertEquals(
            3,
            Pages.of(rows(2), 2)
                .card(1, "Регионы", "/rg list %d")
                .build()
                .size());
        assertEquals(
            4,
            Pages.of(rows(3), 3)
                .card(1, "Регионы", "/rg list %d")
                .build()
                .size());
        assertEquals(
            5,
            Pages.of(rows(4), 3)
                .card(1, "Регионы", "/rg list %d")
                .build()
                .size());
    }

    @Test
    @DisplayName("пустой список это одна шапка")
    void emptyListIsHeaderOnly() {
        Pages pages = Pages.of(rows(0), 3);

        assertEquals(
            1,
            pages.card(1, "Регионы", "/rg list %d")
                .build()
                .size());
        assertEquals(
            1,
            pages.plainLines(1, "Регионы")
                .size());
    }

    @Test
    @DisplayName("номер за границами прижимается к ближайшей странице")
    void outOfRangeNumberClamps() {
        Pages pages = Pages.of(rows(5), 2);
        List<IChatComponent> tooFar = pages.card(99, "Регионы", "/rg list %d")
            .build();
        List<IChatComponent> beforeFirst = pages.card(0, "Регионы", "/rg list %d")
            .build();

        assertEquals(
            "‹ Back   3/3",
            plain(
                tooFar.get(tooFar.size() - 1)
                    .getSiblings()));
        assertEquals(
            "1/3   Next ›",
            plain(
                beforeFirst.get(beforeFirst.size() - 1)
                    .getSiblings()));
    }

    @Test
    @DisplayName("шаблон команды подставляет номер страницы")
    void commandTemplateSubstitutesNumber() {
        assertTrue(hasClick(footer(7, 3, 2), "/rg list 1"));
        assertTrue(hasClick(footer(7, 3, 2), "/rg list 3"));
    }

    @Test
    @DisplayName("шаблон без разъёма под номер отклоняется сразу")
    void templateWithoutSlotThrows() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Pages.of(rows(5), 2)
                .card(2, "Регионы", "/rg list"));
    }

    @Test
    @DisplayName("чужие знаки процента шаблон не разбирает и не роняет")
    void templateLeavesForeignPercentSigns() {
        List<IChatComponent> footer = Pages.of(rows(5), 2)
            .card(2, "Регионы", "/rg list %d sort=%%")
            .build()
            .get(3)
            .getSiblings();

        assertTrue(hasClick(footer, "/rg list 1 sort=%%"));
    }

    @Test
    @DisplayName("консоль получает те же страницы плоским текстом")
    void plainLinesCarrySamePages() {
        Pages pages = Pages.of(rows(5), 2);
        List<String> plain = pages.plainLines(2, "Регионы");

        assertEquals(4, plain.size());
        assertEquals("Регионы · total 5", plain.get(0));
        assertEquals("row 3", plain.get(1));
        assertEquals("row 4", plain.get(2));
        assertEquals("Page 2 of 3", plain.get(3));
    }

    @Test
    @DisplayName("служебные слова переводятся выбранным языком линейки")
    void serviceWordsFollowChosenLanguage() {
        Texts.language("ru_RU");
        try {
            Pages pages = Pages.of(rows(5), 2);
            List<IChatComponent> footer = pages.card(2, "Регионы", "/rg list %d")
                .build()
                .get(3)
                .getSiblings();

            assertEquals("‹ Назад   2/3   Далее ›", plain(footer));
            assertEquals(
                "Регионы · всего 5",
                pages.plainLines(1, "Регионы")
                    .get(0));
            assertEquals(
                "Страница 2 из 3",
                pages.plainLines(2, "Регионы")
                    .get(3));
        } finally {
            Texts.language(Texts.DEFAULT_LANGUAGE);
        }
    }

    private static List<IChatComponent> footer(int totalRows, int pageSize, int number) {
        List<IChatComponent> lines = Pages.of(rows(totalRows), pageSize)
            .card(number, "Регионы", "/rg list %d")
            .build();
        return lines.get(lines.size() - 1)
            .getSiblings();
    }

    private static List<RichLine> rows(int count) {
        List<RichLine> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            rows.add(
                RichLine.of()
                    .value("row " + i));
        }
        return rows;
    }

    private static String plain(List<IChatComponent> pieces) {
        StringBuilder text = new StringBuilder();
        for (IChatComponent piece : pieces) {
            text.append(piece.getUnformattedText());
        }
        return text.toString();
    }

    private static boolean hasClick(List<IChatComponent> pieces, String command) {
        for (IChatComponent piece : pieces) {
            ClickEvent click = piece.getChatStyle()
                .getChatClickEvent();
            if (click != null && command.equals(click.getValue())) {
                return true;
            }
        }
        return false;
    }
}
