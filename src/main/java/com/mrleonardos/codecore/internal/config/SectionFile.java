package com.mrleonardos.codecore.internal.config;

import java.nio.file.Path;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.SectionSpec;

/**
 * Секция главного файла в том же виде, в каком мод видит обычный файл настроек.
 *
 * <p>
 * Отдельного файла у секции нет: сохранение переписывает главный файл целиком, вместе с секциями всех
 * остальных модов.
 */
final class SectionFile<T> implements ConfigFile<T> {

    private final SectionSpec<T> spec;
    private final MainConfig owner;

    private T value;

    SectionFile(SectionSpec<T> spec, MainConfig owner) {
        this.spec = spec;
        this.owner = owner;
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("Section " + spec.name() + " of the main config is not loaded yet");
        }
        return value;
    }

    @Override
    public boolean loaded() {
        return value != null;
    }

    @Override
    public void save() {
        owner.write();
    }

    @Override
    public void reload() {
        owner.reload();
    }

    @Override
    public Path path() {
        return owner.path();
    }

    void bind(TomlDocument document) {
        T parsed = document == null ? null : document.bindSection(spec.name(), spec.type());
        if (parsed == null) {
            value = spec.defaults()
                .get();
            return;
        }
        spec.validator()
            .accept(parsed);
        value = parsed;
    }

    void store(TomlDocument document) {
        document.storeSection(spec.name(), value, spec.type(), TomlBinder.commentOf(spec.type()));
    }
}
