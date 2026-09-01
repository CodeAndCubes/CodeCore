package com.mrleonardos.codecore.internal.client.image;

import java.awt.image.BufferedImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/**
 * Отдаёт готовую картинку видеокарте.
 *
 * <p>
 * Вызывать только из главного потока клиента: контекст OpenGL принадлежит ему, и загрузка текстуры из
 * рабочего потока молча портит состояние отрисовки.
 */
final class ImageTextures implements ImageTextureSink {

    private static final String NAME_PREFIX = "codecore/image";

    @Override
    public ResourceLocation upload(BufferedImage image) {
        DynamicTexture texture = new DynamicTexture(image.getWidth(), image.getHeight());
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), texture.getTextureData(), 0, image.getWidth());
        texture.updateDynamicTexture();

        return Minecraft.getMinecraft()
            .getTextureManager()
            .getDynamicTextureLocation(NAME_PREFIX, texture);
    }

    @Override
    public void release(ResourceLocation location) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .deleteTexture(location);
    }
}
