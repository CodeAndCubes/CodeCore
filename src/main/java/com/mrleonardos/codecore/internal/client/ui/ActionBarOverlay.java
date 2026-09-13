package com.mrleonardos.codecore.internal.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import com.mrleonardos.codecore.api.client.ui.anim.Easing;
import com.mrleonardos.codecore.api.client.ui.render.Painter;
import com.mrleonardos.codecore.internal.ActionBarSink;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Строка над хотбаром на экране клиента.
 *
 * <p>
 * Рисуется последней, по {@code Post(ALL)}: это единственная точка, которая срабатывает при любом виде
 * HUD. Привязка к хотбару выглядела бы честнее, но его событие не приходит вовсе, когда хотбар отключён
 * чужим модом, и строка пропала бы вместе с ним.
 *
 * <p>
 * Место считается от подписи предмета, а не от края экрана: подпись игра рисует на {@code height - 59}
 * (и на четырнадцать ниже, когда полосы опыта нет), и строка встаёт над ней. Так обе надписи видны, когда
 * игрок листает хотбар во время показа.
 *
 * <p>
 * Очереди нет: новая строка занимает место прежней. Показ гаснет плавно за последние полсекунды, потому
 * что мгновенное исчезновение глаз читает как мигание.
 */
public final class ActionBarOverlay implements ActionBarSink {

    private static final int ITEM_NAME_Y = 59;
    private static final int NO_EXPERIENCE_SHIFT = 14;
    private static final int GAP = 4;
    private static final int FADE_MILLIS = 500;
    private static final long MILLIS = 1000L;
    private static final int WHITE = 0xFFFFFF;
    private static final int SHADOW = 0x3F3F3F;
    private static final int OPAQUE = 255;

    /** Ниже этой альфы шрифт игры считает цвет непрозрачным, и строка вспыхнула бы на прощание. */
    private static final int MIN_ALPHA = 8;

    private final Painter painter;

    private String text = "";
    private long hideAt;

    public ActionBarOverlay(Painter painter) {
        this.painter = painter;
    }

    @Override
    public void show(String text, int seconds) {
        this.text = text == null ? "" : text;
        this.hideAt = System.currentTimeMillis() + seconds * MILLIS;
    }

    @Override
    public void clear() {
        text = "";
        hideAt = 0;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || text.isEmpty()) {
            return;
        }
        long left = hideAt - System.currentTimeMillis();
        if (left <= 0) {
            clear();
            return;
        }
        int alpha = (int) (Easing.outCubic(Math.min(1F, left / (float) FADE_MILLIS)) * OPAQUE);
        if (alpha < MIN_ALPHA) {
            return;
        }
        int x = (event.resolution.getScaledWidth() - painter.textWidth(text)) / 2;
        int y = event.resolution.getScaledHeight() - ITEM_NAME_Y - painter.lineHeight() - GAP + shift();

        painter.text(text, x + 1, y + 1, alpha << 24 | SHADOW);
        painter.text(text, x, y, alpha << 24 | WHITE);
    }

    /** Без полосы опыта подпись предмета опускается, и строка едет за ней. */
    private static int shift() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.playerController != null && !minecraft.playerController.shouldDrawHUD() ? NO_EXPERIENCE_SHIFT
            : 0;
    }
}
