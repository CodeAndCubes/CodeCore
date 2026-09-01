package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigSpec;

/**
 * Файл настроек в json.
 *
 * <p>
 * Запись атомарная: сначала во временный файл, потом подмена, поэтому прерванное сохранение не оставит
 * от настроек половину. Файл, который не удалось разобрать, откладывается рядом с суффиксом
 * {@code .broken}, а работа продолжается со значениями по умолчанию: сервер не должен падать из-за
 * пропущенной запятой.
 *
 * <p>
 * Если цепочка миграций оборвалась на полпути, файл не переписывается вовсе, а рядом остаётся копия
 * {@code .broken}. Иначе обновление со схемы 1 сразу на 5 при одном объявленном шаге стёрло бы всё, чего
 * текущий класс не знает.
 */
public final class JsonConfigFile<T> implements ConfigFile<T> {

    private final ConfigSpec<T> spec;
    private final ConfigPaths paths;
    private final Logger log;

    private T value;
    private Path path;

    JsonConfigFile(ConfigSpec<T> spec, ConfigPaths paths, Logger log) {
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
        JsonObject data = GsonFactory.gson()
            .toJsonTree(value)
            .getAsJsonObject();
        data.addProperty(ConfigKeys.SCHEMA_VERSION, spec.schemaVersion());
        write(data);
    }

    /** Прочитать файл или создать его со значениями по умолчанию. */
    void load() {
        path = paths.resolve(spec);
        if (!Files.isRegularFile(path)) {
            value = spec.defaults()
                .get();
            save();
            log.info("Created config {} at {}", describe(), path);
            return;
        }

        JsonObject data = read();
        if (data == null) {
            quarantine();
            value = spec.defaults()
                .get();
            save();
            return;
        }

        MigrationOutcome outcome = MigrationRunner.run(data, spec.migrations(), spec.schemaVersion(), describe(), log);
        value = parse(data);
        if (value == null) {
            quarantine();
            value = spec.defaults()
                .get();
            save();
            return;
        }
        if (outcome == MigrationOutcome.INCOMPLETE) {
            keepCopy();
            return;
        }
        if (outcome == MigrationOutcome.MIGRATED) {
            save();
        }
    }

    /** Забыть содержимое: используется, когда выгружается мир. */
    void unload() {
        value = null;
        path = null;
    }

    private JsonObject read() {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = new JsonParser().parse(reader);
            if (parsed == null || !parsed.isJsonObject()) {
                log.warn("Config {} is not a json object", describe());
                return null;
            }
            return parsed.getAsJsonObject();
        } catch (IOException | JsonParseException failure) {
            log.warn("Failed to read config {}: {}", describe(), failure.toString());
            return null;
        }
    }

    private T parse(JsonObject data) {
        try {
            T parsed = GsonFactory.gson()
                .fromJson(data, spec.type());
            if (parsed == null) {
                return null;
            }
            spec.validator()
                .accept(parsed);
            return parsed;
        } catch (JsonParseException failure) {
            log.warn("Failed to parse config {}: {}", describe(), failure.toString());
            return null;
        }
    }

    private void write(JsonObject data) {
        Path temporary = path.resolveSibling(
            path.getFileName()
                .toString() + ConfigKeys.TEMPORARY_SUFFIX);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GsonFactory.gson()
                    .toJson(data, writer);
            }
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException failure) {
            log.error("Failed to save config {}: {}", describe(), failure.toString());
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
        return spec.modid() + "/" + spec.name();
    }
}
