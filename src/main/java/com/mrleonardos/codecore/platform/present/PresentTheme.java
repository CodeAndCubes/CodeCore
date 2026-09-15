package com.mrleonardos.codecore.platform.present;

import net.minecraft.util.EnumChatFormatting;

/**
 * Палитра богатых ответов линейки: цвета заголовков, меток и кнопок в одном месте.
 *
 * <p>
 * Подобрана под тёмный фон чата: шапка списка бирюзой, метки серым, значения белым, черта и дополнение
 * тёмно-серым, акцент золотом, скобки кнопок тёмно-серым с жёлтым словом. Мод подменяет акцент на свой
 * цвет и берёт остальное готовым, поэтому ответы разных модов читаются как одно целое.
 */
public final class PresentTheme {

    /** Палитра линейки: точка отсчёта для чужих карточек и постраничников. */
    public static final PresentTheme LINEUP = new PresentTheme(
        EnumChatFormatting.AQUA,
        EnumChatFormatting.GRAY,
        EnumChatFormatting.WHITE,
        EnumChatFormatting.DARK_GRAY,
        EnumChatFormatting.GOLD,
        EnumChatFormatting.DARK_GRAY,
        EnumChatFormatting.YELLOW);

    private final EnumChatFormatting title;
    private final EnumChatFormatting label;
    private final EnumChatFormatting value;
    private final EnumChatFormatting separator;
    private final EnumChatFormatting accent;
    private final EnumChatFormatting bracket;
    private final EnumChatFormatting action;

    private PresentTheme(EnumChatFormatting title, EnumChatFormatting label, EnumChatFormatting value,
        EnumChatFormatting separator, EnumChatFormatting accent, EnumChatFormatting bracket,
        EnumChatFormatting action) {
        this.title = title;
        this.label = label;
        this.value = value;
        this.separator = separator;
        this.accent = accent;
        this.bracket = bracket;
        this.action = action;
    }

    /** Копия с другим цветом шапки списка. */
    public PresentTheme title(EnumChatFormatting color) {
        return new PresentTheme(color, label, value, separator, accent, bracket, action);
    }

    /** Копия с другим цветом меток полей. */
    public PresentTheme label(EnumChatFormatting color) {
        return new PresentTheme(title, color, value, separator, accent, bracket, action);
    }

    /** Копия с другим цветом значений. */
    public PresentTheme value(EnumChatFormatting color) {
        return new PresentTheme(title, label, color, separator, accent, bracket, action);
    }

    /** Копия с другим цветом черты и дополнения. */
    public PresentTheme separator(EnumChatFormatting color) {
        return new PresentTheme(title, label, value, color, accent, bracket, action);
    }

    /** Копия с акцентом мода: им красятся заголовок карточки и заметные слова. */
    public PresentTheme accent(EnumChatFormatting color) {
        return new PresentTheme(title, label, value, separator, color, bracket, action);
    }

    /** Копия с другим цветом скобок кнопок. */
    public PresentTheme bracket(EnumChatFormatting color) {
        return new PresentTheme(title, label, value, separator, accent, color, action);
    }

    /** Копия с другим цветом слов кнопок. */
    public PresentTheme action(EnumChatFormatting color) {
        return new PresentTheme(title, label, value, separator, accent, bracket, color);
    }

    EnumChatFormatting title() {
        return title;
    }

    EnumChatFormatting label() {
        return label;
    }

    EnumChatFormatting value() {
        return value;
    }

    EnumChatFormatting separator() {
        return separator;
    }

    EnumChatFormatting accent() {
        return accent;
    }

    EnumChatFormatting bracket() {
        return bracket;
    }

    EnumChatFormatting action() {
        return action;
    }
}
