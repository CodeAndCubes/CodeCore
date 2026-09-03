package com.mrleonardos.codecore.api.client.ui.render;

import com.mrleonardos.codecore.api.client.ClientApi;
import com.mrleonardos.codecore.api.client.image.ImageHandle;

/**
 * Отрисовка готовой картинки в интерфейсе.
 *
 * <p>
 * В 1.7.10 у стандартного {@code drawTexturedModalRect} размер картинки зашит как 256×256, поэтому
 * произвольную текстуру он рисует кусочком. {@link Painter} собирает квад руками, и картинка любого
 * размера ложится в заданный прямоугольник целиком.
 *
 * <p>
 * Рисуется картинка по ручке {@link ImageHandle}: неготовая ручка не рисует ничего, и заглушку показывает
 * сам вызывающий.
 */
public final class Images {

    private static final float FULL = 1F;

    private Images() {}

    /** Нарисовать картинку в квадрате со стороной {@code size}. */
    public static void draw(ImageHandle image, int x, int y, int size) {
        draw(image, x, y, size, size, FULL);
    }

    /** Нарисовать картинку в квадрате с заданной прозрачностью. */
    public static void draw(ImageHandle image, int x, int y, int size, float alpha) {
        draw(image, x, y, size, size, alpha);
    }

    /** Нарисовать картинку в прямоугольнике. */
    public static void draw(ImageHandle image, int x, int y, int width, int height) {
        draw(image, x, y, width, height, FULL);
    }

    /** Нарисовать картинку в прямоугольнике с заданной прозрачностью. */
    public static void draw(ImageHandle image, int x, int y, int width, int height, float alpha) {
        ClientApi.painter()
            .image(image, x, y, width, height, alpha);
    }

}
