package com.mrleonardos.codecore.api.client.ui.anim;

/**
 * Число, которое доезжает до нового значения не мгновенно.
 *
 * <p>
 * Хранит, откуда и куда едет, и считает текущее значение по часам. Привязки к тикам нет намеренно:
 * интерфейс рисуется чаще, чем идут тики, и по тикам движение выглядит рваным.
 */
public final class Animated {

    private final int durationMillis;

    private float from;
    private float target;
    private long startedAt;

    public Animated(float initial, int durationMillis) {
        this.from = initial;
        this.target = initial;
        this.durationMillis = durationMillis;
        this.startedAt = 0;
    }

    /** Задать новую цель. Повторный вызов с той же целью ничего не сбрасывает. */
    public void to(float value) {
        if (Math.abs(value - target) < 0.001F) {
            return;
        }
        from = value();
        target = value;
        startedAt = System.currentTimeMillis();
    }

    /** Поставить значение сразу, без движения. */
    public void set(float value) {
        from = value;
        target = value;
        startedAt = 0;
    }

    /** Текущее значение. */
    public float value() {
        if (startedAt == 0 || durationMillis <= 0) {
            return target;
        }
        float progress = (System.currentTimeMillis() - startedAt) / (float) durationMillis;
        if (progress >= 1F) {
            return target;
        }
        return from + (target - from) * Easing.outCubic(progress);
    }

    /** Доехало ли значение до цели. */
    public boolean settled() {
        return Math.abs(value() - target) < 0.001F;
    }

    public float target() {
        return target;
    }
}
