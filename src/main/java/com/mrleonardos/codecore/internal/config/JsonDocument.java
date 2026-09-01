package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mrleonardos.codecore.api.config.ConfigData;

/**
 * Файл машинных данных в json.
 *
 * <p>
 * Комментариев тут нет и следить за чужими ключами не за чем: файл пишет только мод, а десять тысяч
 * строк журнала разбираются заметно быстрее, чем то же самое в toml.
 */
final class JsonDocument implements ConfigDocument {

    private JsonObject json;

    private JsonDocument(JsonObject json) {
        this.json = json;
    }

    static JsonDocument empty() {
        return new JsonDocument(new JsonObject());
    }

    static JsonDocument read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = new JsonParser().parse(reader);
            if (parsed == null || !parsed.isJsonObject()) {
                throw new IOException("Config at " + path + " is not a json object");
            }
            return new JsonDocument(parsed.getAsJsonObject());
        }
    }

    @Override
    public ConfigData data() {
        return new JsonData(json);
    }

    @Override
    public <T> T bind(Class<T> type) {
        return GsonFactory.gson()
            .fromJson(json, type);
    }

    @Override
    public void store(Object value, Class<?> type, int schemaVersion) {
        json = GsonFactory.gson()
            .toJsonTree(value)
            .getAsJsonObject();
        json.addProperty(ConfigKeys.SCHEMA_VERSION, schemaVersion);
    }

    @Override
    public List<String> unknownKeys(Class<?> type) {
        return Collections.emptyList();
    }

    @Override
    public void writeTo(Writer writer) throws IOException {
        GsonFactory.gson()
            .toJson(json, writer);
    }
}
