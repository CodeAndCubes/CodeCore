package com.mrleonardos.codecore.internal.db;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Содержимое {@code config/code/core/core-databases.toml}.
 *
 * <p>
 * Баз столько, сколько нужно серверу, и каждая независима. Заводской файл пуст: ядро само ни одной базы
 * не заводит, а сервер без баз работает как работал.
 *
 * <p>
 * Имя сервера здесь не повторяется: оно живёт в главном файле линейки одним ключом {@code serverId} и
 * оттуда же берётся сервисом. Второе место, где его можно написать, однажды разошлось бы с первым.
 */
public final class DatabasesFile {

    @Comment({ "Базы данных для модов линейки. Имя таблицы это имя базы, по нему её и спрашивает мод.", "Пример:",
        "  [databases.global]", "  role = \"global\"", "  driverClass = \"org.mariadb.jdbc.Driver\"",
        "  url = \"jdbc:mariadb://10.0.0.5:3306/mymods\"", "  user = \"mymods\"", "  password = \"secret\"",
        "  poolSize = 8" })
    public Map<String, DatabaseEntry> databases = new LinkedHashMap<>();

    @Comment({ "Какую базу брать, когда метку роли носят несколько: роль = имя базы.",
        "Нужен только при неоднозначности; молчаливого выбора первой попавшейся не бывает.", "Пример:",
        "  [roleDefaults]", "  server = \"local\"" })
    public Map<String, String> roleDefaults = new LinkedHashMap<>();
}
