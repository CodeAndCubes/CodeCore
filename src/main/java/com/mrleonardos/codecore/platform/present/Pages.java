package com.mrleonardos.codecore.platform.present;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.event.ClickEvent;

import com.mrleonardos.codecore.platform.ServerTexts;

/**
 * Постраничник поверх любого списка строк.
 *
 * <p>
 * Шапка «Заголовок · всего N», строки страницы и футер с листанием: «‹ Назад 2/7 Далее ›», где
 * стрелки это команды по шаблону, например {@code /rg list %d}. Первая страница без «Назад», последняя
 * без «Далее», единственная без футера вовсе. Консоли и RCON достаются те же страницы плоским текстом:
 * кликов там нет, и вместо кнопок стоит строка положения.
 */
public final class Pages {

    /** Метка кнопки листания назад. */
    public static final String BACK = "codecore.present.pages.back";
    /** Метка кнопки листания вперёд. */
    public static final String NEXT = "codecore.present.pages.next";
    /** Строка положения: страница и их число. */
    public static final String PAGE = "codecore.present.pages.page";
    /** Хвост шапки с общим числом записей. */
    public static final String TOTAL = "codecore.present.pages.total";

    private static final int TURN_GAP = 3;
    private static final String BACK_OPENING = "‹ ";
    private static final String NEXT_CLOSING = " ›";
    private static final String PAGE_SLOT = "%d";

    private final List<RichLine> rows;
    private final int pageSize;
    private final PresentTheme theme;

    private Pages(List<RichLine> rows, int pageSize, PresentTheme theme) {
        this.rows = new ArrayList<>(rows);
        this.pageSize = pageSize;
        this.theme = theme;
    }

    /** Постраничник палитрой линейки. */
    public static Pages of(List<RichLine> rows, int pageSize) {
        return of(rows, pageSize, PresentTheme.LINEUP);
    }

    /** Постраничник своей палитрой. */
    public static Pages of(List<RichLine> rows, int pageSize, PresentTheme theme) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        return new Pages(rows, pageSize, theme);
    }

    /** Число страниц: ноль у пустого списка. */
    public int pageCount() {
        return (rows.size() + pageSize - 1) / pageSize;
    }

    /** Пуст ли список. */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    /**
     * Карточка страницы: шапка, строки и футер с листанием.
     *
     * <p>
     * Шаблон команды задаёт клик листания: номер страницы встаёт на место первого {@code %d}, остальные
     * знаки {@code %} шаблон не разбирает и не трогает. Шаблон без {@code %d} отклоняется сразу: кнопка
     * листания иначе молча повторила бы ту же команду.
     *
     * @throws IllegalArgumentException когда в шаблоне нет {@code %d}
     */
    public RichCard card(int number, String title, String commandTemplate) {
        int page = checked(number);
        RichCard card = RichCard.of(theme)
            .line(header(title));
        for (RichLine row : slice(page)) {
            card.line(row);
        }
        if (pageCount() > 1) {
            card.line(turns(page, commandTemplate));
        }
        return card;
    }

    /** Плоские строки страницы для консоли и RCON: те же страницы без кликов и скобок. */
    public List<String> plainLines(int number, String title) {
        int page = checked(number);
        List<String> whole = new ArrayList<>();
        whole.add(title + " · " + ServerTexts.format(TOTAL, rows.size()));
        for (RichLine row : slice(page)) {
            whole.add(row.plain());
        }
        if (pageCount() > 1) {
            whole.add(ServerTexts.format(PAGE, page, pageCount()));
        }
        return whole;
    }

    private RichLine header(String title) {
        return RichLine.of()
            .title(title)
            .muted(" · ")
            .label(TOTAL, rows.size());
    }

    private RichLine turns(int number, String commandTemplate) {
        int total = pageCount();
        RichLine line = RichLine.of();
        if (number > 1) {
            line.framed(
                BACK_OPENING,
                ServerTexts.format(BACK),
                "",
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, page(commandTemplate, number - 1)))
                .hover(PAGE, number - 1, total)
                .spacing(TURN_GAP);
        }
        line.muted(number + "/" + total);
        if (number < total) {
            line.spacing(TURN_GAP)
                .framed(
                    "",
                    ServerTexts.format(NEXT),
                    NEXT_CLOSING,
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, page(commandTemplate, number + 1)))
                .hover(PAGE, number + 1, total);
        }
        return line;
    }

    private List<RichLine> slice(int page) {
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, rows.size());
        return rows.subList(from, to);
    }

    private int checked(int number) {
        int total = pageCount();
        return total <= 1 ? 1 : Math.max(1, Math.min(number, total));
    }

    private static String page(String commandTemplate, int number) {
        int slot = commandTemplate.indexOf(PAGE_SLOT);
        if (slot < 0) {
            throw new IllegalArgumentException("Page command template needs %d for the page number");
        }
        return commandTemplate.substring(0, slot) + number + commandTemplate.substring(slot + PAGE_SLOT.length());
    }
}
