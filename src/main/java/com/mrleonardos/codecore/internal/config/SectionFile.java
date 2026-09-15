package com.mrleonardos.codecore.internal.config;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

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
    private final Logger log;

    private T value;

    SectionFile(SectionSpec<T> spec, MainConfig owner, Logger log) {
        this.spec = spec;
        this.owner = owner;
        this.log = log;
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
        try {
            spec.validator()
                .accept(parsed);
        } catch (RuntimeException failure) {
            log.warn(
                "Section {} of the main config was rejected by its validator, defaults are used: {}",
                spec.name(),
                failure.toString());
            value = spec.defaults()
                .get();
            return;
        }
        value = parsed;
    }

    void store(TomlDocument document) {
        document.storeSection(spec.name(), value, spec.type(), TomlBinder.commentOf(spec.type()));
    }
}
