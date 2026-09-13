package com.mrleonardos.codecore.api.db;

import java.sql.Connection;

/**
 * Работа с открытым соединением.
 *
 * <p>
 * Ядро даёт соединение и забирает его обратно, остальное дело мода: запросы он пишет сам, потому что
 * знает свои таблицы. Проверяемое исключение разрешено и оборачивается в {@link DatabaseException} с
 * исходной причиной.
 */
@FunctionalInterface
public interface DbWork<T> {

    T run(Connection connection) throws Exception;
}
