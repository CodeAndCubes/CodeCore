package com.mrleonardos.codecore.api.client.ui.render;

import com.mrleonardos.codecore.api.client.ClientApi;

/**
 * Размытие того, что уже нарисовано под панелью.
 *
 * <p>
 * Работу делает {@link Painter}: кадр копируется в текстуру и рисуется обратно через шейдер, который
 * усредняет соседние пиксели.
 *
 * <p>
 * Шейдеры есть не везде, поэтому {@link #available()} спрашивают до вызова: без них панель просто рисуется
 * плотнее. Молча оставлять прозрачную дыру нельзя: на светлом фоне текст станет нечитаемым.
 */
public final class Blur {

    private Blur() {}

    /** Умеет ли эта видеокарта то, что нужно для размытия. */
    public static boolean available() {
        return ClientApi.painter()
            .blurAvailable();
    }

    /**
     * Размыть содержимое экрана под указанным прямоугольником.
     *
     * @param strength радиус размытия в пикселях экрана
     * @return {@code false}, если размыть не вышло; тогда вызывающий рисует обычную подложку
     */
    public static boolean draw(int left, int top, int right, int bottom, float strength) {
        return ClientApi.painter()
            .blur(left, top, right, bottom, strength);
    }
}
