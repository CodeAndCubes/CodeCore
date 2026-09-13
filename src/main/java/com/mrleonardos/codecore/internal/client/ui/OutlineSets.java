package com.mrleonardos.codecore.internal.client.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mrleonardos.codecore.api.client.ui.render.OutlineBox;
import com.mrleonardos.codecore.api.client.ui.render.OutlineStyle;

/**
 * Наборы боксов по ключам и отбор видимых.
 *
 * <p>
 * Без классов игры нарочно: отбор по миру и дальности это единственное, что отделяет сотню выданных
 * коробок от трёх нарисованных, и проверять его на живом клиенте глазами было бы нечем.
 *
 * <p>
 * Порядок ключей сохраняется: два набора с прозрачными рамками ложатся друг на друга одинаково от кадра
 * к кадру, иначе перекрытие мерцало бы.
 */
public final class OutlineSets {

    private final Map<String, Layer> sets = new LinkedHashMap<>();

    /** Заменить набор под ключом. Пустой список снимает набор. */
    public void show(String key, OutlineStyle style, List<OutlineBox> boxes) {
        if (key == null || key.isEmpty() || style == null) {
            throw new IllegalArgumentException("Outline set needs a key and a style");
        }
        if (boxes == null || boxes.isEmpty()) {
            sets.remove(key);
            return;
        }
        sets.put(key, new Layer(style, new ArrayList<>(boxes)));
    }

    public void hide(String key) {
        sets.remove(key);
    }

    public boolean shown(String key) {
        return sets.containsKey(key);
    }

    public void clear() {
        sets.clear();
    }

    /** Сколько наборов держится сейчас. */
    public int size() {
        return sets.size();
    }

    /**
     * Обойти то, что видно отсюда.
     *
     * @param maxDistance дальность в блоках; ноль и меньше снимают отсечение
     */
    public void visible(int dimension, double camX, double camZ, int maxDistance, Visitor visitor) {
        for (Layer layer : sets.values()) {
            for (OutlineBox box : layer.boxes) {
                if (box.dimension() == dimension && near(box, camX, camZ, maxDistance)) {
                    visitor.box(layer.style, box);
                }
            }
        }
    }

    private static boolean near(OutlineBox box, double camX, double camZ, int maxDistance) {
        if (maxDistance <= 0) {
            return true;
        }
        double dx = box.centerX() - camX;
        double dz = box.centerZ() - camZ;
        double reach = maxDistance + box.reach();
        return dx * dx + dz * dz <= reach * reach;
    }

    /** Кому уходят видимые коробки. */
    public interface Visitor {

        void box(OutlineStyle style, OutlineBox box);
    }

    private static final class Layer {

        private final OutlineStyle style;
        private final List<OutlineBox> boxes;

        private Layer(OutlineStyle style, List<OutlineBox> boxes) {
            this.style = style;
            this.boxes = boxes;
        }
    }
}
