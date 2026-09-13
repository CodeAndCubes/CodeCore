package com.mrleonardos.codecore.internal.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;

/**
 * Папка под файл базы sqlite.
 *
 * <p>
 * Драйвер заводит сам только файл, но не папку над ним: адрес вида {@code jdbc:sqlite:world/codecore/logs.db}
 * на чистом сервере отвечает «unable to open database file», и админ читает это как недоступную базу.
 * Папку он назвал сам, поэтому создать её по его же адресу честно.
 *
 * <p>
 * База в памяти и адрес в форме URI пропускаются: файла там нет вовсе.
 */
final class SqliteFiles {

    private static final String PREFIX = "jdbc:sqlite:";
    private static final String URI_FORM = "file:";

    private SqliteFiles() {}

    static void ensureFolder(String url, Logger log) {
        if (url == null || !url.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            return;
        }
        String target = url.substring(PREFIX.length());
        if (target.isEmpty() || target.startsWith(":")
            || target.regionMatches(true, 0, URI_FORM, 0, URI_FORM.length())) {
            return;
        }
        try {
            Path folder = Paths.get(target)
                .toAbsolutePath()
                .getParent();
            if (folder != null && !Files.isDirectory(folder)) {
                Files.createDirectories(folder);
            }
        } catch (InvalidPathException | IOException failure) {
            log.warn("Sqlite folder for {} could not be created: {}", url, failure.toString());
        }
    }
}
