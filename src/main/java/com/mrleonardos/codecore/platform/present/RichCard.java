package com.mrleonardos.codecore.platform.present;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.IChatComponent;

import com.mrleonardos.codecore.platform.ServerTexts;

/**
 * Многострочная карточка: не жёсткая форма, а список строк с помощниками для типовых блоков.
 *
 * <p>
 * Заголовок, поля «метка: значение», черта и кнопки собираются из тех же кирпичей, что и одиночная
 * строка, поэтому компактный ответ это одна строка, развёрнутый это карточка, и набор кирпичей один.
 * Кнопки копятся в общей строке внизу и разделяются промежутком.
 */
public final class RichCard {

    private static final int BUTTON_GAP = 3;

    private final List<RichLine> lines = new ArrayList<>();
    private final PresentTheme theme;
    private RichLine actions;

    private RichCard(PresentTheme theme) {
        this.theme = theme;
    }

    /** Пустая карточка своей палитрой: строки дописываются вручную. */
    public static RichCard of(PresentTheme theme) {
        return new RichCard(theme);
    }

    /** Карточка с заголовком палитрой линейки: имя сущности цветом акцента. */
    public static RichCard of(String title) {
        return of(PresentTheme.LINEUP).heading(title);
    }

    /** Карточка с заголовком своей палитрой. */
    public static RichCard of(String title, PresentTheme theme) {
        return of(theme).heading(title);
    }

    /** Заголовок карточки: имя сущности цветом акцента, жирным. */
    public RichCard heading(String title) {
        return line(
            RichLine.of()
                .accent(title)
                .bold());
    }

    /** Произвольная строка: всё, для чего типового блока нет. */
    public RichCard line(RichLine line) {
        lines.add(line);
        return this;
    }

    /** Поле «метка: значение» с общим выравниванием цветов. */
    public RichCard field(String labelKey, String value) {
        return line(
            RichLine.of()
                .label(labelKey)
                .muted(": ")
                .value(value));
    }

    /** Горизонтальная черта между блоками. */
    public RichCard separator() {
        return line(
            RichLine.of()
                .separator());
    }

    /** Кнопка в футере: переведённая метка, клик исполняет команду. */
    public RichCard button(String labelKey, String command) {
        return action(labelKey, command, null, true);
    }

    /** Кнопка в футере с подсказкой по наведению: переведённая метка и переведённая подсказка. */
    public RichCard button(String labelKey, String command, String hoverKey) {
        return action(labelKey, command, hoverKey, true);
    }

    /** Кнопка в футере: клик подставляет команду в строку ввода. */
    public RichCard suggestion(String labelKey, String command) {
        return action(labelKey, command, null, false);
    }

    /** Кнопка в футере с подсказкой по наведению: клик подставляет команду, наведение переводит ключ. */
    public RichCard suggestion(String labelKey, String command, String hoverKey) {
        return action(labelKey, command, hoverKey, false);
    }

    /** Готовые строки: одна на строку чата. */
    public List<IChatComponent> build() {
        List<IChatComponent> whole = new ArrayList<>();
        for (RichLine line : fullLines()) {
            whole.add(line.build(theme));
        }
        return whole;
    }

    List<String> plainLines() {
        List<String> whole = new ArrayList<>();
        for (RichLine line : fullLines()) {
            whole.add(line.plain());
        }
        return whole;
    }

    private List<RichLine> fullLines() {
        List<RichLine> all = new ArrayList<>(lines);
        if (actions != null) {
            all.add(actions);
        }
        return all;
    }

    private RichCard action(String labelKey, String command, String hoverKey, boolean runs) {
        if (actions == null) {
            actions = RichLine.of();
        } else {
            actions.spacing(BUTTON_GAP);
        }
        if (runs) {
            actions.button(ServerTexts.format(labelKey), command);
        } else {
            actions.suggestion(ServerTexts.format(labelKey), command);
        }
        if (hoverKey != null) {
            actions.hover(hoverKey);
        }
        return this;
    }
}
