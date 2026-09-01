package com.mrleonardos.codecore.internal.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

/**
 * Поля класса настроек в том же наборе, который видит gson: только они считаются описанными, всё
 * остальное в файле принадлежит человеку.
 */
final class ConfigFields {

    private ConfigFields() {}

    static List<Field> of(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    /** Есть ли у типа перечень полей, по которому ядро знает, что в файле его, а что чужое. */
    static boolean described(Type type) {
        Class<?> raw = raw(type);
        if (raw == null || raw.isPrimitive() || raw.isEnum() || raw.isArray()) {
            return false;
        }
        if (raw == String.class || Number.class.isAssignableFrom(raw)
            || raw == Boolean.class
            || raw == Character.class) {
            return false;
        }
        if (Map.class.isAssignableFrom(raw) || Collection.class.isAssignableFrom(raw)
            || JsonElement.class.isAssignableFrom(raw)) {
            return false;
        }
        return !of(raw).isEmpty();
    }

    /** Тип значений карты, чтобы спуститься в её записи с их собственным перечнем полей. */
    static Type mapValueType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        Class<?> raw = raw(type);
        if (raw == null || !Map.class.isAssignableFrom(raw)) {
            return null;
        }
        Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
        return arguments.length == 2 ? arguments[1] : null;
    }

    static Class<?> raw(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return raw(((ParameterizedType) type).getRawType());
        }
        return null;
    }
}
