package com.mrleonardos.codecore.internal.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.mrleonardos.codecore.api.client.image.ImageHandle;
import com.mrleonardos.codecore.api.client.ui.render.Blur;
import com.mrleonardos.codecore.api.client.ui.render.Painter;

public final class GlPainter implements Painter {

    private static final float FULL = 1F;
    private static final float FACE_LEFT = 8F / 64F;
    private static final float FACE_TOP = 8F / 64F;
    private static final float FACE_RIGHT = 16F / 64F;
    private static final float FACE_BOTTOM = 16F / 64F;
    private static final float HAT_LEFT = 40F / 64F;
    private static final float HAT_RIGHT = 48F / 64F;

    @Override
    public int textWidth(String text) {
        return font().getStringWidth(text);
    }

    @Override
    public int lineHeight() {
        return font().FONT_HEIGHT;
    }

    @Override
    public void text(String text, int x, int y, int argb) {
        font().drawString(text, x, y, argb);
    }

    @Override
    public void rect(int left, int top, int right, int bottom, int argb) {
        Gui.drawRect(left, top, right, bottom, argb);
    }

    @Override
    public void image(ImageHandle image, int x, int y, int width, int height, float alpha) {
        if (image == null || !image.ready()) {
            return;
        }
        bind(image.texture());
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(FULL, FULL, FULL, alpha);

        quad(x, y, width, height, 0, 0, FULL, FULL);

        GL11.glColor4f(FULL, FULL, FULL, FULL);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public boolean head(String playerName, int x, int y, int size, float alpha) {
        ResourceLocation skin = skinOf(playerName);
        if (skin == null) {
            return false;
        }
        bind(skin);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(FULL, FULL, FULL, alpha);

        quad(x, y, size, size, FACE_LEFT, FACE_TOP, FACE_RIGHT, FACE_BOTTOM);
        quad(x, y, size, size, HAT_LEFT, FACE_TOP, HAT_RIGHT, FACE_BOTTOM);

        GL11.glColor4f(FULL, FULL, FULL, FULL);
        GL11.glDisable(GL11.GL_BLEND);
        return true;
    }

    @Override
    public boolean blur(int left, int top, int right, int bottom, float strength) {
        return Blur.draw(left, top, right, bottom, strength);
    }

    @Override
    public boolean blurAvailable() {
        return Blur.available();
    }

    @Override
    public void clip(int left, int top, int right, int bottom) {
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scale = resolution.getScaleFactor();
        int height = bottom - top;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            left * scale,
            minecraft.displayHeight - (top + height) * scale,
            Math.max(0, (right - left) * scale),
            Math.max(0, height * scale));
    }

    @Override
    public void endClip() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void push() {
        GL11.glPushMatrix();
    }

    @Override
    public void pop() {
        GL11.glPopMatrix();
    }

    @Override
    public void translate(int x, int y, int z) {
        GL11.glTranslatef(x, y, z);
    }

    private static FontRenderer font() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    private static void bind(ResourceLocation texture) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
    }

    private static ResourceLocation skinOf(String playerName) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (playerName == null || playerName.isEmpty() || minecraft.theWorld == null) {
            return null;
        }
        EntityPlayer player = minecraft.theWorld.getPlayerEntityByName(playerName);
        return player instanceof AbstractClientPlayer ? ((AbstractClientPlayer) player).getLocationSkin() : null;
    }

    private static void quad(int x, int y, int width, int height, float u0, float v0, float u1, float v1) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, u0, v1);
        tessellator.addVertexWithUV(x + width, y + height, 0, u1, v1);
        tessellator.addVertexWithUV(x + width, y, 0, u1, v0);
        tessellator.addVertexWithUV(x, y, 0, u0, v0);
        tessellator.draw();
    }
}
