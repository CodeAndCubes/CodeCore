package com.mrleonardos.codecore.api.client.ui.theme;

/**
 * Палитра и размеры интерфейса.
 *
 * <p>
 * Один набор значений на все моды линейки: так окна чата, прав и всего, что появится дальше, выглядят
 * частями одного целого, а не набором разных поделок.
 */
public final class Theme {

    /** Фон панели. */
    public static final int SURFACE = 0x14161A;

    /** Фон панели, лежащей поверх другой. */
    public static final int SURFACE_RAISED = 0x1E2126;

    /** Разделители и рамки. */
    public static final int OUTLINE = 0x2E333B;

    /** Основной текст. */
    public static final int TEXT = 0xE6E9EF;

    /** Второстепенный текст: время, подписи. */
    public static final int TEXT_MUTED = 0x8A9199;

    /** Текст недоступного действия. */
    public static final int TEXT_DISABLED = 0x5A616B;

    /** Акцент: активная вкладка, выделение. */
    public static final int ACCENT = 0x4C9AFF;

    /** Тревожный цвет: ошибки, удалённое. */
    public static final int DANGER = 0xF4614A;

    /** Цвет непрочитанного бейджа. */
    public static final int BADGE = 0xF0A93B;

    /** Отступ внутри панелей. */
    public static final int PADDING = 4;

    /** Высота строки вкладок. */
    public static final int TAB_HEIGHT = 12;

    /** Скругление, которое имитируется срезанными углами. */
    public static final int CORNER = 2;

    private Theme() {}
}
