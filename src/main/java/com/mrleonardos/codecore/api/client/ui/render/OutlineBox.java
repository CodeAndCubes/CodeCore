package com.mrleonardos.codecore.api.client.ui.render;

/**
 * Коробка в мире, которую надо обвести.
 *
 * <p>
 * Углы задаются как угодно, внутри они приводятся к «меньший и больший»: игрок кликает по углам в любом
 * порядке, и заставлять вызывающего сортировать их значило бы заводить эту ошибку в каждом моде.
 *
 * <p>
 * Границы входят внутрь, как и везде в линейке: коробка из одного блока это блок с одинаковыми углами, а
 * рисуется она целиком, от его начала до конца.
 *
 * <p>
 * Мир зовётся номером измерения, а не названием: название у модовых миров какое угодно и повторяется
 * между сборками, номер уникален.
 */
public final class OutlineBox {

    private final int dimension;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private OutlineBox(int dimension, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.dimension = dimension;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /** Коробка по двум углам в любом порядке. */
    public static OutlineBox of(int dimension, int x1, int y1, int z1, int x2, int y2, int z2) {
        return new OutlineBox(
            dimension,
            Math.min(x1, x2),
            Math.min(y1, y2),
            Math.min(z1, z2),
            Math.max(x1, x2),
            Math.max(y1, y2),
            Math.max(z1, z2));
    }

    /** Номер измерения, в котором коробка имеет смысл. */
    public int dimension() {
        return dimension;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    /** Середина коробки по горизонтали: по ней считается дальность отсечения. */
    public double centerX() {
        return (minX + maxX + 1) / 2D;
    }

    public double centerZ() {
        return (minZ + maxZ + 1) / 2D;
    }

    /** Половина наибольшей стороны по горизонтали: чтобы огромная коробка не пропадала из-за середины. */
    public double reach() {
        return Math.max(maxX - minX + 1, maxZ - minZ + 1) / 2D;
    }
}
