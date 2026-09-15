package com.mrleonardos.codecore.internal.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.Logger;

/**
 * Запись файла настроек: сначала во временный, потом подмена.
 *
 * <p>
 * Прерванное сохранение не оставит от настроек половину: на месте основного файла до последнего момента
 * лежит прежнее содержимое.
 */
final class ConfigWriting {

    private ConfigWriting() {}

    /** Кто умеет вылить себя в поток символов. */
    interface Output {

        void writeTo(Writer writer) throws IOException;
    }

    static void atomically(Path path, Output output, String describe, Logger log) {
        Path temporary = path.resolveSibling(
            path.getFileName()
                .toString() + ConfigKeys.TEMPORARY_SUFFIX);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                output.writeTo(writer);
            }
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException failure) {
            log.error("Failed to save config {}: {}", describe, failure.toString());
            discard(temporary, describe, log);
        }
    }

    private static void discard(Path temporary, String describe, Logger log) {
        try {
            Files.deleteIfExists(temporary);
        } catch (IOException leftover) {
            log.warn(
                "Temporary file {} of config {} could not be removed: {}",
                temporary,
                describe,
                leftover.toString());
        }
    }
}
