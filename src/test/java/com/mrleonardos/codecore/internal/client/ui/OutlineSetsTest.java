package com.mrleonardos.codecore.internal.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.ui.render.OutlineBox;
import com.mrleonardos.codecore.api.client.ui.render.OutlineStyle;

class OutlineSetsTest {

    private static final int OVERWORLD = 0;
    private static final int NETHER = -1;
    private static final int DISTANCE = 128;

    @Test
    @DisplayName("повторный показ под тем же ключом заменяет набор, а не добавляет")
    void sameKeyReplacesTheSet() {
        OutlineSets sets = new OutlineSets();

        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.singletonList(near()));
        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.singletonList(near()));

        assertEquals(1, sets.size());
        assertEquals(1, visible(sets, OVERWORLD));
    }

    @Test
    @DisplayName("пустой список снимает набор")
    void emptyListHidesTheSet() {
        OutlineSets sets = new OutlineSets();

        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.singletonList(near()));
        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.emptyList());

        assertFalse(sets.shown("coderegions:selection"));
    }

    @Test
    @DisplayName("неизвестный ключ снимается молча")
    void unknownKeyIsNotAnError() {
        OutlineSets sets = new OutlineSets();

        sets.hide("нет такого");

        assertEquals(0, sets.size());
    }

    @Test
    @DisplayName("наборы двух модов рисуются вместе")
    void twoModsShareTheOutline() {
        OutlineSets sets = new OutlineSets();

        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.singletonList(near()));
        sets.show("codeworldedit:brush", OutlineStyle.BORDER, Collections.singletonList(near()));

        assertEquals(2, visible(sets, OVERWORLD));
    }

    @Test
    @DisplayName("из сотни коробок рисуются только близкие")
    void farBoxesAreSkipped() {
        List<OutlineBox> boxes = new ArrayList<>();
        boxes.add(OutlineBox.of(OVERWORLD, 0, 60, 0, 5, 70, 5));
        boxes.add(OutlineBox.of(OVERWORLD, 40, 60, 40, 45, 70, 45));
        boxes.add(OutlineBox.of(OVERWORLD, -60, 60, 20, -55, 70, 25));
        for (int far = 0; far < 97; far++) {
            int x = 4000 + far * 100;
            boxes.add(OutlineBox.of(OVERWORLD, x, 60, x, x + 5, 70, x + 5));
        }
        OutlineSets sets = new OutlineSets();
        sets.show("coderegions:claims", OutlineStyle.BORDER, boxes);

        assertEquals(3, visible(sets, OVERWORLD));
    }

    @Test
    @DisplayName("коробка чужого мира не рисуется")
    void anotherWorldIsSkipped() {
        OutlineSets sets = new OutlineSets();
        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.singletonList(near()));

        assertEquals(0, visible(sets, NETHER));
    }

    @Test
    @DisplayName("огромная коробка видна, пока в ней стоишь")
    void hugeBoxIsVisibleFromInside() {
        OutlineSets sets = new OutlineSets();
        sets.show(
            "coderegions:claims",
            OutlineStyle.BORDER,
            Collections.singletonList(OutlineBox.of(OVERWORLD, -2000, 0, -2000, 2000, 255, 2000)));

        assertEquals(1, visible(sets, OVERWORLD));
    }

    @Test
    @DisplayName("нулевая дальность снимает отсечение")
    void zeroDistanceDrawsEverything() {
        OutlineSets sets = new OutlineSets();
        sets.show(
            "coderegions:claims",
            OutlineStyle.BORDER,
            Collections.singletonList(OutlineBox.of(OVERWORLD, 90000, 60, 90000, 90005, 70, 90005)));

        int[] seen = new int[1];
        sets.visible(OVERWORLD, 0D, 0D, 0, (style, box) -> seen[0]++);

        assertEquals(1, seen[0]);
    }

    @Test
    @DisplayName("все наборы снимаются разом")
    void clearDropsEverything() {
        OutlineSets sets = new OutlineSets();
        sets.show("coderegions:selection", OutlineStyle.SELECTION, Collections.singletonList(near()));
        sets.show("codeworldedit:brush", OutlineStyle.BORDER, Collections.singletonList(near()));

        sets.clear();

        assertEquals(0, sets.size());
        assertFalse(sets.shown("coderegions:selection"));
    }

    private static OutlineBox near() {
        return OutlineBox.of(OVERWORLD, 0, 60, 0, 5, 70, 5);
    }

    private static int visible(OutlineSets sets, int dimension) {
        int[] seen = new int[1];
        sets.visible(dimension, 0D, 0D, DISTANCE, (style, box) -> seen[0]++);
        return seen[0];
    }
}
