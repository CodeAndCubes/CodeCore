package com.mrleonardos.codecore.internal.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.db.Database;
import com.mrleonardos.codecore.api.db.DatabaseException;
import com.mrleonardos.codecore.api.db.DatabaseUnavailableException;
import com.mrleonardos.codecore.api.db.DbStatus;
import com.mrleonardos.codecore.api.db.DbWork;
import com.mrleonardos.codecore.api.db.ModMigrations;

/**
 * База из настроек: пул, рабочий поток и запрет держать тик.
 *
 * <p>
 * Рабочий поток один на базу и задачи исполняет по очереди. Так мод, поставивший миграции, а следом
 * запросы, получает предсказуемый порядок; параллелить нечего, а пул нужен тем, кто берёт соединение
 * в своих потоках.
 *
 * <p>
 * Обещание {@code async} завершается в серверном потоке, поэтому слушатель результата волен трогать мир
 * и игроков. Серверный поток назван явно: в одиночной игре главных потоков два, и работа с миром из
 * клиентского даёт гонки на пустом месте.
 */
public final class DatabaseImpl implements Database, AutoCloseable {

    private static final String PROBE = "select 1";

    private final String id;
    private final String role;
    private final String dialect;
    private final ConnectionPool pool;
    private final ExecutorService worker;
    private final Consumer<Runnable> mainThread;
    private final BooleanSupplier onMainThread;
    private final LongSupplier clock;
    private final Logger log;
    private final Set<CompletableFuture<?>> pending = ConcurrentHashMap.newKeySet();

    private volatile DbStatus status = DbStatus.unknown();

    public DatabaseImpl(String id, String role, String dialect, ConnectionPool pool, Consumer<Runnable> mainThread,
        BooleanSupplier onMainThread, LongSupplier clock, Logger log) {
        this.id = id;
        this.role = role;
        this.dialect = dialect;
        this.pool = pool;
        this.mainThread = mainThread;
        this.onMainThread = onMainThread;
        this.clock = clock;
        this.log = log;
        this.worker = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "codecore-db-" + id);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String role() {
        return role;
    }

    @Override
    public String dialect() {
        return dialect;
    }

    @Override
    public Connection connection() {
        refuseMainThread();
        return pool.borrow();
    }

    @Override
    public <T> T sync(DbWork<T> work) {
        refuseMainThread();
        return run(work, false);
    }

    @Override
    public <T> T syncInTransaction(DbWork<T> work) {
        refuseMainThread();
        return run(work, true);
    }

    @Override
    public <T> CompletableFuture<T> async(DbWork<T> work) {
        return submit(() -> run(work, false));
    }

    @Override
    public <T> CompletableFuture<T> asyncInTransaction(DbWork<T> work) {
        return submit(() -> run(work, true));
    }

    @Override
    public ModMigrations migrations(String modid) {
        return new MigrationsImpl(this, modid);
    }

    @Override
    public DbStatus status() {
        return status;
    }

    /**
     * Спросить базу тестовым запросом.
     *
     * <p>
     * Отказ здесь не исключение, а ответ: сводка при старте перечисляет и живые базы, и мёртвые, а
     * сервер поднимается в обоих случаях.
     */
    public CompletableFuture<DbStatus> check() {
        return submit(() -> {
            try {
                run(connection -> {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(PROBE);
                    }
                    return null;
                }, false);
            } catch (DatabaseException failure) {
                log.debug("Database {}: the probe failed, {}", id, failure.getMessage());
            }
            return status;
        });
    }

    /** Работа прямо в этом потоке: ею пользуются миграции, уже оказавшиеся в рабочем потоке. */
    <T> T runHere(DbWork<T> work, boolean transaction) {
        return run(work, transaction);
    }

    /** Поставить работу в очередь рабочего потока: тем же путём идут миграции. */
    <T> CompletableFuture<T> submit(Supplier<T> call) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        pending.add(promise);
        promise.whenComplete((value, failure) -> pending.remove(promise));
        try {
            worker.execute(() -> {
                try {
                    T value = call.get();
                    mainThread.accept(() -> promise.complete(value));
                } catch (RuntimeException failure) {
                    mainThread.accept(() -> promise.completeExceptionally(failure));
                }
            });
        } catch (RejectedExecutionException stopped) {
            promise.completeExceptionally(new DatabaseUnavailableException("Database " + id + " is stopped", stopped));
        }
        return promise;
    }

    @Override
    public void close() {
        worker.shutdownNow();
        pool.close();
        for (CompletableFuture<?> promise : pending) {
            promise.completeExceptionally(new DatabaseUnavailableException("Database " + id + " is stopped"));
        }
        pending.clear();
    }

    private void refuseMainThread() {
        if (onMainThread.getAsBoolean()) {
            throw new IllegalStateException(
                "Database " + id + ": no calls from the main thread, use async or work from your own thread");
        }
    }

    private <T> T run(DbWork<T> work, boolean transaction) {
        long started = clock.getAsLong();
        try (Connection connection = pool.borrow()) {
            T value = transaction ? inTransaction(connection, work) : work.run(connection);
            status = DbStatus.ok(clock.getAsLong() - started);
            return value;
        } catch (DatabaseException known) {
            status = DbStatus.failed(known.getMessage());
            throw known;
        } catch (Exception failure) {
            status = DbStatus.failed(String.valueOf(failure.getMessage()));
            throw new DatabaseException("Database " + id + ": the work failed", failure);
        }
    }

    private <T> T inTransaction(Connection connection, DbWork<T> work) throws Exception {
        boolean previous = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            T value = work.run(connection);
            connection.commit();
            return value;
        } catch (Exception failure) {
            rollback(connection);
            throw failure;
        } finally {
            restore(connection, previous);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
            log.warn("Database {}: transaction rolled back", id);
        } catch (SQLException failure) {
            log.warn("Database {}: rollback failed, {}", id, failure.getMessage());
        }
    }

    private void restore(Connection connection, boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException failure) {
            log.warn("Database {}: autocommit was not restored, {}", id, failure.getMessage());
        }
    }
}
