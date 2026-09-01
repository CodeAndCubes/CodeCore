package com.mrleonardos.codecore.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrleonardos.codecore.api.config.ConfigData;

final class JsonData implements ConfigData {

    private static final char PATH_SEPARATOR = '.';

    private final JsonObject json;

    JsonData(JsonObject json) {
        this.json = json;
    }

    JsonObject json() {
        return json;
    }

    @Override
    public boolean has(String path) {
        return element(path) != null;
    }

    @Override
    public Object get(String path) {
        return value(element(path));
    }

    @Override
    public String string(String path, String fallback) {
        JsonElement found = element(path);
        return isString(found) ? found.getAsString() : fallback;
    }

    @Override
    public int integer(String path, int fallback) {
        JsonElement found = element(path);
        return isNumber(found) ? found.getAsInt() : fallback;
    }

    @Override
    public long number(String path, long fallback) {
        JsonElement found = element(path);
        return isNumber(found) ? found.getAsLong() : fallback;
    }

    @Override
    public boolean flag(String path, boolean fallback) {
        JsonElement found = element(path);
        return isBoolean(found) ? found.getAsBoolean() : fallback;
    }

    @Override
    public ConfigData table(String path) {
        JsonElement found = element(path);
        return found != null && found.isJsonObject() ? new JsonData(found.getAsJsonObject()) : null;
    }

    @Override
    public List<ConfigData> tables(String path) {
        JsonElement found = element(path);
        if (found == null || !found.isJsonArray()) {
            return Collections.emptyList();
        }
        List<ConfigData> tables = new ArrayList<>();
        for (JsonElement element : found.getAsJsonArray()) {
            if (element.isJsonObject()) {
                tables.add(new JsonData(element.getAsJsonObject()));
            }
        }
        return tables;
    }

    @Override
    public Set<String> keys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    @Override
    public void set(String path, Object value) {
        int split = path.lastIndexOf(PATH_SEPARATOR);
        if (split < 0) {
            json.add(path, element(value));
            return;
        }
        parent(path.substring(0, split), true).add(path.substring(split + 1), element(value));
    }

    @Override
    public void remove(String path) {
        int split = path.lastIndexOf(PATH_SEPARATOR);
        if (split < 0) {
            json.remove(path);
            return;
        }
        JsonObject owner = parent(path.substring(0, split), false);
        if (owner != null) {
            owner.remove(path.substring(split + 1));
        }
    }

    @Override
    public ConfigData newTable() {
        return new JsonData(new JsonObject());
    }

    private JsonElement element(String path) {
        int split = path.lastIndexOf(PATH_SEPARATOR);
        if (split < 0) {
            return json.get(path);
        }
        JsonObject owner = parent(path.substring(0, split), false);
        return owner == null ? null : owner.get(path.substring(split + 1));
    }

    private JsonObject parent(String path, boolean create) {
        JsonObject current = json;
        for (String part : path.split("\\.")) {
            JsonElement next = current.get(part);
            if (next == null || !next.isJsonObject()) {
                if (!create) {
                    return null;
                }
                next = new JsonObject();
                current.add(part, next);
            }
            current = next.getAsJsonObject();
        }
        return current;
    }

    private static Object value(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            return new JsonData(element.getAsJsonObject());
        }
        if (element.isJsonArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonElement item : element.getAsJsonArray()) {
                values.add(value(item));
            }
            return values;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            return primitive.getAsNumber();
        }
        return primitive.getAsString();
    }

    private static JsonElement element(Object value) {
        if (value == null) {
            return com.google.gson.JsonNull.INSTANCE;
        }
        if (value instanceof JsonData) {
            return ((JsonData) value).json;
        }
        if (value instanceof JsonElement) {
            return (JsonElement) value;
        }
        if (value instanceof List) {
            JsonArray array = new JsonArray();
            for (Object element : (List<?>) value) {
                array.add(element(element));
            }
            return array;
        }
        if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        }
        if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        }
        return new JsonPrimitive(String.valueOf(value));
    }

    private static boolean isString(JsonElement element) {
        return element != null && element.isJsonPrimitive()
            && element.getAsJsonPrimitive()
                .isString();
    }

    private static boolean isNumber(JsonElement element) {
        return element != null && element.isJsonPrimitive()
            && element.getAsJsonPrimitive()
                .isNumber();
    }

    private static boolean isBoolean(JsonElement element) {
        return element != null && element.isJsonPrimitive()
            && element.getAsJsonPrimitive()
                .isBoolean();
    }
}
