package com.mrleonardos.codecore.api.client.image;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Откуда берётся картинка.
 *
 * <p>
 * Либо адрес в сети, либо файл на диске игрока. Больше вариантов не нужно: всё остальное сводится
 * к одному из этих двух.
 */
public final class ImageSource {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private final String value;
    private final boolean remote;

    private ImageSource(String value, boolean remote) {
        this.value = value;
        this.remote = remote;
    }

    /**
     * Картинка по сетевому адресу.
     *
     * @throws IllegalArgumentException если это не {@code http} и не {@code https}
     */
    public static ImageSource url(String address) {
        if (!allowed(address)) {
            throw new IllegalArgumentException("Only http and https addresses are allowed: " + address);
        }
        return new ImageSource(address, true);
    }

    /** Годится ли адрес для сети: только {@code http} и {@code https}, ничего локального. */
    public static boolean allowed(String address) {
        if (address == null) {
            return false;
        }
        String lower = address.toLowerCase(Locale.ROOT);
        return lower.startsWith(HTTP) || lower.startsWith(HTTPS);
    }

    /** Картинка из файла. */
    public static ImageSource file(Path path) {
        return new ImageSource(
            path.toAbsolutePath()
                .toString(),
            false);
    }

    /** Адрес или путь, как они были переданы при создании. */
    public String value() {
        return value;
    }

    /** Нужна ли для получения сеть. */
    public boolean remote() {
        return remote;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ImageSource)) {
            return false;
        }
        ImageSource that = (ImageSource) other;
        return remote == that.remote && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode() * 31 + (remote ? 1 : 0);
    }

    @Override
    public String toString() {
        return value;
    }
}
