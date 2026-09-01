package com.mrleonardos.codecore.internal.config;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.ConfigFormat;

/**
 * Подсказка тому, чей файл не разобрался.
 *
 * <p>
 * Имя секции кириллицей это первое, что напишет русскоязычный админ, и toml такое имя без кавычек не
 * принимает: файл целиком уезжает в {@code .broken}, а в логе стоит только «Invalid bare key». Подсказка
 * идёт следом за именем отложенного файла, чтобы причина и лечение лежали рядом с последствием.
 *
 * <p>
 * Разбирать текст чужого исключения смысла нет: на кириллицу night-config отвечает «Invalid bare key», на
 * пробел в том же месте «Invalid separator ... in table name», и завтра формулировки поменяются. Поэтому
 * подсказка одна на любую ошибку разбора toml и говорит про частую причину, а не про эту.
 *
 * <p>
 * Сама строка на английском, как и остальной лог ядра, а пример в ней кириллицей нарочно. Русский текст в
 * консоли под cp866 превратится в мусор, английский останется читаемым, и учит здесь пример: даже
 * покорёженный, он показывает кавычки на своём месте.
 */
final class ConfigHints {

    private static final String QUOTED_NAMES = "Common cause: a section or key name written in Cyrillic, or with a space or a dot. "
        + "TOML accepts such names only in quotes: [channels.\"Ярмарка\"], not [channels.Ярмарка]";

    private ConfigHints() {}

    /** Сказать про кавычки в именах, если разбор сорвался на toml. */
    static void afterBadRead(ConfigFormat format, Logger log) {
        if (format == ConfigFormat.TOML) {
            log.warn(QUOTED_NAMES);
        }
    }

    /** Текст подсказки: по нему её узнаёт проверка. */
    static String quotedNames() {
        return QUOTED_NAMES;
    }
}
