package com.mrleonardos.codecore.internal.text;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Перевод строк на стороне сервера.
 *
 * <p>
 * Ключ перевода разбирает тот, кто показывает строку, а не тот, кто её шлёт. Клиент без нашего мода
 * файла перевода не видит, поэтому {@code codeessentials.message.kit_saved} доехал бы до игрока таким
 * же ключом. Строку собирает сервер и шлёт готовой.
 *
 * <p>
 * Язык берётся из главного файла линейки, файлы читаются из ресурсов модов по первому звену ключа:
 * {@code codeessentials.message.x} лежит в {@code /assets/codeessentials/lang/}. Выбранный язык ложится
 * поверх {@code en_US}, поэтому недопереведённый файл оставляет английскую строку, а не ключ.
 */
public final class Texts {

    /** Язык, на котором линейка написана целиком: подложка под любой другой. */
    public static final String DEFAULT_LANGUAGE = "en_US";

    private static final Object[] EMPTY = new Object[0];

    private static volatile Texts current = new Texts(DEFAULT_LANGUAGE);

    private final Map<String, Map<String, String>> tables = new ConcurrentHashMap<>();
    private final String language;

    public Texts(String language) {
        this.language = normalized(language);
    }

    /** Текущий переводчик сервера. */
    public static Texts current() {
        return current;
    }

    /** Сменить язык: таблицы читаются заново при первом обращении. */
    public static void language(String language) {
        current = new Texts(language);
    }

    /** Имя языка в виде имени файла перевода. */
    public String language() {
        return language;
    }

    /**
     * Собрать строку по ключу.
     *
     * @return пустой ответ, когда такого ключа нет ни в выбранном языке, ни в {@code en_US}: решать,
     *         что показать вместо него, зовущему
     */
    public Optional<String> find(String key, Object... arguments) {
        if (key == null || key.isEmpty()) {
            return Optional.empty();
        }
        int dot = key.indexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        String line = table(key.substring(0, dot)).get(key);
        if (line == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(String.format(line, arguments == null ? EMPTY : arguments));
        } catch (IllegalFormatException mismatch) {
            return Optional.of(line);
        }
    }

    private Map<String, String> table(String modId) {
        Map<String, String> known = tables.get(modId);
        if (known != null) {
            return known;
        }
        Map<String, String> loaded = LangFiles.read(modId, DEFAULT_LANGUAGE);
        if (!DEFAULT_LANGUAGE.equals(language)) {
            loaded.putAll(LangFiles.read(modId, language));
        }
        tables.put(modId, loaded);
        return loaded;
    }

    /**
     * Привести имя языка к виду файла: {@code ru_ru} и {@code RU_ru} это тот же {@code ru_RU}. Опечатка
     * в регистре стоила бы админу всего перевода, а разобрать её по логу нечем.
     */
    private static String normalized(String raw) {
        if (raw == null || raw.trim()
            .isEmpty()) {
            return DEFAULT_LANGUAGE;
        }
        String name = raw.trim();
        int split = name.indexOf('_');
        if (split <= 0 || split == name.length() - 1) {
            return name.toLowerCase(Locale.ROOT);
        }
        return name.substring(0, split)
            .toLowerCase(Locale.ROOT) + "_"
            + name.substring(split + 1)
                .toUpperCase(Locale.ROOT);
    }
}
