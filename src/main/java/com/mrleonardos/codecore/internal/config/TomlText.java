package com.mrleonardos.codecore.internal.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Пустая строка перед заголовком секции.
 *
 * <p>
 * Писатель night-config отбивает пустой строкой только первую таблицу, и файл из шести секций подряд
 * читается сплошняком. Отбивка ставится по готовому тексту и повторной записи не размножается: там, где
 * пустая строка уже есть, ничего не добавляется.
 */
final class TomlText {

    private static final String COMMENT = "#";
    private static final String TABLE = "[";
    private static final String NEWLINE = "\n";
    private static final String BLANK = "";

    private TomlText() {}

    static String spaceSections(String text) {
        List<String> spaced = new ArrayList<>();
        List<String> comments = new ArrayList<>();
        boolean lastBlank = true;

        for (String line : text.split(NEWLINE, -1)) {
            if (line.startsWith(COMMENT)) {
                comments.add(line);
                continue;
            }
            if (line.startsWith(TABLE) && !lastBlank) {
                spaced.add(BLANK);
            }
            spaced.addAll(comments);
            comments.clear();
            spaced.add(line);
            lastBlank = line.isEmpty();
        }
        spaced.addAll(comments);
        return String.join(NEWLINE, spaced);
    }
}
