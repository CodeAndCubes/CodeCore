package com.mrleonardos.codecore.internal.config;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mrleonardos.codecore.api.config.Comment;

/**
 * Наложение значений на прочитанное содержимое файла.
 *
 * <p>
 * Свежее дерево не пишется никогда: ключ, которого нет в классе настроек, и строка, дописанная
 * человеком, остаются на месте. Правило одно: ядро трогает только те ключи, которые описаны полями, а
 * там, где описания нет (карта записей, сырое дерево), своими считаются все ключи сразу, поэтому
 * удалённая модом запись исчезает и с диска.
 *
 * <p>
 * Вложенные таблицы собираются заново, а корень правится на месте. Пересборка задаёт порядок: описанные
 * ключи идут в порядке полей класса, чужие ключи секции следом за ними и дальше остаются на месте. У
 * секции без перечня полей (карта записей) порядок берётся из прочитанного файла, поэтому карта, которую
 * мод не менял, ложится обратно строка в строку.
 */
final class TomlBinder {

    private static final String COMMENT_LINE_PREFIX = " ";
    private static final char COMMENT_LINE_SEPARATOR = '\n';

    private TomlBinder() {}

    static void store(JsonObject values, CommentedConfig target, Type type) {
        merge(values, target, target, type);
    }

    /** Собрать секцию заново, забрав из прежней её комментарии и чужие ключи. */
    static void storeSection(JsonObject values, CommentedConfig previous, CommentedConfig target, Type type) {
        merge(values, previous, target, type);
    }

    /** Перенести комментарий ключа из прежней таблицы в собранную заново. */
    static void carry(CommentedConfig previous, CommentedConfig target, String key) {
        carryComment(previous, target, key);
    }

    /** Строка комментария в том виде, в каком её ждёт night-config. */
    static String text(String... lines) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < lines.length; index++) {
            if (index > 0) {
                text.append(COMMENT_LINE_SEPARATOR);
            }
            if (!lines[index].isEmpty()) {
                text.append(COMMENT_LINE_PREFIX)
                    .append(lines[index]);
            }
        }
        return text.toString();
    }

    /** Описание класса секции: заголовок, который стоит над её первой строкой. */
    static String[] commentOf(Class<?> type) {
        Comment comment = type.getAnnotation(Comment.class);
        return comment == null ? new String[0] : comment.value();
    }

    static void comment(CommentedConfig target, String key, String... lines) {
        if (lines.length > 0) {
            target.setComment(path(key), text(lines));
        }
    }

    /** Ключи файла, которых нет в классе настроек: их мод не читает, но и не стирает. */
    static void unknown(UnmodifiableConfig config, Type type, String prefix, List<String> found) {
        if (!ConfigFields.described(type)) {
            return;
        }
        Map<String, Field> described = fieldsByName(ConfigFields.raw(type));
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            String key = entry.getKey();
            Field field = described.get(key);
            if (field == null) {
                if (!ConfigKeys.SCHEMA_VERSION.equals(key)) {
                    found.add(prefix + key);
                }
                continue;
            }
            Object value = entry.getRawValue();
            if (value instanceof UnmodifiableConfig) {
                unknown((UnmodifiableConfig) value, field.getGenericType(), prefix + key + ".", found);
            }
        }
    }

    private static void merge(JsonObject values, CommentedConfig previous, CommentedConfig target, Type type) {
        if (ConfigFields.described(type)) {
            mergeDescribed(values, previous, target, ConfigFields.raw(type));
            return;
        }
        mergeAll(values, previous, target, ConfigFields.mapValueType(type));
    }

    private static void mergeDescribed(JsonObject values, CommentedConfig previous, CommentedConfig target,
        Class<?> type) {
        boolean inPlace = previous == target;
        Set<String> described = new LinkedHashSet<>();

        for (Field field : ConfigFields.of(type)) {
            String key = field.getName();
            described.add(key);
            JsonElement value = values.get(key);
            if (value == null || value.isJsonNull()) {
                if (inPlace) {
                    target.remove(path(key));
                }
                continue;
            }
            if (!inPlace) {
                carryComment(previous, target, key);
            }
            put(previous, target, key, value, field.getGenericType());
            describe(target, key, field);
        }

        if (!inPlace) {
            carryRest(previous, target, described);
        }
    }

    private static void mergeAll(JsonObject values, CommentedConfig previous, CommentedConfig target, Type valueType) {
        boolean inPlace = previous == target;
        if (inPlace) {
            for (String key : keysOf(target)) {
                if (!values.has(key)) {
                    target.remove(path(key));
                }
            }
        }

        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value == null || value.isJsonNull()) {
                if (inPlace) {
                    target.remove(path(key));
                }
                continue;
            }
            if (!inPlace) {
                carryComment(previous, target, key);
            }
            put(previous, target, key, value, valueType);
        }
    }

    private static void put(CommentedConfig previous, CommentedConfig target, String key, JsonElement value,
        Type type) {
        if (!value.isJsonObject()) {
            target.set(path(key), TomlValues.fromJson(value, target));
            return;
        }

        CommentedConfig rebuilt = TomlValues.subConfig(target);
        merge(value.getAsJsonObject(), childOf(previous, key), rebuilt, type);
        target.set(path(key), rebuilt);
    }

    private static CommentedConfig childOf(CommentedConfig previous, String key) {
        if (previous == null) {
            return null;
        }
        Object existing = previous.getRaw(path(key));
        return existing instanceof Config ? commented((Config) existing) : null;
    }

    private static void carryComment(CommentedConfig previous, CommentedConfig target, String key) {
        if (previous == null) {
            return;
        }
        String comment = previous.getComment(path(key));
        if (comment != null) {
            target.setComment(path(key), comment);
        }
    }

    private static void carryRest(CommentedConfig previous, CommentedConfig target, Set<String> described) {
        if (previous == null) {
            return;
        }
        for (UnmodifiableConfig.Entry entry : previous.entrySet()) {
            if (described.contains(entry.getKey())) {
                continue;
            }
            Object value = entry.getRawValue();
            target.set(path(entry.getKey()), value);
            carryComment(previous, target, entry.getKey());
        }
    }

    /** Ключи таблицы отдельным списком: по нему идёт удаление, а карту под конфигом трогать нельзя. */
    private static List<String> keysOf(UnmodifiableConfig config) {
        List<String> keys = new ArrayList<>();
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    private static void describe(CommentedConfig target, String key, Field field) {
        Comment own = field.getAnnotation(Comment.class);
        if (own != null) {
            comment(target, key, own.value());
            return;
        }
        Class<?> raw = ConfigFields.raw(field.getGenericType());
        Comment inherited = raw == null ? null : raw.getAnnotation(Comment.class);
        if (inherited != null) {
            comment(target, key, inherited.value());
        }
    }

    private static Map<String, Field> fieldsByName(Class<?> type) {
        Map<String, Field> byName = new LinkedHashMap<>();
        for (Field field : ConfigFields.of(type)) {
            byName.put(field.getName(), field);
        }
        return byName;
    }

    private static CommentedConfig commented(Config config) {
        return config instanceof CommentedConfig ? (CommentedConfig) config : CommentedConfig.fake(config);
    }

    private static List<String> path(String key) {
        return Collections.singletonList(key);
    }
}
