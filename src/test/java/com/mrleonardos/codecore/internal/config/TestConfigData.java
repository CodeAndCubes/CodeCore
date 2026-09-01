package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mrleonardos.codecore.api.config.ConfigData;

/** Дерево настроек из куска toml: чтобы тесты соседних пакетов не заводили файлы на диске. */
public final class TestConfigData {

    private TestConfigData() {}

    public static ConfigData of(String toml) throws IOException {
        Path file = Files.createTempFile("codecore-test", ".toml");
        try {
            Files.write(file, toml.getBytes(StandardCharsets.UTF_8));
            return TomlDocument.read(file)
                .data();
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
