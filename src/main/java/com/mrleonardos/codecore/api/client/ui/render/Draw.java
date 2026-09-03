package com.mrleonardos.codecore.api.client.ui.render;

import com.mrleonardos.codecore.api.client.ClientApi;

/**
 * Примитивы рисования интерфейса.
 *
 * <p>
 * Подписи здесь целиком на {@code int}, {@code float} и {@code String}, а работу делает {@link Painter}:
 * это он знает про {@code Gui.drawRect}, {@code glScissor} и масштаб интерфейса. Обрезка по прямоугольнику
 * живёт там же не случайно: {@code glScissor} работает в пикселях окна, а интерфейс рисуется в
 * масштабированных координатах, и из-за путаницы между ними на нестандартном масштабе интерфейс уезжает.
 */
public final class Draw {

    private static final int FULL_ALPHA = 0xFF000000;
    private static final int COLOR_MASK = 0x00FFFFFF;
    private static final int SHADOW_COLOR = 0x000000;
    private static final double HALF_PIXEL = 0.5;

    private Draw() {}

    /** Заливка прямоугольника цветом с альфой. */
    public static void rect(int left, int top, int right, int bottom, int argb) {
        ClientApi.painter()
            .rect(left, top, right, bottom, argb);
    }

    /** Заливка со сплошным цветом и отдельной прозрачностью от нуля до единицы. */
    public static void rect(int left, int top, int right, int bottom, int rgb, float alpha) {
        int clamped = Math.max(0, Math.min(255, Math.round(alpha * 255)));
        rect(left, top, right, bottom, (clamped << 24) | (rgb & COLOR_MASK));
    }

    /** Горизонтальная линия толщиной в пиксель. */
    public static void horizontalLine(int left, int right, int top, int argb) {
        rect(left, top, right, top + 1, argb);
    }

    /** Вертикальная линия толщиной в пиксель. */
    public static void verticalLine(int left, int top, int bottom, int argb) {
        rect(left, top, left + 1, bottom, argb);
    }

    /**
     * Прямоугольник со скруглёнными углами.
     *
     * <p>
     * Углы набираются полосками по окружности, а не рисуются веером треугольников: обычная заливка
     * работает при любом состоянии OpenGL, тогда как собственный вызов тесселятора зависит от того, что
     * оставил после себя предыдущий рисующий код.
     */
    public static void roundedRect(int left, int top, int right, int bottom, int radius, int rgb, float alpha) {
        int limited = Math.max(0, Math.min(radius, Math.min(right - left, bottom - top) / 2));
        if (limited == 0) {
            rect(left, top, right, bottom, rgb, alpha);
            return;
        }

        for (int step = 0; step < limited; step++) {
            double offset = limited - step - HALF_PIXEL;
            int inset = limited - (int) Math.round(Math.sqrt((double) limited * limited - offset * offset));
            rect(left + inset, top + step, right - inset, top + step + 1, rgb, alpha);
            rect(left + inset, bottom - step - 1, right - inset, bottom - step, rgb, alpha);
        }
        rect(left, top + limited, right, bottom - limited, rgb, alpha);
    }

    /**
     * Мягкая тень вокруг прямоугольника.
     *
     * <p>
     * Несколько рамок с убывающей прозрачностью: дешевле размытия и на тёмном фоне неотличимо, зато панель
     * перестаёт выглядеть наклейкой поверх картинки.
     */
    public static void shadow(int left, int top, int right, int bottom, int spread, float alpha) {
        for (int step = spread; step >= 1; step--) {
            float layer = alpha * (1F - (step - 1) / (float) spread) / spread;
            rect(left - step, top - step, right + step, bottom + step, SHADOW_COLOR, layer);
        }
    }

    /** Рамка толщиной в пиксель по границе прямоугольника. */
    public static void outline(int left, int top, int right, int bottom, int argb) {
        horizontalLine(left, right, top, argb);
        horizontalLine(left, right, bottom - 1, argb);
        verticalLine(left, top, bottom, argb);
        verticalLine(right - 1, top, bottom, argb);
    }

    /**
     * Ограничить рисование прямоугольником в координатах интерфейса.
     *
     * <p>
     * Обязательно закрывать вызовом {@link #endClip()}.
     */
    public static void clip(int left, int top, int right, int bottom) {
        ClientApi.painter()
            .clip(left, top, right, bottom);
    }

    /** Снять ограничение области рисования. */
    public static void endClip() {
        ClientApi.painter()
            .endClip();
    }

    /** Разобрать цвет вида {@code #4CAF50}; при непонятной записи вернуть запасной. */
    public static int color(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String digits = value.startsWith("#") ? value.substring(1) : value;
        try {
            return FULL_ALPHA | (int) (Long.parseLong(digits, 16) & COLOR_MASK);
        } catch (NumberFormatException malformed) {
            return fallback;
        }
    }

    /** Тот же цвет с другой прозрачностью. */
    public static int withAlpha(int argb, float alpha) {
        int clamped = Math.max(0, Math.min(255, Math.round(alpha * 255)));
        return (clamped << 24) | (argb & COLOR_MASK);
    }
}
