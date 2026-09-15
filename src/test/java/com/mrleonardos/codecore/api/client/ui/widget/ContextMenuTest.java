package com.mrleonardos.codecore.api.client.ui.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.ClientApi;
import com.mrleonardos.codecore.api.client.ClientRuntime;
import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageHandle;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.ui.render.Painter;
import com.mrleonardos.codecore.api.client.ui.render.WorldOutline;

/**
 * Геометрия выпадающего меню: координаты пунктов наблюдаются через подставной painter, без OpenGL.
 */
class ContextMenuTest {

    private static final RecordingPainter PAINTER = new RecordingPainter();

    @BeforeAll
    static void installClientSide() {
        if (!ClientApi.available()) {
            ClientApi.install(new FakeClientRuntime(PAINTER));
        }
    }

    @BeforeEach
    void forgetPreviousCalls() {
        PAINTER.texts.clear();
    }

    @Test
    @DisplayName("меню не уезжает за левый и верхний край экрана")
    void menuStaysInsideTheLeftAndTopEdges() {
        ContextMenu menu = new ContextMenu();
        menu.open(PAINTER, items(), -40, -40, 100, 100);
        menu.render(PAINTER, 0, 0);

        assertEquals(Collections.singletonList("Пункт 6,6"), PAINTER.texts, "пункт отступает на границу экрана");
    }

    @Test
    @DisplayName("от правого края меню отодвигается и не вылезает слева")
    void menuStaysInsideTheRightEdge() {
        ContextMenu menu = new ContextMenu();
        menu.open(PAINTER, items(), 95, 50, 100, 100);
        menu.render(PAINTER, 0, 0);

        assertTrue(
            PAINTER.texts.get(0)
                .endsWith(" 32,54"),
            PAINTER.texts.get(0));
    }

    private static List<ContextMenu.Item> items() {
        return Arrays.asList(new ContextMenu.Item("Пункт", () -> {}));
    }

    private static final class RecordingPainter implements Painter {

        final List<String> texts = new ArrayList<>();

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
            texts.add(text + " " + x + "," + y);
        }

        @Override
        public void rect(int left, int top, int right, int bottom, int argb) {}

        @Override
        public void image(ImageHandle image, int x, int y, int width, int height, float alpha) {}

        @Override
        public boolean head(String playerName, int x, int y, int size, float alpha) {
            return false;
        }

        @Override
        public boolean blur(int left, int top, int right, int bottom, float strength) {
            return false;
        }

        @Override
        public boolean blurAvailable() {
            return false;
        }

        @Override
        public void clip(int left, int top, int right, int bottom) {}

        @Override
        public void endClip() {}

        @Override
        public void push() {}

        @Override
        public void pop() {}

        @Override
        public void translate(int x, int y, int z) {}
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

        @Override
        public WorldOutline outline() {
            return null;
        }
    }
}
