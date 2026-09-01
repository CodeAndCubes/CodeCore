package com.mrleonardos.codecore.internal.client.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.image.ImageFit;

class ImageNormalizerTest {

    private static final int TARGET = 64;

    @Test
    @DisplayName("картинка 4K сводится к нужному квадрату")
    void hugeImageBecomesSquare() {
        BufferedImage source = filled(3840, 2160, Color.RED);

        BufferedImage result = ImageNormalizer.normalize(source, TARGET, ImageFit.COVER);

        assertEquals(TARGET, result.getWidth());
        assertEquals(TARGET, result.getHeight());
    }

    @Test
    @DisplayName("маленькая картинка растягивается до нужного размера")
    void smallImageIsUpscaled() {
        BufferedImage result = ImageNormalizer.normalize(filled(16, 16, Color.BLUE), TARGET, ImageFit.COVER);

        assertEquals(TARGET, result.getWidth());
        assertEquals(TARGET, result.getHeight());
    }

    @Test
    @DisplayName("COVER режет по центру, а не по краю")
    void coverKeepsCenter() {
        BufferedImage source = filled(200, 100, Color.BLACK);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.GREEN);
        graphics.fillRect(50, 0, 100, 100);
        graphics.dispose();

        BufferedImage result = ImageNormalizer.normalize(source, TARGET, ImageFit.COVER);

        Color middle = new Color(result.getRGB(TARGET / 2, TARGET / 2), true);
        assertTrue(middle.getGreen() > middle.getRed(), "в середине должен остаться центр исходной картинки");
    }

    @Test
    @DisplayName("CONTAIN вписывает целиком и оставляет прозрачные поля")
    void containKeepsWholeImage() {
        BufferedImage result = ImageNormalizer.normalize(filled(200, 100, Color.RED), TARGET, ImageFit.CONTAIN);

        assertEquals(TARGET, result.getWidth());
        assertEquals(TARGET, result.getHeight());
        assertEquals(0, new Color(result.getRGB(1, 1), true).getAlpha(), "угол должен остаться пустым");
    }

    @Test
    @DisplayName("узкая полоса не ломает уменьшение")
    void extremeAspectRatioSurvives() {
        BufferedImage result = ImageNormalizer.normalize(filled(4000, 20, Color.RED), TARGET, ImageFit.COVER);

        assertEquals(TARGET, result.getWidth());
        assertEquals(TARGET, result.getHeight());
    }

    private static BufferedImage filled(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }
}
