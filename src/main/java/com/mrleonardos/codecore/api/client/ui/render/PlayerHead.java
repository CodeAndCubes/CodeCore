package com.mrleonardos.codecore.api.client.ui.render;

import java.util.Locale;

import com.mrleonardos.codecore.api.client.ClientApi;

/**
 * Лицо игрока из его скина и заглушка на случай, когда скина нет.
 *
 * <p>
 * Скин берёт {@link Painter} у сущности игрока: у неё он либо загружен, либо это стандартный Стив. Просить
 * скин по нику бесполезно, когда игрока рядом нет: вместо картинки нарисуется отсутствующая текстура.
 *
 * <p>
 * Координаты лица в раскладке 64×64 зашиты в реализацию {@link Painter}. Старый формат 64×32 встречается
 * редко, и на нём лицо окажется смещённым, но разметку это не ломает.
 */
public final class PlayerHead {

    private static final int[] PLACEHOLDER_COLORS = { 0x4C9AFF, 0x36B37E, 0xF0A93B, 0xF4614A, 0x9B6BF2, 0x2EBFC4 };
    private static final int LETTER_COLOR = 0xFFFFFFFF;
    private static final int LETTER_SHIFT = 1;

    private PlayerHead() {}

    /**
     * Нарисовать лицо игрока.
     *
     * @return {@code false}, если скина нет и рисовать было нечего
     */
    public static boolean draw(String playerName, int x, int y, int size) {
        return draw(playerName, x, y, size, 1F);
    }

    /** То же с заданной прозрачностью, чтобы лицо гасло вместе со строкой. */
    public static boolean draw(String playerName, int x, int y, int size, float alpha) {
        return ClientApi.painter()
            .head(playerName, x, y, size, alpha);
    }

    /** Нарисовать заглушку: первая буква ника на цвете, который у этого ника всегда один и тот же. */
    public static void drawPlaceholder(String playerName, int x, int y, int size) {
        drawPlaceholder(playerName, x, y, size, 1F);
    }

    /** Заглушка с заданной прозрачностью. */
    public static void drawPlaceholder(String playerName, int x, int y, int size, float alpha) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }

        Draw.rect(x, y, x + size, y + size, placeholderColor(playerName), alpha);

        Painter painter = ClientApi.painter();
        String letter = playerName.substring(0, 1)
            .toUpperCase(Locale.ROOT);
        int letterX = x + (size - painter.textWidth(letter)) / 2;
        int letterY = y + (size - painter.lineHeight()) / 2 + LETTER_SHIFT;
        painter.text(letter, letterX, letterY, Draw.withAlpha(LETTER_COLOR, alpha));
    }

    /**
     * Цвет заглушки для ника.
     *
     * <p>
     * Знак снимается маской, а не {@code Math.abs}: у него {@code Integer.MIN_VALUE} остаётся отрицательным,
     * а такой хэш подбирается перебором из обычных ников и уронил бы отрисовку чата у всех, кто видит строку.
     */
    static int placeholderColor(String playerName) {
        return PLACEHOLDER_COLORS[(playerName.hashCode() & Integer.MAX_VALUE) % PLACEHOLDER_COLORS.length];
    }
}
