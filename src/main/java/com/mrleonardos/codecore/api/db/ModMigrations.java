package com.mrleonardos.codecore.api.db;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Версии схемы одного мода в одной базе.
 *
 * <p>
 * Ядро держит таблицу {@code schema_migrations} с парой мод и версия, остальное дело мода: какие
 * таблицы заводить и чем их наполнять, знает он.
 */
public interface ModMigrations {

    /**
     * Наибольшая применённая версия или ноль, если мод в этой базе ещё не появлялся.
     *
     * @throws IllegalStateException если вызвано из главного потока
     * @throws DatabaseException     если база не ответила
     */
    int currentVersion();

    /**
     * Применить недостающие шаги по возрастанию версии.
     *
     * <p>
     * Работа идёт в рабочем потоке базы, результат приезжает в главный поток. Сбой шага откатывает его
     * и останавливает применение: следующие шаги не исполняются, а в исключении названа версия.
     *
     * @return сколько шагов применено
     * @throws IllegalArgumentException если в списке два шага с одним номером
     */
    CompletableFuture<Integer> apply(List<MigrationStep> steps);

    /**
     * То же, но прямо здесь и сейчас.
     *
     * <p>
     * Для подъёма при старте сервера: данные нужны до первого тика, а обещание в этот момент завершать
     * некому, очередь главного потока ещё никто не крутит. Из тика запрещено так же, как и {@code sync}.
     *
     * @return сколько шагов применено
     * @throws IllegalStateException если вызвано из идущего тика
     */
    int applyNow(List<MigrationStep> steps);
}
