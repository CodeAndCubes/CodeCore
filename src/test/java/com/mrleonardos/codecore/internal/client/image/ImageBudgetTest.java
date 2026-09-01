package com.mrleonardos.codecore.internal.client.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.internal.config.ImagesSection;

class ImageBudgetTest {

    private static final Logger LOG = LogManager.getLogger(ImageBudgetTest.class);

    @Test
    @DisplayName("заводская секция даёт заводские пределы")
    void factorySectionGivesFactoryLimits() {
        ImageLimits limits = ImageBudget.of(new ImagesSection(), LOG);

        assertEquals(ImageLimits.MAX_BYTES, limits.maxBytes());
        assertEquals(ImageLimits.MAX_SOURCE_PIXELS, limits.maxSourcePixels());
        assertEquals(ImageLimits.MAX_SIZE, limits.maxSize());
        assertEquals(ImageLimits.MAX_HANDLES, limits.maxHandles());
        assertEquals(ImageLimits.CONNECT_TIMEOUT_MS, limits.connectTimeoutMs());
        assertEquals(ImageLimits.READ_TIMEOUT_MS, limits.readTimeoutMs());
    }

    @Test
    @DisplayName("настройка опускает потолок")
    void settingLowersTheCeiling() {
        ImagesSection section = new ImagesSection();
        section.maxBytes = 64 * 1024;
        section.maxSize = 128;
        section.maxHandles = 32;
        section.connectTimeoutMs = 1000;

        ImageLimits limits = ImageBudget.of(section, LOG);

        assertEquals(64 * 1024, limits.maxBytes());
        assertEquals(128, limits.maxSize());
        assertEquals(32, limits.maxHandles());
        assertEquals(1000, limits.connectTimeoutMs());
    }

    @Test
    @DisplayName("поднять потолок настройка не может")
    void settingCannotRaiseTheCeiling() {
        ImagesSection section = new ImagesSection();
        section.maxBytes = ImageLimits.MAX_BYTES * 4;
        section.maxSourcePixels = Integer.MAX_VALUE;
        section.maxSize = 4096;
        section.maxHandles = 100000;
        section.readTimeoutMs = 600000;

        ImageLimits limits = ImageBudget.of(section, LOG);

        assertEquals(ImageLimits.MAX_BYTES, limits.maxBytes());
        assertEquals(ImageLimits.MAX_SOURCE_PIXELS, limits.maxSourcePixels());
        assertEquals(ImageLimits.MAX_SIZE, limits.maxSize());
        assertEquals(ImageLimits.MAX_HANDLES, limits.maxHandles());
        assertEquals(ImageLimits.READ_TIMEOUT_MS, limits.readTimeoutMs());
    }

    @Test
    @DisplayName("значение меньше единицы считается незаданным")
    void valueBelowOneReadsAsUnset() {
        ImagesSection section = new ImagesSection();
        section.maxBytes = 0;
        section.maxSize = -1;

        ImageLimits limits = ImageBudget.of(section, LOG);

        assertEquals(ImageLimits.MAX_BYTES, limits.maxBytes());
        assertEquals(ImageLimits.MAX_SIZE, limits.maxSize());
    }
}
