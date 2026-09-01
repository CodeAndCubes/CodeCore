package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.JsonObject;
import com.mrleonardos.codecore.api.config.ConfigData;

/**
 * Файл настроек в toml.
 *
 * <p>
 * Содержимое читается в дерево вместе с комментариями и живёт в памяти до записи: значения на него
 * накладываются, а не подменяют его целиком. Дерево держит порядок ключей файла, потому что разбор идёт
 * в подставленную упорядоченную карту, а не в ту, что заводит парсер сам по себе.
 */
final class TomlDocument implements ConfigDocument {

    private static final String INDENT = "";
    private static final String NEWLINE = "\n";

    private final CommentedConfig config;

    private TomlDocument(CommentedConfig config) {
        this.config = config;
    }

    static TomlDocument empty() {
        return new TomlDocument(TomlFormat.newConfig(LinkedHashMap::new));
    }

    static TomlDocument read(Path path) throws IOException {
        CommentedConfig config = TomlFormat.newConfig(LinkedHashMap::new);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            new TomlParser().parse(reader, config, ParsingMode.REPLACE);
        }
        return new TomlDocument(config);
    }

    CommentedConfig config() {
        return config;
    }

    @Override
    public ConfigData data() {
        return new TomlData(config);
    }

    @Override
    public <T> T bind(Class<T> type) {
        return GsonFactory.gson()
            .fromJson(TomlValues.toJson(config), type);
    }

    @Override
    public void store(Object value, Class<?> type, int schemaVersion) {
        config.set(Collections.singletonList(ConfigKeys.SCHEMA_VERSION), (long) schemaVersion);
        TomlBinder.store(
            GsonFactory.gson()
                .toJsonTree(value)
                .getAsJsonObject(),
            config,
            type);
        TomlBinder.comment(config, ConfigKeys.SCHEMA_VERSION, TomlBinder.commentOf(type));
    }

    /** Наложить значения на вложенную таблицу: так собирается главный файл из секций. */
    void storeSection(String name, Object value, Class<?> type, String... comment) {
        CommentedConfig table = newTable();
        JsonObject values = GsonFactory.gson()
            .toJsonTree(value)
            .getAsJsonObject();
        TomlBinder.storeSection(values, existingTable(name), table, type);
        putTable(name, table);
        TomlBinder.comment(config, name, comment);
    }

    /** Вложенная таблица из файла или {@code null}, если её там нет. */
    CommentedConfig existingTable(String name) {
        Object existing = config.getRaw(Collections.singletonList(name));
        return existing instanceof CommentedConfig ? (CommentedConfig) existing : null;
    }

    /** Пустая таблица того же формата. */
    CommentedConfig newTable() {
        return TomlValues.subConfig(config);
    }

    /** Положить таблицу на её место в файле. */
    void putTable(String name, CommentedConfig table) {
        config.set(Collections.singletonList(name), table);
    }

    /** Прочитать вложенную таблицу в класс настроек или {@code null}, если её в файле нет. */
    <T> T bindSection(String name, Class<T> type) {
        Object existing = config.getRaw(Collections.singletonList(name));
        if (!(existing instanceof CommentedConfig)) {
            return null;
        }
        return GsonFactory.gson()
            .fromJson(TomlValues.toJson((CommentedConfig) existing), type);
    }

    @Override
    public List<String> unknownKeys(Class<?> type) {
        List<String> found = new ArrayList<>();
        TomlBinder.unknown(config, type, "", found);
        return found;
    }

    @Override
    public void writeTo(Writer writer) throws IOException {
        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.setIndent(INDENT);
        tomlWriter.setNewline(NEWLINE);
        StringWriter text = new StringWriter();
        tomlWriter.write(config, text);
        writer.write(TomlText.spaceSections(text.toString()));
    }
}
