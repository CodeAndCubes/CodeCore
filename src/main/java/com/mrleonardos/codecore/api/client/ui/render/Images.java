package com.mrleonardos.codecore.api.client.ui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * Отрисовка готовой текстуры в интерфейсе.
 *
 * <p>
 * В 1.7.10 у стандартного {@code drawTexturedModalRect} размер картинки зашит как 256×256, поэтому
 * произвольную текстуру он рисует кусочком. Здесь квад собирается руками, и картинка любого размера
 * ложится в заданный прямоугольник целиком.
 */
public final class Images {

    private static final float FULL = 1F;

    private Images() {}

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
