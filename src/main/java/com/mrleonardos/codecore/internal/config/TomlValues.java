package com.mrleonardos.codecore.internal.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Перевод значений между деревом toml и деревом gson.
 *
 * <p>
 * Разбор в объект и обратно идёт через gson: он один и тот же для обоих форматов, поэтому карта групп,
 * список правил и перечисление ложатся на класс одинаково, чем бы ни был файл.
 */
final class TomlValues {

    private static final char DECIMAL_POINT = '.';

    private TomlValues() {}

    static JsonObject toJson(UnmodifiableConfig config) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Object> entry : config.valueMap()
            .entrySet()) {
            json.add(entry.getKey(), toJson(entry.getValue()));
        }
        return json;
    }

    static JsonElement toJson(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof UnmodifiableConfig) {
            return toJson((UnmodifiableConfig) value);
        }
        if (value instanceof List) {
            JsonArray array = new JsonArray();
            for (Object element : (List<?>) value) {
                array.add(toJson(element));
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

    static Object fromJson(JsonElement value, Config owner) {
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (value.isJsonObject()) {
            CommentedConfig table = subConfig(owner);
            for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject()
                .entrySet()) {
                Object converted = fromJson(entry.getValue(), owner);
                if (converted != null) {
                    table.set(entry.getKey(), converted);
                }
            }
            return table;
        }
        if (value.isJsonArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonElement element : value.getAsJsonArray()) {
                Object converted = fromJson(element, owner);
                if (converted != null) {
                    values.add(converted);
                }
            }
            return values;
        }

        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        return number(primitive.getAsNumber());
    }

    static CommentedConfig subConfig(Config owner) {
        Config created = owner.createSubConfig();
        return created instanceof CommentedConfig ? (CommentedConfig) created : CommentedConfig.fake(created);
    }

    /**
     * Число в том виде, в каком его понимает toml.
     *
     * <p>
     * Gson отдаёт число либо готовым {@link Number}, либо отложенным разбором строки. Дробное от целого
     * отличается точкой и показателем степени: без этого целое поле уехало бы в файл как {@code 3.0}.
     */
    private static Object number(Number value) {
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return value.longValue();
        }
        if (value instanceof Double || value instanceof Float) {
            return value.doubleValue();
        }
        String text = value.toString();
        if (text.indexOf(DECIMAL_POINT) >= 0 || text.indexOf('e') >= 0 || text.indexOf('E') >= 0) {
            return value.doubleValue();
        }
        return value.longValue();
    }
}
