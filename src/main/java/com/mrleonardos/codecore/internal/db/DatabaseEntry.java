package com.mrleonardos.codecore.internal.db;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Одна база в настройках.
 *
 * <p>
 * Тип базы задаётся этой записью и с её ролью не связан: общая база и серверная бывают обе на MariaDB,
 * а журналы рядом с миром в файле sqlite. Администратор волен собрать любое сочетание.
 *
 * <p>
 * Пароль лежит открытым текстом. Это серверный файл, который правит администратор, и в систему контроля
 * версий он не попадает; права на него стоит держать такими же, как на файлы мира.
 */
public final class DatabaseEntry {

    @Comment({ "Метка роли: global, server, logs или своя.", "Нужна для поиска базы по роли, повторяться может.",
        "Пустая метка значит, что базу ищут только по её имени." })
    public String role = "";

    @Comment({ "Полное имя класса драйвера. Ядро драйверы не поставляет:",
        "org.mariadb.jdbc.Driver или org.sqlite.JDBC кладёт администратор в mods/ или libs/." })
    public String driverClass = "";

    @Comment({ "Адрес базы. Схема выбирает движок:", "jdbc:mariadb://host:3306/база, jdbc:mysql://host:3306/база,",
        "jdbc:sqlite:world/codecore/logs.db (путь от папки сервера)." })
    public String url = "";

    @Comment("Пользователь базы. Для sqlite оставляют пустым.")
    public String user = "";

    @Comment("Пароль в открытом виде. Файл наружу не отдавать.")
    public String password = "";

    @Comment("Сколько соединений держать. Зажимается в 1..32, для sqlite всегда 1.")
    public int poolSize = DbLimits.POOL_SIZE_DEFAULT;

    @Comment("Сколько ждать свободного соединения, миллисекунды. Зажимается в 100..60000.")
    public int connectionTimeoutMs = DbLimits.CONNECTION_TIMEOUT_DEFAULT;

    @Comment("Потолок времени на запрос, миллисекунды. Зажимается в 100..600000.")
    public int queryTimeoutMs = DbLimits.QUERY_TIMEOUT_DEFAULT;

    @Comment("Через сколько секунд простоя закрывать соединение. Зажимается в 5..3600.")
    public int idleTimeoutSeconds = DbLimits.IDLE_TIMEOUT_DEFAULT;
}
