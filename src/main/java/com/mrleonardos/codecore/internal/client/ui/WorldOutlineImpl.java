package com.mrleonardos.codecore.internal.client.ui;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.GL11;

import com.mrleonardos.codecore.api.client.ui.anim.Easing;
import com.mrleonardos.codecore.api.client.ui.render.OutlineBox;
import com.mrleonardos.codecore.api.client.ui.render.OutlineStyle;
import com.mrleonardos.codecore.api.client.ui.render.WorldOutline;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Единственная на линейку подписка на отрисовку мира.
 *
 * <p>
 * Линии рисуются без записи в буфер глубины: иначе рамка резала бы блоки за собой, а прозрачная вода и
 * стекло начали бы мигать. Стиль решает, видно ли её сквозь блоки; выключенная проверка глубины нужна
 * границе привата, потому что искать её взглядом по стенам игрок не будет.
 *
 * <p>
 * Состояние OpenGL снимается и возвращается через {@code glPushAttrib}, а не выставляется обратно руками:
 * возвращать включённым то, что было выключено до нас, значит однажды подарить чужому моду освещение,
 * которого он не просил.
 *
 * <p>
 * Набор снимается при входе в мир, а не только при выходе с сервера: игрок ушёл в нижний мир, и выделение
 * верхнего к нему отношения не имеет.
 */
public final class WorldOutlineImpl implements WorldOutline {

    private static final float FULL = 255F;
    private static final float BLINK_FLOOR = 0.35F;
    private static final float BLINK_RANGE = 0.65F;

    private final OutlineSets sets = new OutlineSets();
    private final Supplier<ClientOutlineSettings> settings;

    public WorldOutlineImpl(Supplier<ClientOutlineSettings> settings) {
        this.settings = settings;
    }

    @Override
    public void show(String key, OutlineStyle style, List<OutlineBox> boxes) {
        sets.show(key, style, boxes);
    }

    @Override
    public void hide(String key) {
        sets.hide(key);
    }

    @Override
    public boolean shown(String key) {
        return sets.shown(key);
    }

    @Override
    public void clear() {
        sets.clear();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.world != null && event.world.isRemote) {
            sets.clear();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        ClientOutlineSettings options = settings.get();
        if (!options.enabled) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityLivingBase camera = minecraft.renderViewEntity;
        if (camera == null || minecraft.theWorld == null) {
            return;
        }
        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.partialTicks;
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.partialTicks;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LINE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glTranslated(-camX, -camY, -camZ);

        sets.visible(
            minecraft.theWorld.provider.dimensionId,
            camX,
            camZ,
            options.maxDistance,
            (style, box) -> draw(style, box));

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void draw(OutlineStyle style, OutlineBox box) {
        int argb = style.argb();
        float alpha = (argb >>> 24) / FULL * blink(style);
        if (style.visibleThroughBlocks()) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        GL11.glLineWidth(style.thickness());

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA_F((argb >> 16 & 0xFF) / FULL, (argb >> 8 & 0xFF) / FULL, (argb & 0xFF) / FULL, alpha);
        OutlineLines.of(box, style.grid(), (x1, y1, z1, x2, y2, z2) -> {
            tessellator.addVertex(x1, y1, z1);
            tessellator.addVertex(x2, y2, z2);
        });
        tessellator.draw();
    }

    /** Мигание считается по часам, а не по тикам: кадров больше, и по тикам рамка дёргалась бы. */
    private static float blink(OutlineStyle style) {
        int period = style.blinkMillis();
        if (period <= 0) {
            return 1F;
        }
        float progress = System.currentTimeMillis() % period / (float) period;
        return BLINK_FLOOR + BLINK_RANGE * Easing.pulse(progress);
    }
}
