package com.mrleonardos.codecore.internal.client.image;

import java.awt.image.BufferedImage;

import net.minecraft.util.ResourceLocation;

/**
 * Куда уходит готовая картинка.
 *
 * <p>
 * Отдельный интерфейс нужен ради тестов: сервис картинок проверяется без контекста OpenGL, а живая
 * реализация одна и живёт в {@link ImageTextures}.
 */
interface ImageTextureSink {

    ResourceLocation upload(BufferedImage image);

    void release(ResourceLocation location);
}
