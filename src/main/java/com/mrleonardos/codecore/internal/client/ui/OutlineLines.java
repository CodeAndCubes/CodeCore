package com.mrleonardos.codecore.internal.client.ui;

import com.mrleonardos.codecore.api.client.ui.render.OutlineBox;

/**
 * Из коробки в отрезки.
 *
 * <p>
 * Счёт отрезков вынесен из рисования нарочно: здесь нет ни OpenGL, ни классов игры, поэтому геометрию
 * можно проверить тестом, а не глазами на живом клиенте.
 *
 * <p>
 * Отрезки отдаются приёмнику по одному и нигде не копятся: рисование идёт каждый кадр, и список на сотню
 * коробок означал бы сотню списков в секунду на ровном месте.
 */
public final class OutlineLines {

    /** Больше этого числа шагов сетка не даёт: иначе коробка на тысячу блоков нарисует тысячу линий. */
    static final int MAX_STEPS = 48;

    private OutlineLines() {}

    /**
     * Разложить коробку на отрезки.
     *
     * @param grid шаг сетки в блоках; ноль оставляет двенадцать рёбер
     */
    public static void of(OutlineBox box, int grid, Sink sink) {
        double minX = box.minX();
        double minY = box.minY();
        double minZ = box.minZ();
        double maxX = box.maxX() + 1D;
        double maxY = box.maxY() + 1D;
        double maxZ = box.maxZ() + 1D;

        edges(minX, minY, minZ, maxX, maxY, maxZ, sink);
        if (grid <= 0) {
            return;
        }
        walls(box, grid, minX, minY, minZ, maxX, maxY, maxZ, sink);
    }

    /**
     * Шаг, подогнанный под длину стороны.
     *
     * <p>
     * Удваивается, пока шагов больше {@link #MAX_STEPS}: сетка остаётся сеткой, а не сплошной заливкой,
     * и число линий не зависит от того, насколько велика коробка.
     */
    static int step(int length, int grid) {
        int step = Math.max(1, grid);
        while (length / step > MAX_STEPS) {
            step *= 2;
        }
        return step;
    }

    private static void edges(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Sink sink) {
        sink.line(minX, minY, minZ, maxX, minY, minZ);
        sink.line(minX, minY, maxZ, maxX, minY, maxZ);
        sink.line(minX, maxY, minZ, maxX, maxY, minZ);
        sink.line(minX, maxY, maxZ, maxX, maxY, maxZ);

        sink.line(minX, minY, minZ, minX, minY, maxZ);
        sink.line(maxX, minY, minZ, maxX, minY, maxZ);
        sink.line(minX, maxY, minZ, minX, maxY, maxZ);
        sink.line(maxX, maxY, minZ, maxX, maxY, maxZ);

        sink.line(minX, minY, minZ, minX, maxY, minZ);
        sink.line(maxX, minY, minZ, maxX, maxY, minZ);
        sink.line(minX, minY, maxZ, minX, maxY, maxZ);
        sink.line(maxX, minY, maxZ, maxX, maxY, maxZ);
    }

    /** Сетка идёт по четырём стенам: пол и потолок закрыты землёй и небом, рисовать их незачем. */
    private static void walls(OutlineBox box, int grid, double minX, double minY, double minZ, double maxX, double maxY,
        double maxZ, Sink sink) {
        int alongX = step(box.maxX() - box.minX() + 1, grid);
        int alongZ = step(box.maxZ() - box.minZ() + 1, grid);
        int alongY = step(box.maxY() - box.minY() + 1, grid);

        for (double x = minX + alongX; x < maxX; x += alongX) {
            sink.line(x, minY, minZ, x, maxY, minZ);
            sink.line(x, minY, maxZ, x, maxY, maxZ);
        }
        for (double z = minZ + alongZ; z < maxZ; z += alongZ) {
            sink.line(minX, minY, z, minX, maxY, z);
            sink.line(maxX, minY, z, maxX, maxY, z);
        }
        for (double y = minY + alongY; y < maxY; y += alongY) {
            sink.line(minX, y, minZ, maxX, y, minZ);
            sink.line(minX, y, maxZ, maxX, y, maxZ);
            sink.line(minX, y, minZ, minX, y, maxZ);
            sink.line(maxX, y, minZ, maxX, y, maxZ);
        }
    }

    /** Куда уходят отрезки: тесселятор на клиенте, счётчик в тесте. */
    public interface Sink {

        void line(double x1, double y1, double z1, double x2, double y2, double z2);
    }
}
