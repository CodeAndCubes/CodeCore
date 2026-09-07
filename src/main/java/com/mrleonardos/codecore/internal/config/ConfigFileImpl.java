package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigSpec;

/**
 * Открытый файл настроек.
 *
 * <p>
 * Файл, который не удалось разобрать, откладывается рядом с суффиксом {@code .broken}, а работа
 * продолжается со значениями по умолчанию: сервер не должен падать из-за пропущенной запятой.
 *
 * <p>
 * Если цепочка миграций оборвалась на полпути, файл не переписывается вовсе, а рядом остаётся копия
 * {@code .broken}. Иначе обновление со схемы 1 сразу на 5 при одном объявленном шаге стёрло бы всё, чего
 * текущий класс не знает.
 */
public final class ConfigFileImpl<T> implements ConfigFile<T> {

    private final ConfigSpec<T> spec;
    private final ConfigPaths paths;
    private final Logger log;

    private T value;
    private Path path;
    private ConfigDocument document;

    ConfigFileImpl(ConfigSpec<T> spec, ConfigPaths paths, Logger log) {
        this.spec = spec;
        this.paths = paths;
        this.log = log;
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException(
                "Config " + describe() + " is not loaded yet; world state is only available while a world is loaded");
        }
        return value;
    }

    @Override
    public boolean loaded() {
        return value != null;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public void reload() {
        load();
    }

    @Override
    public void save() {
        if (value == null) {
            return;
        }
        if (document == null) {
            document = ConfigDocuments.empty(spec.format());
        }
        document.store(value, spec.type(), spec.schemaVersion());
        ConfigWriting.atomically(path, document::writeTo, describe(), log);
    }

    /** Прочитать файл или создать его со значениями по умолчанию. */
    void load() {
        path = paths.resolve(spec);
        if (!Files.isRegularFile(path)) {
            if (spec.foreign()) {
                unloadForeign();
                return;
            }
            createFromDefaults();
            log.info("Created config {} at {}", describe(), path);
            return;
        }

        ConfigDocument read = read();
        if (read == null) {
            if (spec.foreign()) {
                unloadForeign();
                ConfigHints.afterBadRead(spec.format(), log);
                return;
            }
            quarantine();
            ConfigHints.afterBadRead(spec.format(), log);
            createFromDefaults();
            return;
        }

        MigrationOutcome outcome = MigrationRunner
            .run(read.data(), spec.migrations(), spec.schemaVersion(), describe(), log);
        T parsed = parse(read);
        if (parsed == null) {
            if (spec.foreign()) {
                unloadForeign();
                return;
            }
            quarantine();
            createFromDefaults();
            return;
        }

        document = read;
        value = parsed;
        reportUnknownKeys();
        if (spec.foreign()) {
            return;
        }
        if (outcome == MigrationOutcome.INCOMPLETE) {
            keepCopy();
            return;
        }
        if (outcome == MigrationOutcome.MIGRATED) {
            save();
            return;
        }
        if (outcome == MigrationOutcome.UNCHANGED) {
            topUp();
        }
    }

    /**
     * Дописать в файл настройки, которых в нём ещё нет.
     *
     * <p>
     * Обновлённый мод приносит новые поля, а файл на диске остаётся прежним: он читается и, пока никто
     * ничего не менял, не переписывается. Админ в итоге не видит новых настроек вовсе и не может их
     * поменять, пока не удалит файл целиком, а работают они на заводских значениях. Поэтому после чтения
     * модель накладывается на прочитанное, и файл сохраняется, только если от этого он изменился.
     *
     * <p>
     * Сравниваются две отрисовки одного документа, а не отрисовка с текстом на диске: иначе любая мелочь
     * форматирования переписывала бы файл на каждом запуске. Значения человека при этом не трогаются, они
     * уже разобраны в модель; возвращается только то, чего в файле нет.
     */
    private void topUp() {
        String before = render();
        document.store(value, spec.type(), spec.schemaVersion());
        String after = render();
        if (before.equals(after)) {
            return;
        }
        ConfigWriting.atomically(path, document::writeTo, describe(), log);
        log.info("Config {} was topped up with settings this version added, existing values were kept", describe());
    }

    private String render() {
        StringWriter writer = new StringWriter();
        try {
            document.writeTo(writer);
        } catch (IOException failure) {
            throw new IllegalStateException("Failed to render config " + describe() + " in memory", failure);
        }
        return writer.toString();
    }

    /**
     * Чужой файл прочитать не вышло. Своего мы бы завели заново, а этот трогать нечем: пусть лежит
     * как есть, а спросивший увидит по {@link #loaded()}, что значений нет.
     */
    private void unloadForeign() {
        value = null;
        document = null;
    }

    /** Забыть содержимое: используется, когда выгружается мир. */
    void unload() {
        value = null;
        document = null;
        path = null;
    }

    private void createFromDefaults() {
        document = ConfigDocuments.empty(spec.format());
        value = spec.defaults()
            .get();
        save();
    }

    private ConfigDocument read() {
        try {
            return ConfigDocuments.read(spec.format(), path);
        } catch (IOException | RuntimeException failure) {
            log.warn("Failed to read config {}: {}", describe(), failure.toString());
            return null;
        }
    }

    private T parse(ConfigDocument read) {
        try {
            T parsed = read.bind(spec.type());
            if (parsed == null) {
                return null;
            }
            spec.validator()
                .accept(parsed);
            return parsed;
        } catch (RuntimeException failure) {
            log.warn("Failed to parse config {}: {}", describe(), failure.toString());
            return null;
        }
    }

    private void reportUnknownKeys() {
        List<String> unknown = document.unknownKeys(spec.type());
        if (!unknown.isEmpty()) {
            log.info("Config {} has keys this mod does not read, they are left as they are: {}", describe(), unknown);
        }
    }

    private void quarantine() {
        Path broken = brokenPath();
        try {
            Files.move(path, broken, StandardCopyOption.REPLACE_EXISTING);
            log.warn("Config {} was moved to {} and replaced with defaults", describe(), broken);
        } catch (IOException failure) {
            log.error("Failed to set aside broken config {}: {}", describe(), failure.toString());
        }
    }

    private void keepCopy() {
        Path broken = brokenPath();
        try {
            Files.copy(path, broken, StandardCopyOption.REPLACE_EXISTING);
            log.warn(
                "Config {} stopped short of schema version {}; the file is left as it was and copied to {}",
                describe(),
                spec.schemaVersion(),
                broken);
        } catch (IOException failure) {
            log.error("Failed to copy unmigrated config {}: {}", describe(), failure.toString());
        }
    }

    private Path brokenPath() {
        return path.resolveSibling(
            path.getFileName()
                .toString() + ConfigKeys.BROKEN_SUFFIX);
    }

    private String describe() {
        return spec.role() + "/" + ConfigPaths.fileName(spec);
    }
}
