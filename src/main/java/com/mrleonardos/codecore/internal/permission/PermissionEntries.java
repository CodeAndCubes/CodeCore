package com.mrleonardos.codecore.internal.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Чистка того, что пришло из файла прав.
 *
 * <p>
 * Gson кладёт {@code null} поверх инициализатора поля, поэтому строчка {@code "inherits": null} в
 * {@code permissions.json} до сих пор роняла первую же проверку права. Списки и карты после чистки
 * непустые ссылки, а мусорные элементы в них выброшены.
 */
final class PermissionEntries {

    private PermissionEntries() {}

    static List<String> texts(List<String> source) {
        List<String> cleaned = new ArrayList<>();
        if (source == null) {
            return cleaned;
        }
        for (String value : source) {
            if (value != null && !value.isEmpty()) {
                cleaned.add(value);
            }
        }
        return cleaned;
    }

    static Map<String, String> pairs(Map<String, String> source) {
        Map<String, String> cleaned = new LinkedHashMap<>();
        if (source == null) {
            return cleaned;
        }
        for (Map.Entry<String, String> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                cleaned.put(entry.getKey(), entry.getValue());
            }
        }
        return cleaned;
    }

    static <T> Map<String, T> named(Map<String, T> source, Consumer<T> normalize) {
        Map<String, T> cleaned = new LinkedHashMap<>();
        if (source == null) {
            return cleaned;
        }
        for (Map.Entry<String, T> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            normalize.accept(entry.getValue());
            cleaned.put(entry.getKey(), entry.getValue());
        }
        return cleaned;
    }
}
