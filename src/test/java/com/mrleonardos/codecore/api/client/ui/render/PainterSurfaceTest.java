package com.mrleonardos.codecore.api.client.ui.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.ClientApi;
import com.mrleonardos.codecore.api.client.ClientRuntime;
import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageHandle;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.image.ImageState;

/**
 * Draw, PlayerHead и Images рисуют через Painter и больше ничем.
 *
 * <p>
 * Подставной painter записывает вызовы списком, поэтому геометрия скруглений, рамки и заглушки аватара
 * проверяется без контекста OpenGL и без запуска игры. До этого проверить её было нечем: каждый примитив
 * уходил прямо в {@code Gui.drawRect} и {@code GL11}.
 */
class PainterSurfaceTest {

    private static final RecordingPainter PAINTER = new RecordingPainter();

    @BeforeAll
    static void installClientSide() {
        if (!ClientApi.available()) {
            ClientApi.install(new FakeClientRuntime(PAINTER));
        }
    }

    @BeforeEach
    void forgetPreviousCalls() {
        PAINTER.calls.clear();
        PAINTER.headDrawn = true;
    }

    @Test
    @DisplayName("заливка уходит в painter как есть")
    void rectGoesStraightThrough() {
        Draw.rect(1, 2, 3, 4, 0xFF204060);

        assertEquals(Collections.singletonList("rect 1,2,3,4 ff204060"), PAINTER.calls);
    }

    @Test
    @DisplayName("прозрачность складывается в цвет до painter")
    void alphaIsFoldedIntoTheColor() {
        Draw.rect(0, 0, 10, 10, 0x204060, 0.5F);

        assertEquals(Collections.singletonList("rect 0,0,10,10 80204060"), PAINTER.calls);
    }

    @Test
    @DisplayName("рамка это четыре заливки по краям")
    void outlineIsFourRects() {
        Draw.outline(0, 0, 10, 6, 0xFF000000);

        assertEquals(
            Arrays.asList(
                "rect 0,0,10,1 ff000000",
                "rect 0,5,10,6 ff000000",
                "rect 0,0,1,6 ff000000",
                "rect 9,0,10,6 ff000000"),
            PAINTER.calls);
    }

    @Test
    @DisplayName("скругление нулевого радиуса это обычный прямоугольник")
    void roundedRectWithoutRadiusIsPlain() {
        Draw.roundedRect(0, 0, 10, 10, 0, 0x112233, 1F);

        assertEquals(Collections.singletonList("rect 0,0,10,10 ff112233"), PAINTER.calls);
    }

    @Test
    @DisplayName("скруглённые углы набираются полосками, середина одной заливкой")
    void roundedRectFillsCornersInStripes() {
        Draw.roundedRect(0, 0, 20, 20, 4, 0x112233, 1F);

        assertEquals(9, PAINTER.calls.size(), "по две полоски на каждый из четырёх шагов радиуса плюс середина");
        assertEquals("rect 0,4,20,16 ff112233", PAINTER.calls.get(8), "середина рисуется последней и целиком");
    }

    @Test
    @DisplayName("ножницы открываются и закрываются через painter")
    void clipGoesThroughThePainter() {
        Draw.clip(1, 2, 3, 4);
        Draw.endClip();

        assertEquals(Arrays.asList("clip 1,2,3,4", "endClip"), PAINTER.calls);
    }

    @Test
    @DisplayName("разбор цвета обходится без painter")
    void colorParsingTouchesNothing() {
        assertEquals(0xFF4CAF50, Draw.color("#4CAF50", 0));
        assertEquals(0xFF4CAF50, Draw.color("4CAF50", 0));
        assertEquals(7, Draw.color("не цвет", 7));
        assertEquals(0x804CAF50, Draw.withAlpha(0xFF4CAF50, 0.5F));
        assertTrue(PAINTER.calls.isEmpty());
    }

