package com.mrleonardos.codecore.api.client.ui.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutlineBoxTest {

    @Test
    @DisplayName("углы в любом порядке дают одну коробку")
    void cornersAreSorted() {
        OutlineBox straight = OutlineBox.of(0, 10, 60, -5, 20, 70, 5);
        OutlineBox swapped = OutlineBox.of(0, 20, 70, 5, 10, 60, -5);

        assertEquals(straight.minX(), swapped.minX());
        assertEquals(straight.minY(), swapped.minY());
        assertEquals(straight.minZ(), swapped.minZ());
        assertEquals(straight.maxX(), swapped.maxX());
        assertEquals(straight.maxY(), swapped.maxY());
        assertEquals(straight.maxZ(), swapped.maxZ());
    }

    @Test
    @DisplayName("коробка из одного блока занимает блок, а не ноль")
    void degenerateBoxKeepsTheBlock() {
        OutlineBox box = OutlineBox.of(0, 7, 64, 7, 7, 64, 7);

        assertEquals(7, box.minX());
        assertEquals(7, box.maxX());
        assertEquals(7.5D, box.centerX());
        assertEquals(7.5D, box.centerZ());
        assertEquals(0.5D, box.reach());
    }

    @Test
    @DisplayName("охват считается по большей стороне")
    void reachFollowsTheLongerSide() {
        OutlineBox box = OutlineBox.of(0, 0, 0, 0, 99, 10, 9);

        assertEquals(50D, box.reach());
    }
}
