package com.mrleonardos.codecore.api.client.ui.anim;

/**
 * Кривые сглаживания.
 *
 * <p>
 * Равномерное движение выглядит механическим: интерфейс кажется живым, когда начинает быстро и
 * притормаживает к концу. Поэтому по умолчанию везде используется {@link #outCubic(float)}.
 */
public final class Easing {

    private static final float HALF = 0.5F;
    private static final float TWO = 2F;
    private static final float FOUR = 4F;

    private Easing() {}

    /** Быстрый старт, мягкая остановка. */
    public static float outCubic(float progress) {
        float shifted = clamp(progress) - 1F;
        return shifted * shifted * shifted + 1F;
    }

    /** Мягко с обоих концов: для того, что и появляется, и исчезает. */
    public static float inOutQuad(float progress) {
        float value = clamp(progress);
        return value < HALF ? TWO * value * value : 1F - (-TWO * value + TWO) * (-TWO * value + TWO) / TWO;
    }

    /** Короткий всплеск и возврат: для пульсации. */
    public static float pulse(float progress) {
        float value = clamp(progress);
        return FOUR * value * (1F - value);
    }

    private static float clamp(float progress) {
        return Math.max(0F, Math.min(1F, progress));
    }
}
