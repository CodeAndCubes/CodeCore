package com.mrleonardos.codecore.internal.client.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.api.client.image.ImageSource;
import com.mrleonardos.codecore.internal.client.LimitedStream;

/**
 * Достаёт байты картинки: из сети или с диска.
 *
 * <p>
 * Чтение обрывается, как только превышен предел объёма: заявленная в заголовке длина ничего не гарантирует,
 * а бесконечный ответ иначе съел бы память клиента.
 */
final class ImageFetcher {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT = "CodeCore";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT = "image/*";
    private static final int HTTP_OK = 200;

    private ImageFetcher() {}

    static byte[] fetch(ImageSource source) throws IOException {
        return source.remote() ? download(source.value()) : read(Paths.get(source.value()));
    }

    private static byte[] download(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(ImageLimits.CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(ImageLimits.READ_TIMEOUT_MS);
        connection.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);
        connection.setRequestProperty(ACCEPT_HEADER, ACCEPT);
        connection.setInstanceFollowRedirects(true);

        try {
            if (connection.getResponseCode() != HTTP_OK) {
                throw new IOException("Unexpected response " + connection.getResponseCode() + " from " + address);
            }
            if (connection.getContentLength() > ImageLimits.MAX_BYTES) {
                throw new IOException("Image at " + address + " declares " + connection.getContentLength() + " bytes");
            }
            try (InputStream stream = connection.getInputStream()) {
                return LimitedStream.readAll(stream, ImageLimits.MAX_BYTES, address);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static byte[] read(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("No image file at " + path);
        }
        if (Files.size(path) > ImageLimits.MAX_BYTES) {
            throw new IOException("Image file " + path + " is larger than " + ImageLimits.MAX_BYTES + " bytes");
        }
        return Files.readAllBytes(path);
    }
}
