package com.mrleonardos.codecore.internal.client.image;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.internal.config.ImagesSection;

/**
 * Пределы картинок из секции {@code [images]}, зажатые заводскими потолками.
 *
 * <p>
 * Потолок опускается, но не поднимается: настройка задаёт границу безопасности, а не отменяет её.
 * Значение меньше единицы считается незаданным, и тогда действует заводское.
 */
public final class ImageBudget {

    private static final int UNSET = 1;

    private ImageBudget() {}

    public static ImageLimits of(ImagesSection section, Logger log) {
        return new ImageLimits(
            lowered("maxBytes", section.maxBytes, ImageLimits.MAX_BYTES, log),
            lowered("maxSourcePixels", section.maxSourcePixels, ImageLimits.MAX_SOURCE_PIXELS, log),
            lowered("maxSize", section.maxSize, ImageLimits.MAX_SIZE, log),
            lowered("maxHandles", section.maxHandles, ImageLimits.MAX_HANDLES, log),
            lowered("connectTimeoutMs", section.connectTimeoutMs, ImageLimits.CONNECT_TIMEOUT_MS, log),
            lowered("readTimeoutMs", section.readTimeoutMs, ImageLimits.READ_TIMEOUT_MS, log));
    }

    private static int lowered(String key, int value, int ceiling, Logger log) {
        if (value < UNSET) {
            log.warn("Image limit {} is {}, which reads as unset; keeping the built-in {}", key, value, ceiling);
            return ceiling;
        }
        if (value > ceiling) {
            log.warn("Image limit {} of {} is above the built-in ceiling {}, keeping the ceiling", key, value, ceiling);
            return ceiling;
        }
        return value;
    }
}
