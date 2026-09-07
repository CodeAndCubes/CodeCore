package com.mrleonardos.codecore.internal.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Чтение файла настроек с диска. */
final class ConfigReading {

    /**
     * Метка порядка байтов в начале файла.
     *
     * <p>
     * Для разбора это лишний знак в первом ключе, и файл уезжает в {@code .broken} целиком, вместе со
     * всеми настройками сервера. Дописывает её «Блокнот» Windows при каждом сохранении, поэтому
     * администратор получал сброс настроек за одну правку файла в редакторе, который стоит в системе.
     */
    private static final int BOM = '\uFEFF';

    private ConfigReading() {}

    /** Открыть файл в UTF-8, пропустив метку порядка байтов, если она есть. */
    static Reader utf8(Path path) throws IOException {
        BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        reader.mark(1);
        if (reader.read() != BOM) {
            reader.reset();
        }
        return reader;
    }
}
