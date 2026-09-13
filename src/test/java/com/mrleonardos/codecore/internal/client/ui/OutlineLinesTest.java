package com.mrleonardos.codecore.internal.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.ui.render.OutlineBox;

class OutlineLinesTest {

    private static final int EDGES = 12;

    @Test
    @DisplayName("без сетки коробка это двенадцать рёбер")
    void plainBoxIsTwelveEdges() {
        assertEquals(EDGES, count(OutlineBox.of(0, 0, 60, 0, 15, 70, 15), 0));
    }

    @Test
    @DisplayName("коробка из одного блока рисуется целиком, а не точкой")
    void singleBlockKeepsItsVolume() {
        List<double[]> lines = lines(OutlineBox.of(0, 7, 64, 7, 7, 64, 7), 0);

        assertEquals(EDGES, lines.size());
        assertTrue(
            lines.stream()
                .anyMatch(line -> line[0] == 7D && line[3] == 8D),
            "ребро длиной в блок не найдено");
    }

    @Test
    @DisplayName("сетка добавляет линии по четырём стенам")
    void gridFillsTheWalls() {
        int plain = count(OutlineBox.of(0, 0, 0, 0, 63, 63, 63), 0);
        int withGrid = count(OutlineBox.of(0, 0, 0, 0, 63, 63, 63), 16);

        assertEquals(EDGES, plain);
        assertEquals(EDGES + 3 * 2 + 3 * 2 + 3 * 4, withGrid);
    }

    @Test
    @DisplayName("шаг сетки растёт вместе с коробкой")
    void stepGrowsWithTheBox() {
        assertEquals(16, OutlineLines.step(256, 16));
        assertEquals(16, OutlineLines.step(768, 16));
        assertEquals(32, OutlineLines.step(1024, 16));
        assertEquals(64, OutlineLines.step(3000, 16));
    }

    @Test
    @DisplayName("коробка на десять тысяч блоков не рисует десять тысяч линий")
    void hugeBoxStaysCheap() {
        assertTrue(count(OutlineBox.of(0, 0, 0, 0, 9999, 255, 9999), 16) < 400);
    }

    private static int count(OutlineBox box, int grid) {
        return lines(box, grid).size();
    }

    private static List<double[]> lines(OutlineBox box, int grid) {
        List<double[]> lines = new ArrayList<>();
        OutlineLines.of(box, grid, (x1, y1, z1, x2, y2, z2) -> lines.add(new double[] { x1, y1, z1, x2, y2, z2 }));
        return lines;
    }
}
