package com.mrleonardos.codecore.internal.client.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.mrleonardos.codecore.api.client.image.ImageLimits;

/**
 * Разбирает байты в картинку.
 *
 * <p>
 * Размер читается из заголовка до самого разбора: файл в пару сотен килобайт может разворачиваться в
 * сотни мегапикселей, и узнавать об этом уже после выделения памяти поздно.
 *
 * <p>
 * Набор форматов зависит от того, какие декодеры есть в сборке: png, jpeg и gif идут в самой Java, webp —
 * только если рядом лежит его декодер.
 */
final class ImageDecoder {

    private ImageDecoder() {}

    static BufferedImage decode(byte[] bytes) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (stream == null) {
                throw new IOException("Cannot open image stream");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("Unsupported image format");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                int index = reader.getMinIndex();
                long pixels = (long) reader.getWidth(index) * reader.getHeight(index);
                if (pixels > ImageLimits.current()
                    .maxSourcePixels()) {
                    throw new IOException("Image is too large: " + pixels + " pixels");
                }
                return reader.read(index);
            } finally {
                reader.dispose();
            }
        }
    }

    /** Форматы, которые эта сборка действительно умеет читать. */
    static Set<String> formats() {
        ImageFormats.register();
        Set<String> names = new LinkedHashSet<>();
        for (String name : ImageIO.getReaderFormatNames()) {
            names.add(name.toLowerCase(Locale.ROOT));
        }
        return names;
    }
}
