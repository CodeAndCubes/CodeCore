package com.mrleonardos.codecore.internal.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Движок базы по схеме адреса.
 *
 * <p>
 * Спрашивать драйвер о движке поздно: решение нужно до того, как драйвер загружен, и ошибку в адресе
 * администратор должен увидеть при старте, а не первым запросом мода.
 *
 * <p>
 * Встроенный слой знает ровно два движка. Это рамка встроенной реализации, а не интерфейса: чужой
 * {@code DatabaseService} из реестра волен понимать что угодно.
 */
public final class Dialects {

    public static final String MARIADB = "mariadb";
    public static final String SQLITE = "sqlite";

    private static final List<String> SCHEMES = Collections
        .unmodifiableList(Arrays.asList("jdbc:mariadb:", "jdbc:mysql:", "jdbc:sqlite:"));

    private Dialects() {}

    /** Движок или пустая ссылка, когда схема чужая. */
    public static String of(String url) {
        if (url == null) {
            return null;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.startsWith("jdbc:mariadb:") || lower.startsWith("jdbc:mysql:")) {
            return MARIADB;
        }
        if (lower.startsWith("jdbc:sqlite:")) {
            return SQLITE;
        }
        return null;
    }

    /** Схемы, которые слой понимает: их перечисляют в отказе. */
    public static List<String> schemes() {
        return SCHEMES;
    }
}