    @Test
    @DisplayName("лицо игрока рисует painter, и он же говорит, что скина нет")
    void headIsDrawnByThePainter() {
        assertTrue(PlayerHead.draw("Steve", 4, 5, 8, 0.5F));
        assertEquals(Collections.singletonList("head Steve 4,5 8 0.5"), PAINTER.calls);

        PAINTER.headDrawn = false;
        assertFalse(PlayerHead.draw("Alex", 0, 0, 8));
    }

    @Test
    @DisplayName("заглушка аватара это подложка и буква по центру")
    void placeholderIsARectAndACenteredLetter() {
        PlayerHead.drawPlaceholder("steve", 10, 20, 16);

        assertEquals(2, PAINTER.calls.size());
        assertTrue(
            PAINTER.calls.get(0)
                .startsWith("rect 10,20,26,36 ff"),
            PAINTER.calls.get(0));
        assertEquals("text S 15,24 ffffffff", PAINTER.calls.get(1), "буква по центру квадрата 16 на 16");
    }

    @Test
    @DisplayName("пустой ник заглушку не рисует")
    void placeholderSkipsAnEmptyName() {
        PlayerHead.drawPlaceholder("", 0, 0, 16);

        assertTrue(PAINTER.calls.isEmpty());
    }

    @Test
    @DisplayName("картинка рисуется по ручке")
    void imageIsDrawnByItsHandle() {
        Images.draw(new ReadyHandle(), 3, 4, 32, 0.25F);

        assertEquals(Collections.singletonList("image 3,4 32x32 0.25"), PAINTER.calls);
    }

    private static final class RecordingPainter implements Painter {

        final List<String> calls = new ArrayList<>();

        boolean headDrawn = true;

        @Override
        public int textWidth(String text) {
            return text.length() * 6;
        }

        @Override
        public int lineHeight() {
            return 9;
        }

        @Override
        public void text(String text, int x, int y, int argb) {
            calls.add("text " + text + " " + x + "," + y + " " + Integer.toHexString(argb));
        }

        @Override
        public void rect(int left, int top, int right, int bottom, int argb) {
            calls.add("rect " + left + "," + top + "," + right + "," + bottom + " " + Integer.toHexString(argb));
        }

        @Override
        public void image(ImageHandle image, int x, int y, int width, int height, float alpha) {
            calls.add("image " + x + "," + y + " " + width + "x" + height + " " + alpha);
        }

        @Override
        public boolean head(String playerName, int x, int y, int size, float alpha) {
            calls.add("head " + playerName + " " + x + "," + y + " " + size + " " + alpha);
            return headDrawn;
        }

        @Override
        public boolean blur(int left, int top, int right, int bottom, float strength) {
            calls.add("blur " + left + "," + top + "," + right + "," + bottom + " " + strength);
            return true;
        }

        @Override
        public boolean blurAvailable() {
            return true;
        }

        @Override
        public void clip(int left, int top, int right, int bottom) {
            calls.add("clip " + left + "," + top + "," + right + "," + bottom);
        }

        @Override
        public void endClip() {
            calls.add("endClip");
        }

        @Override
        public void push() {
            calls.add("push");
        }

        @Override
        public void pop() {
            calls.add("pop");
        }

        @Override
        public void translate(int x, int y, int z) {
            calls.add("translate " + x + "," + y + "," + z);
        }
    }

    private static final class FakeClientRuntime implements ClientRuntime {

        private final Painter painter;

        FakeClientRuntime(Painter painter) {
            this.painter = painter;
        }

        @Override
        public ImageService images() {
            return null;
        }

        @Override
        public AvatarService avatars() {
            return null;
        }

        @Override
        public Painter painter() {
            return painter;
        }
    }

    private static final class ReadyHandle implements ImageHandle {

        @Override
        public ImageState state() {
            return ImageState.READY;
        }

        @Override
        public boolean ready() {
            return true;
        }

        @Override
        public ResourceLocation texture() {
            return null;
        }
    }
}
