package com.mrleonardos.codecore.api.client.ui.render;

import com.mrleonardos.codecore.api.client.image.ImageHandle;

/**
 * Поверхность рисования: всё, что мод делает с экраном, и ни одного типа игры.
 *
 * <p>
 * Берётся из {@code ClientApi.painter()}. За тринадцатью методами прячется то, что в 1.7.10 приходится
 * делать руками: {@code Tessellator.instance} с {@code startDrawingQuads}, вызовы {@code GL11},
 * {@code GL13} и {@code GL20}, {@code Minecraft.getMinecraft()}, {@code ScaledResolution},
 * {@code fontRenderer}, {@code getTextureManager().bindTexture}. Именно этот набор в следующих версиях
 * меняется целиком: в 1.8 тесселятор стал {@code WorldRenderer}, потом {@code BufferBuilder}, ручные
 * вызовы {@code GL11} уехали за {@code GlStateManager} и дальше за {@code RenderSystem}.
 *
 * <p>
 * Методы принимают и отдают только {@code int}, {@code float}, {@code String}, {@code boolean} и
 * {@link ImageHandle}. Координаты в пикселях интерфейса, цвет в {@code 0xAARRGGBB}.
 */
public interface Painter {

    /** Ширина строки в пикселях интерфейса. */
    int textWidth(String text);

    /** Высота строки шрифта. */
    int lineHeight();

    /** Написать строку без тени: тень рисующий код добавляет сам, если она ему нужна. */
    void text(String text, int x, int y, int argb);

    /** Залить прямоугольник цветом с альфой. */
    void rect(int left, int top, int right, int bottom, int argb);

    /** Нарисовать картинку целиком в заданный прямоугольник; неготовая ручка не рисует ничего. */
    void image(ImageHandle image, int x, int y, int width, int height, float alpha);

    /**
     * Нарисовать лицо игрока по нику.
     *
     * @return {@code false}, если скина рядом нет и рисовать было нечего
     */
    boolean head(String playerName, int x, int y, int size, float alpha);

    /**
     * Размыть то, что уже нарисовано под прямоугольником.
     *
     * @param strength радиус размытия в пикселях экрана
     * @return {@code false}, если размыть не вышло; тогда вызывающий рисует обычную подложку
     */
    boolean blur(int left, int top, int right, int bottom, float strength);

    /** Умеет ли эта видеокарта то, что нужно для размытия. */
    boolean blurAvailable();

    /** Ограничить рисование прямоугольником; обязательно закрывать вызовом {@link #endClip()}. */
    void clip(int left, int top, int right, int bottom);

    /** Снять ограничение области рисования. */
    void endClip();

    /** Запомнить текущее преобразование. */
    void push();

    /** Вернуть запомненное преобразование. */
    void pop();

    /** Сдвинуть начало координат. */
    void translate(int x, int y, int z);
}
