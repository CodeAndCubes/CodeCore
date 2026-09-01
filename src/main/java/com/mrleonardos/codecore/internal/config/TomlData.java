package com.mrleonardos.codecore.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.mrleonardos.codecore.api.config.ConfigData;

final class TomlData implements ConfigData {

    private final CommentedConfig config;

    TomlData(CommentedConfig config) {
        this.config = config;
    }

    CommentedConfig config() {
        return config;
    }

    @Override
    public boolean has(String path) {
        return config.contains(path);
    }

    @Override
    public Object get(String path) {
        return wrap(config.get(path));
    }

    @Override
    public String string(String path, String fallback) {
        Object value = config.get(path);
        return value instanceof String ? (String) value : fallback;
    }

    @Override
    public int integer(String path, int fallback) {
        Object value = config.get(path);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    @Override
    public long number(String path, long fallback) {
        Object value = config.get(path);
        return value instanceof Number ? ((Number) value).longValue() : fallback;
    }

    @Override
    public boolean flag(String path, boolean fallback) {
        Object value = config.get(path);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    @Override
    public ConfigData table(String path) {
        Object value = config.get(path);
        return value instanceof Config ? new TomlData(commented((Config) value)) : null;
    }

    @Override
    public List<ConfigData> tables(String path) {
        Object value = config.get(path);
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<ConfigData> tables = new ArrayList<>();
        for (Object element : (List<?>) value) {
            if (element instanceof Config) {
                tables.add(new TomlData(commented((Config) element)));
            }
        }
        return tables;
    }

    @Override
    public Set<String> keys() {
        return config.valueMap()
            .keySet();
    }

    @Override
    public void set(String path, Object value) {
        config.set(path, unwrap(value));
    }

    @Override
    public void remove(String path) {
        config.remove(path);
    }

    @Override
    public ConfigData newTable() {
        return new TomlData(config.createSubConfig());
    }

    private Object wrap(Object value) {
        if (value instanceof Config) {
            return new TomlData(commented((Config) value));
        }
        if (value instanceof List) {
            List<Object> wrapped = new ArrayList<>();
            for (Object element : (List<?>) value) {
                wrapped.add(wrap(element));
            }
            return wrapped;
        }
        return value;
    }

    private static Object unwrap(Object value) {
        if (value instanceof TomlData) {
            return ((TomlData) value).config;
        }
        if (value instanceof List) {
            List<Object> plain = new ArrayList<>();
            for (Object element : (List<?>) value) {
                plain.add(unwrap(element));
            }
            return plain;
        }
        return value;
    }

    private static CommentedConfig commented(Config config) {
        return config instanceof CommentedConfig ? (CommentedConfig) config : CommentedConfig.fake(config);
    }
}
