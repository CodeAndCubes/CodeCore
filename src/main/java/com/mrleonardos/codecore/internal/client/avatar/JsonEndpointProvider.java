package com.mrleonardos.codecore.internal.client.avatar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mrleonardos.codecore.api.client.avatar.AvatarProvider;
import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.api.client.image.ImageSource;
import com.mrleonardos.codecore.internal.client.LimitedStream;

/**
 * Аватар через промежуточный запрос: сначала json, в нём адрес картинки.
 *
 * <p>
 * Так устроены почти все внешние профили: по нику отдаётся описание игрока, где ссылка лежит в поле.
 * Запрос уходит в фоновый поток, а рисующий код получает {@code null}, пока ответа нет:
 * ждать сеть в кадре нельзя.
 *
 * <p>
 * Ответ читается с тем же потолком, что и картинки: адрес приходит от сервера, и бесконечный поток json
 * с подконтрольного ему хоста иначе съел бы память клиента. Таймаут чтения тут не помогает, он считает
 * паузу между байтами.
 */
final class JsonEndpointProvider implements AvatarProvider {

    private static final String PATH_SEPARATOR = "\\.";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT = "CodeCore";
    private static final int HTTP_OK = 200;
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;

    private final Map<String, String> resolved = new ConcurrentHashMap<>();
    private final Map<String, Boolean> pending = new ConcurrentHashMap<>();
    private final String template;
    private final String jsonPath;
    private final Executor workers;
    private final Logger log;

    JsonEndpointProvider(String template, String jsonPath, Executor workers, Logger log) {
        this.template = template;
        this.jsonPath = jsonPath;
        this.workers = workers;
        this.log = log;
    }

    @Override
    public ImageSource sourceFor(UUID playerId, String playerName) {
        String key = playerId == null ? playerName : playerId.toString();
        if (key == null || key.isEmpty()) {
            return null;
        }

        String address = resolved.get(key);
        if (address != null) {
            return address.isEmpty() ? null : safeSource(address);
        }
        if (pending.putIfAbsent(key, Boolean.TRUE) == null) {
            workers.execute(() -> resolve(key, playerId, playerName));
        }
        return null;
    }

    private void resolve(String key, UUID playerId, String playerName) {
        try {
            String endpoint = AvatarPlaceholders.apply(template, playerId, playerName);
            resolved.put(key, ImageSource.allowed(endpoint) ? extract(request(endpoint)) : "");
        } catch (Exception failure) {
            log.warn("Avatar endpoint failed for {}: {}", key, failure.toString());
            resolved.put(key, "");
        } finally {
            pending.remove(key);
        }
    }

    private JsonElement request(String endpoint) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(ImageLimits.CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(ImageLimits.READ_TIMEOUT_MS);
        connection.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);
        try {
            if (connection.getResponseCode() != HTTP_OK) {
                throw new IOException("Unexpected response " + connection.getResponseCode() + " from " + endpoint);
            }
            try (InputStream stream = connection.getInputStream()) {
                byte[] body = LimitedStream.readAll(stream, MAX_RESPONSE_BYTES, endpoint);
                return new JsonParser().parse(new String(body, StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    private String extract(JsonElement root) {
        JsonElement current = root;
        for (String step : jsonPath.split(PATH_SEPARATOR)) {
            if (current == null || !current.isJsonObject()) {
                return "";
            }
            JsonObject object = current.getAsJsonObject();
            current = object.get(step);
        }
        return current != null && current.isJsonPrimitive() ? current.getAsString() : "";
    }

    private ImageSource safeSource(String address) {
        try {
            return ImageSource.url(address);
        } catch (IllegalArgumentException wrongScheme) {
            return null;
        }
    }
}
