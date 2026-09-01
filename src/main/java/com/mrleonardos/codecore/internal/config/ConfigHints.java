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
 */
final class ConfigHints {

    private static final String QUOTED_NAMES = "Частая причина: имя секции или ключа написано кириллицей, с пробелом или точкой. "
        + "Такие имена toml принимает только в кавычках: [channels.\"Ярмарка\"], а не [channels.Ярмарка]";

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
