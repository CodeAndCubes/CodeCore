package com.mrleonardos.codecore.internal.client.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.mrleonardos.codecore.api.client.image.ImageFit;

/**
 * Приводит картинку к квадрату нужной стороны.
 *
 * <p>
 * Аватары приходят какие угодно: и 64×64, и 4000×3000. Уменьшение делается ступенями по половине, иначе
 * прыжок с четырёх тысяч пикселей до шестнадцати даёт кашу вместо лица: обычная билинейная фильтрация
 * читает только четыре соседних пикселя и попросту не видит остальные.
 */
final class ImageNormalizer {

    private static final int MIN_STEP = 2;

    private ImageNormalizer() {}

    static BufferedImage normalize(BufferedImage source, int size, ImageFit fit) {
        BufferedImage squared = fit == ImageFit.COVER ? crop(source) : source;
        BufferedImage scaled = downscale(squared, size, fit);
        return fit == ImageFit.COVER ? scaled : center(scaled, size);
    }

    private static BufferedImage crop(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        return source.getSubimage(x, y, side, side);
    }

    private static BufferedImage downscale(BufferedImage source, int size, ImageFit fit) {
        int targetWidth = size;
        int targetHeight = size;
        if (fit == ImageFit.CONTAIN) {
            double ratio = Math.min((double) size / source.getWidth(), (double) size / source.getHeight());
            targetWidth = Math.max(1, (int) Math.round(source.getWidth() * ratio));
            targetHeight = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        }

        BufferedImage current = source;
        int width = source.getWidth();
        int height = source.getHeight();

        while (width / MIN_STEP > targetWidth && height / MIN_STEP > targetHeight) {
            width = Math.max(targetWidth, width / MIN_STEP);
            height = Math.max(targetHeight, height / MIN_STEP);
            current = resize(current, width, height);
        }
        return width == targetWidth && height == targetHeight ? current : resize(current, targetWidth, targetHeight);
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics
            .setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return target;
    }

    private static BufferedImage center(BufferedImage source, int size) {
        BufferedImage target = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.drawImage(source, (size - source.getWidth()) / 2, (size - source.getHeight()) / 2, null);
        graphics.dispose();
        return target;
    }
}
