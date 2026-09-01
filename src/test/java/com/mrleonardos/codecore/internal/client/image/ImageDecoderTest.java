package com.mrleonardos.codecore.internal.client.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.client.image.ImageFit;

class ImageDecoderTest {

    private static final int TARGET = 32;

    @Test
    @DisplayName("webp читается и доводится до нужного размера")
    void webpIsDecoded() throws IOException {
        BufferedImage decoded = ImageDecoder.decode(bytes("/sample.webp"));

        assertEquals(300, decoded.getWidth());
        assertEquals(200, decoded.getHeight());

        BufferedImage normalized = ImageNormalizer.normalize(decoded, TARGET, ImageFit.COVER);
        assertEquals(TARGET, normalized.getWidth());
    }

    @Test
    @DisplayName("jpeg читается тоже")
    void jpegIsDecoded() throws IOException {
        assertEquals(
            300,
            ImageDecoder.decode(bytes("/sample.jpg"))
                .getWidth());
    }

    @Test
    @DisplayName("webp числится среди поддерживаемых форматов")
    void webpIsListed() {
        assertTrue(
            ImageDecoder.formats()
                .contains("webp"));
    }

    @Test
    @DisplayName("непонятные байты не проходят за картинку")
    void garbageIsRejected() {
        assertThrows(IOException.class, () -> ImageDecoder.decode(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }));
    }

    @Test
    @DisplayName("png с прозрачностью не теряет альфу")
    void pngKeepsAlpha() throws IOException {
        BufferedImage source = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(source, "png", png);

        BufferedImage decoded = ImageDecoder.decode(png.toByteArray());

        assertEquals(0, decoded.getRGB(0, 0) >>> 24);
    }

    private static byte[] bytes(String resource) throws IOException {
        try (InputStream stream = ImageDecoderTest.class.getResourceAsStream(resource)) {
            ByteArrayOutputStream collected = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                collected.write(buffer, 0, read);
            }
            return collected.toByteArray();
        }
    }
}
