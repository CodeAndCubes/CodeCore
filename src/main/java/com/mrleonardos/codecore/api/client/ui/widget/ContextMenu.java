package com.mrleonardos.codecore.api.client.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.mrleonardos.codecore.api.client.ui.render.Draw;
import com.mrleonardos.codecore.api.client.ui.render.Painter;
import com.mrleonardos.codecore.api.client.ui.theme.Theme;

/**
 * Выпадающее меню по клику.
 *
 * <p>
 * Меню само отодвигается от краёв экрана, поэтому его можно открывать хоть в правом нижнем углу ленты.
 *
 * <p>
 * Мерку и отрисовку подписи делает {@link Painter}.
 */
public final class ContextMenu {

    private static final int ITEM_HEIGHT = 12;
    private static final int PADDING = 4;
    private static final int MIN_WIDTH = 70;
    private static final int SCREEN_MARGIN = 2;

    private final List<Item> items = new ArrayList<>();
    private int left;
    private int top;
    private int width;
    private boolean open;

    /** Открыть меню в точке; список пунктов задаётся заново на каждое открытие. */
    public void open(Painter painter, List<Item> entries, int x, int y, int screenWidth, int screenHeight) {
        items.clear();
        items.addAll(entries);
        if (items.isEmpty()) {
            open = false;
            return;
        }

        width = MIN_WIDTH;
        for (Item item : items) {
            width = Math.max(width, painter.textWidth(item.label()) + PADDING * 2);
        }

        left = Math.max(SCREEN_MARGIN, Math.min(x, screenWidth - width - SCREEN_MARGIN));
        top = Math.max(SCREEN_MARGIN, Math.min(y, screenHeight - height() - SCREEN_MARGIN));
        open = true;
    }

    public void close() {
        open = false;
        items.clear();
    }

    public boolean open() {
        return open;
    }

    public void render(Painter painter, int mouseX, int mouseY) {
        if (!open) {
            return;
        }

        int bottom = top + height();
        Draw.rect(left, top, left + width, bottom, Theme.SURFACE_RAISED, 0.95F);
        Draw.outline(left, top, left + width, bottom, Theme.OUTLINE | 0xFF000000);

        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            int itemTop = top + PADDING / 2 + index * ITEM_HEIGHT;
            boolean hovered = item.enabled() && inside(mouseX, mouseY, itemTop);

            if (hovered) {
                Draw.rect(left + 1, itemTop, left + width - 1, itemTop + ITEM_HEIGHT, Theme.ACCENT, 0.25F);
            }
            int color = item.enabled() ? (hovered ? Theme.TEXT : Theme.TEXT_MUTED) : Theme.TEXT_DISABLED;
            painter.text(item.label(), left + PADDING, itemTop + 2, color | 0xFF000000);
        }
    }

    /**
     * Обработать клик.
     *
     * @return {@code true}, если клик пришёлся на меню и дальше его передавать не нужно
     */
    public boolean click(int mouseX, int mouseY) {
        if (!open) {
            return false;
        }
        boolean insideMenu = mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height();
        if (!insideMenu) {
            close();
            return true;
        }

        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            if (!inside(mouseX, mouseY, top + PADDING / 2 + index * ITEM_HEIGHT)) {
                continue;
            }
            close();
            if (item.enabled()) {
                item.action()
                    .run();
            }
            return true;
        }
        close();
        return true;
    }

    private boolean inside(int mouseX, int mouseY, int itemTop) {
        return mouseX >= left && mouseX <= left + width && mouseY >= itemTop && mouseY < itemTop + ITEM_HEIGHT;
    }

    private int height() {
        return items.size() * ITEM_HEIGHT + PADDING;
    }

    /** Пункт меню: подпись, действие и признак доступности. */
    public static final class Item {

        private final String label;
        private final Runnable action;
        private final boolean enabled;

        public Item(String label, Runnable action) {
            this(label, action, true);
        }

        public Item(String label, Runnable action, boolean enabled) {
            this.label = label;
            this.action = action;
            this.enabled = enabled;
        }

        public String label() {
            return label;
        }

        public Runnable action() {
            return action;
        }

        public boolean enabled() {
            return enabled;
        }
    }
}
