package com.mrleonardos.codecore.api.db;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

/**
 * Одна база из настроек ядра.
 *
 * <p>
 * Главный поток базу не ждёт никогда. {@link #connection()}, {@link #sync(DbWork)} и
 * {@link #syncInTransaction(DbWork)} из него отказывают {@link IllegalStateException} до всякого
 * обращения к базе: сеть отвечает за десятки миллисекунд, а тик длится пятьдесят. Мод, которому нужен
 * результат в мире, зовёт {@link #async(DbWork)} и получает его в ближайшем тике.
 *
 * <p>
 * Соединение приходит из пула, а закрытие возвращает его туда же, поэтому {@code try-with-resources}
 * остаётся правильным способом работы. Каждый запрос получает таймаут из настроек записи.
 */
public interface Database {

    /** Идентификатор записи в настройках. */
    String id();

    /** Метка роли из настроек или пустая строка. */
    String role();

    /** Движок: {@code mariadb} или {@code sqlite} у встроенного слоя. */
    String dialect();

    /**
     * Соединение из пула. Закрытие возвращает его в пул, а не рвёт связь.
     *
     * @throws IllegalStateException        если вызвано из главного потока
     * @throws DatabaseBusyException        если пул не отдал соединение за отведённое время
     * @throws DatabaseUnavailableException если база не отвечает
     */
    Connection connection();

    /**
     * Выполнить работу и вернуть результат.
     *
     * @throws IllegalStateException если вызвано из главного потока
     * @throws DatabaseException     если работа или база отказали
     */
    <T> T sync(DbWork<T> work);

    /** То же в транзакции: успех коммитится, исключение откатывает всё. */
    <T> T syncInTransaction(DbWork<T> work);

    /**
     * Выполнить работу в рабочем потоке базы, а результат отдать в главном потоке.
     *
     * <p>
     * Слушатель результата может трогать мир и игроков: он уже в тике. Остановка сервера завершает
     * незаконченные обещания {@link DatabaseUnavailableException}.
     */
    <T> CompletableFuture<T> async(DbWork<T> work);

    /** То же в транзакции. */
    <T> CompletableFuture<T> asyncInTransaction(DbWork<T> work);

    /** Версии схемы этого мода в этой базе. */
    ModMigrations migrations(String modid);

    /** Чем кончилась последняя проверка базы. */
    DbStatus status();
}
