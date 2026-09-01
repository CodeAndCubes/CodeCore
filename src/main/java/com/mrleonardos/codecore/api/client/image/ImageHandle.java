package com.mrleonardos.codecore.api.client.image;

import net.minecraft.util.ResourceLocation;

/**
 * Ответ на просьбу о картинке.
 *
 * <p>
 * Объект возвращается сразу, ещё до загрузки: рисующий код спрашивает {@link #ready()} каждый кадр и
 * показывает запасной вариант, пока картинки нет. Ждать загрузку в потоке отрисовки нельзя: это фриз на
 * всё время сетевого запроса.
 */
public interface ImageHandle {

    ImageState state();

    /** Готова ли картинка к отрисовке. */
    boolean ready();

    /** Текстура или {@code null}, пока картинка не готова. */
    ResourceLocation texture();
}
