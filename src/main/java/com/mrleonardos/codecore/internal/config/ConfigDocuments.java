package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.nio.file.Path;

import com.mrleonardos.codecore.api.config.ConfigFormat;

/** Содержимое файла по его формату. */
final class ConfigDocuments {

    private ConfigDocuments() {}

    static ConfigDocument empty(ConfigFormat format) {
        return format == ConfigFormat.JSON ? JsonDocument.empty() : TomlDocument.empty();
    }

    static ConfigDocument read(ConfigFormat format, Path path) throws IOException {
        return format == ConfigFormat.JSON ? JsonDocument.read(path) : TomlDocument.read(path);
    }
}
