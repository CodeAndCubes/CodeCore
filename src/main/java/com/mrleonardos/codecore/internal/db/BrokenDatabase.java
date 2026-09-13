package com.mrleonardos.codecore.internal.db;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import com.mrleonardos.codecore.api.db.Database;
import com.mrleonardos.codecore.api.db.DatabaseException;
import com.mrleonardos.codecore.api.db.DbStatus;
import com.mrleonardos.codecore.api.db.DbWork;
import com.mrleonardos.codecore.api.db.ModMigrations;

/**
 * Запись, которую не удалось поднять.
 *
 * <p>
 * База остаётся в перечне и отвечает на каждый вызов той же причиной, по которой не поднялась. Убрать
 * её из перечня значило бы ответить моду «такой базы нет», и тот пошёл бы искать опечатку в своём
 * идентификаторе вместо недостающего драйвера.
 */
final class BrokenDatabase implements Database {

    private final String id;
    private final String role;
    private final String dialect;
    private final DatabaseException reason;

    BrokenDatabase(String id, String role, String dialect, DatabaseException reason) {
        this.id = id;
        this.role = role;
        this.dialect = dialect;
        this.reason = reason;
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
        throw reason;
    }

    @Override
    public <T> T sync(DbWork<T> work) {
        throw reason;
    }

    @Override
    public <T> T syncInTransaction(DbWork<T> work) {
        throw reason;
    }

    @Override
    public <T> CompletableFuture<T> async(DbWork<T> work) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        promise.completeExceptionally(reason);
        return promise;
    }

    @Override
    public <T> CompletableFuture<T> asyncInTransaction(DbWork<T> work) {
        return async(work);
    }

    @Override
    public ModMigrations migrations(String modid) {
        throw reason;
    }

    @Override
    public DbStatus status() {
        return DbStatus.failed(reason.getMessage());
    }
}
