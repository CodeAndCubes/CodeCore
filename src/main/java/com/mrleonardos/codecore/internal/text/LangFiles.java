package com.mrleonardos.codecore.internal.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Чтение файла перевода мода из его же ресурсов.
 *
 * <p>
 * Формат тот же, что читает игра: строка {@code ключ=значение}, пустые строки и начатые решёткой
 * пропускаются, ключ отделяется первым знаком равенства. Подстановки {@code %d} и {@code %f}
 * переписываются в {@code %s} по образцу {@code StringTranslate}: число в {@code .lang} лежит строкой,
 * и {@code String.format} на нём иначе падает.
 */
final class LangFiles {

    private static final Pattern NUMBER_SLOT = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    private static final String ASSETS = "/assets/";
    private static final String LANG = "/lang/";
    private static final String SUFFIX = ".lang";
    private static final char COMMENT = '#';

    private LangFiles() {}

    /**
     * Прочитать перевод одного мода.
     *
     * @return пары ключ-значение, пустая карта, когда файла нет или он не читается: мода может не быть
     *         на сервере вовсе
     */
    static Map<String, String> read(String modId, String language) {
        Map<String, String> lines = new LinkedHashMap<>();
        String path = ASSETS + modId + LANG + language + SUFFIX;
        try (InputStream stream = LangFiles.class.getResourceAsStream(path)) {
            if (stream == null) {
                return lines;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    put(lines, line);
                }
            }
        } catch (IOException unreadable) {
            return lines;
        }
        return lines;
    }

    private static void put(Map<String, String> lines, String raw) {
        String line = raw.trim();
        if (line.isEmpty() || line.charAt(0) == COMMENT) {
            return;
        }
        int split = line.indexOf('=');
        if (split <= 0) {
            return;
        }
        lines.put(
            line.substring(0, split),
            NUMBER_SLOT.matcher(line.substring(split + 1))
                .replaceAll("%$1s"));
    }
}
