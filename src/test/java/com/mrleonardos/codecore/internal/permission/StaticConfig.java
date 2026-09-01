package com.mrleonardos.codecore.internal.permission;

import java.nio.file.Path;

import com.mrleonardos.codecore.api.config.ConfigFile;

/** Готовые значения вместо файла на диске. */
public final class StaticConfig<T> implements ConfigFile<T> {

    private final T value;

    public StaticConfig(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean loaded() {
        return true;
    }

    @Override
    public void save() {}

    @Override
    public void reload() {}

    @Override
    public Path path() {
        return null;
    }
}
