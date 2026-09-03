package com.mrleonardos.codecore.api.client.ui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

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
 * Рисовать надо по ручке {@link ImageHandle}: неготовая ручка не рисует ничего, и заглушку показывает сам
 * вызывающий. Перегрузки на {@code ResourceLocation} доживают до переезда потребителей.
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

    /** Нарисовать текстуру в квадрате со стороной {@code size}. */
    public static void draw(ResourceLocation texture, int x, int y, int size) {
        draw(texture, x, y, size, size, FULL);
    }

    /** Нарисовать текстуру в квадрате с заданной прозрачностью. */
    public static void draw(ResourceLocation texture, int x, int y, int size, float alpha) {
        draw(texture, x, y, size, size, alpha);
    }

    /** Нарисовать текстуру в прямоугольнике. */
    public static void draw(ResourceLocation texture, int x, int y, int width, int height) {
        draw(texture, x, y, width, height, FULL);
    }

    /** Нарисовать текстуру в прямоугольнике с заданной прозрачностью. */
    public static void draw(ResourceLocation texture, int x, int y, int width, int height, float alpha) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(FULL, FULL, FULL, alpha);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0, FULL);
        tessellator.addVertexWithUV(x + width, y + height, 0, FULL, FULL);
        tessellator.addVertexWithUV(x + width, y, 0, FULL, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();

        GL11.glColor4f(FULL, FULL, FULL, FULL);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
