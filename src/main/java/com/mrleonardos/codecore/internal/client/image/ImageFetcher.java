package com.mrleonardos.codecore.internal.client.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.api.client.image.ImageSource;
import com.mrleonardos.codecore.internal.client.HttpConnections;
import com.mrleonardos.codecore.internal.client.LimitedStream;

/**
 * Достаёт байты картинки: из сети или с диска.
 *
 * <p>
 * Чтение обрывается, как только превышен предел объёма: заявленная в заголовке длина ничего не гарантирует,
 * а бесконечный ответ иначе съел бы память клиента.
 */
final class ImageFetcher {

    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT = "image/*";

    private ImageFetcher() {}

    static byte[] fetch(ImageSource source) throws IOException {
        return source.remote() ? download(source.value()) : read(Paths.get(source.value()));
    }

    private static byte[] download(String address) throws IOException {
        ImageLimits limits = ImageLimits.current();
        HttpURLConnection connection = HttpConnections.open(address);
        connection.setRequestProperty(ACCEPT_HEADER, ACCEPT);
        connection.setInstanceFollowRedirects(true);

        try {
            if (connection.getResponseCode() != HttpConnections.HTTP_OK) {
                throw new IOException("Unexpected response " + connection.getResponseCode() + " from " + address);
            }
            if (connection.getContentLength() > limits.maxBytes()) {
                throw new IOException("Image at " + address + " declares " + connection.getContentLength() + " bytes");
            }
            try (InputStream stream = connection.getInputStream()) {
                return LimitedStream.readAll(stream, limits.maxBytes(), address);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static byte[] read(Path path) throws IOException {
        int maxBytes = ImageLimits.current()
            .maxBytes();
        if (!Files.isRegularFile(path)) {
            throw new IOException("No image file at " + path);
        }
        if (Files.size(path) > maxBytes) {
            throw new IOException("Image file " + path + " is larger than " + maxBytes + " bytes");
        }
        return Files.readAllBytes(path);
    }
}
