package com.mrleonardos.codecore.api.client.ui.render;

/**
 * Как выглядит рамка.
 *
 * <p>
 * Три заводских стиля покрывают всё, ради чего рамку показывают: что я сейчас выделяю, где чужая
 * граница, и что тут не так. Свой стиль собирается от любого из них.
 *
 * <p>
 * Сетка нужна не для красоты: без неё грань в сто блоков читается как линия без расстояния, и игрок не
 * видит, где именно проходит стена. Шаг в шестнадцать блоков совпадает с чанком, поэтому по сетке сразу
 * понятно, сколько чанков занято.
 */
public final class OutlineStyle {

    /** Шаг сетки по умолчанию: он же сторона чанка. */
    public static final int CHUNK_GRID = 16;

    /** Что я сейчас выделяю: голубая рамка с сеткой, видна сквозь блоки. */
    public static final OutlineStyle SELECTION = of(0xC040C0FF).withGrid(CHUNK_GRID)
        .throughBlocks(true);

    /** Граница владения: зелёная рамка без сетки, видна сквозь блоки. */
    public static final OutlineStyle BORDER = of(0xA040FF80).throughBlocks(true);

    /** Тут не так: красная рамка с сеткой и миганием. */
    public static final OutlineStyle ERROR = of(0xE0FF4040).withGrid(CHUNK_GRID)
        .throughBlocks(true)
        .blinkingEvery(700);

    private static final float DEFAULT_THICKNESS = 2F;

    private final int argb;
    private final float thickness;
    private final int grid;
    private final boolean throughBlocks;
    private final int blinkMillis;

    private OutlineStyle(int argb, float thickness, int grid, boolean throughBlocks, int blinkMillis) {
        this.argb = argb;
        this.thickness = thickness;
        this.grid = grid;
        this.throughBlocks = throughBlocks;
        this.blinkMillis = blinkMillis;
    }

    /** Сплошная рамка заданного цвета, без сетки, за блоками не видна. */
    public static OutlineStyle of(int argb) {
        return new OutlineStyle(argb, DEFAULT_THICKNESS, 0, false, 0);
    }

    /** Тот же стиль другим цветом: так соседняя граница отличается от своей, оставаясь такой же по виду. */
    public OutlineStyle withColor(int value) {
        return new OutlineStyle(value, thickness, grid, throughBlocks, blinkMillis);
    }

    public OutlineStyle withThickness(float pixels) {
        return new OutlineStyle(argb, Math.max(1F, pixels), grid, throughBlocks, blinkMillis);
    }

    /** Шаг сетки в блоках; ноль оставляет одну рамку по краям. */
    public OutlineStyle withGrid(int blocks) {
        return new OutlineStyle(argb, thickness, Math.max(0, blocks), throughBlocks, blinkMillis);
    }

    public OutlineStyle throughBlocks(boolean visible) {
        return new OutlineStyle(argb, thickness, grid, visible, blinkMillis);
    }

    /** Полный цикл мигания в миллисекундах; ноль оставляет рамку ровной. */
    public OutlineStyle blinkingEvery(int millis) {
        return new OutlineStyle(argb, thickness, grid, throughBlocks, Math.max(0, millis));
    }

    public int argb() {
        return argb;
    }

    public float thickness() {
        return thickness;
    }

    public int grid() {
        return grid;
    }

    public boolean visibleThroughBlocks() {
        return throughBlocks;
    }

    public int blinkMillis() {
        return blinkMillis;
    }
}
