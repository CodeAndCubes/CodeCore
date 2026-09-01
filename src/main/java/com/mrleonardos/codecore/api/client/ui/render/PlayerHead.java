package com.mrleonardos.codecore.api.client.ui.render;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * Лицо игрока из его скина и заглушка на случай, когда скина нет.
 *
 * <p>
 * Скин берётся у сущности игрока: у неё он либо загружен, либо это стандартный Стив. Просить скин по нику
 * бесполезно, когда игрока рядом нет: вместо картинки нарисуется отсутствующая текстура.
 *
 * <p>
 * Координаты лица взяты из раскладки 64×64. Старый формат 64×32 встречается редко, и на нём лицо окажется
 * смещённым, но разметку это не ломает.
 */
public final class PlayerHead {

    private static final float FACE_LEFT = 8F / 64F;
    private static final float FACE_TOP = 8F / 64F;
    private static final float FACE_RIGHT = 16F / 64F;
    private static final float FACE_BOTTOM = 16F / 64F;
    private static final float HAT_LEFT = 40F / 64F;
    private static final float HAT_RIGHT = 48F / 64F;

    private static final int[] PLACEHOLDER_COLORS = { 0x4C9AFF, 0x36B37E, 0xF0A93B, 0xF4614A, 0x9B6BF2, 0x2EBFC4 };
    private static final int LETTER_COLOR = 0xFFFFFFFF;
    private static final int LETTER_SHIFT = 1;

    private PlayerHead() {}

    /**
     * Нарисовать лицо игрока.
     *
     * @return {@code false}, если скина нет и рисовать было нечего
     */
    public static boolean draw(String playerName, int x, int y, int size) {
        return draw(playerName, x, y, size, 1F);
    }

    /** То же с заданной прозрачностью, чтобы лицо гасло вместе со строкой. */
    public static boolean draw(String playerName, int x, int y, int size, float alpha) {
        ResourceLocation skin = skinOf(playerName);
        if (skin == null) {
            return false;
        }

        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(skin);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, alpha);

        quad(x, y, size, FACE_LEFT, FACE_TOP, FACE_RIGHT, FACE_BOTTOM);
        quad(x, y, size, HAT_LEFT, FACE_TOP, HAT_RIGHT, FACE_BOTTOM);

        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glDisable(GL11.GL_BLEND);
        return true;
    }

    /** Нарисовать заглушку: первая буква ника на цвете, который у этого ника всегда один и тот же. */
    public static void drawPlaceholder(String playerName, int x, int y, int size) {
        drawPlaceholder(playerName, x, y, size, 1F);
    }

    /** Заглушка с заданной прозрачностью. */
    public static void drawPlaceholder(String playerName, int x, int y, int size, float alpha) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }

        Draw.rect(x, y, x + size, y + size, placeholderColor(playerName), alpha);

        Minecraft minecraft = Minecraft.getMinecraft();
        String letter = playerName.substring(0, 1)
            .toUpperCase(Locale.ROOT);
        int letterX = x + (size - minecraft.fontRenderer.getStringWidth(letter)) / 2;
        int letterY = y + (size - minecraft.fontRenderer.FONT_HEIGHT) / 2 + LETTER_SHIFT;
        minecraft.fontRenderer.drawString(letter, letterX, letterY, Draw.withAlpha(LETTER_COLOR, alpha));
    }

    /**
     * Цвет заглушки для ника.
     *
     * <p>
     * Знак снимается маской, а не {@code Math.abs}: у него {@code Integer.MIN_VALUE} остаётся отрицательным,
     * а такой хэш подбирается перебором из обычных ников и уронил бы отрисовку чата у всех, кто видит строку.
     */
    static int placeholderColor(String playerName) {
        return PLACEHOLDER_COLORS[(playerName.hashCode() & Integer.MAX_VALUE) % PLACEHOLDER_COLORS.length];
    }

    private static ResourceLocation skinOf(String playerName) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (playerName == null || playerName.isEmpty() || minecraft.theWorld == null) {
            return null;
        }
        EntityPlayer player = minecraft.theWorld.getPlayerEntityByName(playerName);
        return player instanceof AbstractClientPlayer ? ((AbstractClientPlayer) player).getLocationSkin() : null;
    }

    private static void quad(int x, int y, int size, float u0, float v0, float u1, float v1) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + size, 0, u0, v1);
        tessellator.addVertexWithUV(x + size, y + size, 0, u1, v1);
        tessellator.addVertexWithUV(x + size, y, 0, u1, v0);
        tessellator.addVertexWithUV(x, y, 0, u0, v0);
        tessellator.draw();
    }
}
