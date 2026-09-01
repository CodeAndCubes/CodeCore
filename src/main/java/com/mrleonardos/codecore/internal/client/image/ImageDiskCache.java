package com.mrleonardos.codecore.internal.client.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import com.mrleonardos.codecore.api.client.image.ImageRequest;

/**
 * Готовые картинки на диске.
 *
 * <p>
 * Хранится результат обработки, а не исходник: он уже нужного размера, всегда png и читается мгновенно.
 * Имя файла — хэш от источника вместе с размером, поэтому один и тот же аватар в двух размерах не путается
 * сам с собой.
 */
final class ImageDiskCache {

    private static final String DIGEST = "SHA-256";
    private static final String FORMAT = "png";
    private static final String EXTENSION = ".png";
    private static final int NAME_LENGTH = 32;
    private static final int HEX = 16;

    private final Path directory;

    ImageDiskCache(Path directory) {
        this.directory = directory;
    }

    BufferedImage read(ImageRequest request) {
        Path file = fileFor(request);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return ImageIO.read(file.toFile());
        } catch (IOException unreadable) {
            return null;
        }
    }

    void write(ImageRequest request, BufferedImage image) throws IOException {
        Files.createDirectories(directory);
        Path file = fileFor(request);
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        ImageIO.write(image, FORMAT, temporary.toFile());
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path fileFor(ImageRequest request) {
        return directory.resolve(name(request.toString()) + EXTENSION);
    }

    private static String name(String key) {
        try {
            byte[] hash = MessageDigest.getInstance(DIGEST)
                .digest(key.getBytes(StandardCharsets.UTF_8));
            String hex = new BigInteger(1, hash).toString(HEX);
            return hex.length() > NAME_LENGTH ? hex.substring(0, NAME_LENGTH) : hex;
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required to cache images", impossible);
        }
    }
}
