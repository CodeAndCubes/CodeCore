package com.mrleonardos.codecore.platform.present;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.mrleonardos.codecore.platform.ServerTexts;

/**
 * Одна строка чата из цветных кусков.
 *
 * <p>
 * Кирпичи дописывают куски слева направо: метка, значение, заметное слово, приглушённое дополнение,
 * кнопка. Цвет куска берётся из палитры, а стиль с кликом и подсказкой ставится на последний
 * дописанный кусок, поэтому кнопка получает клик целиком, вместе со скобками.
 *
 * <p>
 * Клик и подсказка работают на ванильном клиенте 1.7.10 без клиентских модов: строка собирается из
 * {@link IChatComponent} со своими {@link ChatStyle} на каждом куске, а переводы меток и подсказок
 * сервер подставляет готовым текстом через {@link ServerTexts}.
 */
public final class RichLine {

    private static final String DIVIDER = "────────────────────";
    private static final String BUTTON_OPENING = "[";
    private static final String BUTTON_CLOSING = "]";

    private final List<Segment> segments = new ArrayList<>();
    private int pieceStart;

    private RichLine() {}

    /** Пустая строка: кирпичи дописываются цепочкой. */
    public static RichLine of() {
        return new RichLine();
    }

    /** Готовый текст без своего цвета: цвет берёт чат. */
    public RichLine text(String text) {
        return piece(text, Role.PLAIN, null);
    }

    /** Готовый текст своим цветом: точечная замена палитры на одном куске. */
    public RichLine text(String text, EnumChatFormatting color) {
        return piece(text, Role.PLAIN, color);
    }

    /** Переведённая метка: подпись поля или служебное слово цветом меток. */
    public RichLine label(String translationKey, Object... arguments) {
        return piece(ServerTexts.format(translationKey, arguments), Role.LABEL, null);
    }

    /** Готовое значение цветом значений. */
    public RichLine value(String text) {
        return piece(text, Role.VALUE, null);
    }

    /** Заметное слово цветом акцента: имя сущности или выделение. */
    public RichLine accent(String text) {
        return piece(text, Role.ACCENT, null);
    }

    /** Шапка списка цветом заголовков палитры. */
    public RichLine title(String text) {
        return piece(text, Role.TITLE, null);
    }

    /** Приглушённое дополнение цветом разделителя: точка, двоеточие, номер страницы. */
    public RichLine muted(String text) {
        return piece(text, Role.SEPARATOR, null);
    }

    /** Горизонтальная черта между блоками карточки. */
    public RichLine separator() {
        return piece(DIVIDER, Role.SEPARATOR, null);
    }

    /** Промежуток из пробелов цветом разделителя. */
    public RichLine spacing(int spaces) {
        if (spaces <= 0) {
            return this;
        }
        char[] gap = new char[spaces];
        for (int i = 0; i < gap.length; i++) {
            gap[i] = ' ';
        }
        return piece(new String(gap), Role.SEPARATOR, null);
    }

    /** Кнопка в скобках: клик исполняет команду. */
    public RichLine button(String label, String command) {
        return framed(BUTTON_OPENING, label, BUTTON_CLOSING, new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /** Кнопка в скобках: клик подставляет команду в строку ввода. */
    public RichLine suggestion(String label, String command) {
        return framed(
            BUTTON_OPENING,
            label,
            BUTTON_CLOSING,
            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    /** Клик последнего куска исполняет команду. */
    public RichLine run(String command) {
        return decorate(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /** Клик последнего куска подставляет команду в строку ввода. */
    public RichLine suggest(String command) {
        return decorate(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    /** Наведение на последний кусок показывает готовый переведённый текст. */
    public RichLine hover(String translationKey, Object... arguments) {
        requirePiece();
        String text = ServerTexts.format(translationKey, arguments);
        for (int i = pieceStart; i < segments.size(); i++) {
            segments.get(i).hover = text;
        }
        return this;
    }

    /** Последний кусок жирным. */
    public RichLine bold() {
        requirePiece();
        for (int i = pieceStart; i < segments.size(); i++) {
            segments.get(i).bold = true;
        }
        return this;
    }

    /** Последний кусок курсивом. */
    public RichLine italic() {
        requirePiece();
        for (int i = pieceStart; i < segments.size(); i++) {
            segments.get(i).italic = true;
        }
        return this;
    }

    /** Готовая строка палитрой линейки. */
    public IChatComponent build() {
        return build(PresentTheme.LINEUP);
    }

    /** Готовая строка своей палитрой: корень с кусками-братьями, у каждого свой стиль. */
    public IChatComponent build(PresentTheme theme) {
        IChatComponent whole = new ChatComponentText("");
        for (Segment segment : segments) {
            whole.appendSibling(component(segment, theme));
        }
        return whole;
    }

    /** Плоский текст без цветов и событий: для консоли и журналов. */
    public String plain() {
        StringBuilder text = new StringBuilder();
        for (Segment segment : segments) {
            text.append(segment.text);
        }
        return text.toString();
    }

    RichLine framed(String opening, String label, String closing, ClickEvent click) {
        pieceStart = segments.size();
        if (!opening.isEmpty()) {
            segments.add(new Segment(opening, Role.BRACKET));
        }
        segments.add(new Segment(label, Role.ACTION));
        if (!closing.isEmpty()) {
            segments.add(new Segment(closing, Role.BRACKET));
        }
        return decorate(click);
    }

    private RichLine piece(String text, Role role, EnumChatFormatting override) {
        pieceStart = segments.size();
        Segment segment = new Segment(text, role);
        segment.color = override;
        segments.add(segment);
        return this;
    }

    private RichLine decorate(ClickEvent click) {
        requirePiece();
        for (int i = pieceStart; i < segments.size(); i++) {
            segments.get(i).click = click;
        }
        return this;
    }

    private void requirePiece() {
        if (segments.isEmpty()) {
            throw new IllegalStateException("Add a piece before styling it");
        }
    }

    private static IChatComponent component(Segment segment, PresentTheme theme) {
        IChatComponent piece = new ChatComponentText(segment.text);
        ChatStyle style = piece.getChatStyle();
        EnumChatFormatting color = segment.color == null ? color(segment.role, theme) : segment.color;
        if (color != null) {
            style.setColor(color);
        }
        if (segment.bold) {
            style.setBold(true);
        }
        if (segment.italic) {
            style.setItalic(true);
        }
        if (segment.click != null) {
            style.setChatClickEvent(segment.click);
        }
        if (segment.hover != null) {
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(segment.hover)));
        }
        return piece;
    }

    private static EnumChatFormatting color(Role role, PresentTheme theme) {
        switch (role) {
            case TITLE:
                return theme.title();
            case LABEL:
                return theme.label();
            case VALUE:
                return theme.value();
            case SEPARATOR:
                return theme.separator();
            case ACCENT:
                return theme.accent();
            case BRACKET:
                return theme.bracket();
            case ACTION:
                return theme.action();
            default:
                return null;
        }
    }

    private enum Role {
        PLAIN,
        TITLE,
        LABEL,
        VALUE,
        SEPARATOR,
        ACCENT,
        BRACKET,
        ACTION
    }

    private static final class Segment {

        private final String text;
        private final Role role;
        private EnumChatFormatting color;
        private boolean bold;
        private boolean italic;
        private ClickEvent click;
        private String hover;

        private Segment(String text, Role role) {
            this.text = text;
            this.role = role;
        }
    }
}
