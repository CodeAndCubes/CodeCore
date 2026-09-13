package com.mrleonardos.codecore.internal.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Откуда пул берёт новые соединения.
 *
 * <p>
 * Узкий шов ради тестов: ожидание, таймаут, возврат соединения и закрытие простоявших проверяются на
 * подставном источнике, без настоящей СУБД и без сети.
 */
public interface ConnectionSource {

    Connection open() throws SQLException;
}
