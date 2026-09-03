package com.mrleonardos.codecore.internal.client.image;

import net.minecraft.util.ResourceLocation;

import com.mrleonardos.codecore.api.client.image.ImageHandle;

/**
 * Ручка, за которой стоит текстура видеокарты.
 *
 * <p>
 * Наружу текстура не отдаётся: {@link ImageHandle} в api про неё не знает, а рисующий код передаёт саму
 * ручку. Этот интерфейс существует затем, чтобы реализация {@code Painter} внутри ядра могла добраться до
 * текстуры, не открывая её чужим модам.
 */
public interface TexturedHandle extends ImageHandle {

    /** Текстура или {@code null}, пока картинка не готова. */
    ResourceLocation texture();
}
