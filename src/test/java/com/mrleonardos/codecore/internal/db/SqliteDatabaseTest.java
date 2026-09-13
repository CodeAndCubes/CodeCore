package com.mrleonardos.codecore.internal.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mrleonardos.codecore.api.db.Database;
import com.mrleonardos.codecore.api.db.DatabaseException;
import com.mrleonardos.codecore.api.db.MigrationStep;
import com.mrleonardos.codecore.api.db.ModMigrations;

/**
 * Настоящий sqlite: он и есть один из двух движков встроенного слоя.
 */
class SqliteDatabaseTest {

    private static final Logger LOG = LogManager.getLogger(SqliteDatabaseTest.class);
    private static final String MODID = "codetest";

    @TempDir
    Path folder;

    private DatabaseServiceImpl service;

    @AfterEach
    void close() {
        if (service != null) {
            service.close();
            service = null;
        }
    }

    private Database database(BooleanSupplier onMainThread) {
        DatabasesFile file = new DatabasesFile();
        DatabaseEntry entry = new DatabaseEntry();
        entry.role = "logs";
        entry.driverClass = "org.sqlite.JDBC";
        entry.url = "jdbc:sqlite:" + folder.resolve("codetest.db");
        file.databases.put("logs", entry);
        service = DatabaseServiceImpl
            .of(file, () -> "survival-1", Runnable::run, onMainThread, System::currentTimeMillis, LOG);
        return service.database("logs");
    }

    private static List<MigrationStep> steps() {
        return Arrays.asList(
            MigrationStep.of(1, "create table if not exists homes (owner varchar(64) not null, name varchar(64))"),
            MigrationStep.of(2, "create index if not exists homes_owner on homes (owner)"));
    }

    @Test
    @DisplayName("шаги применяются по порядку и второй раз не исполняются")
    void stepsApplyOnce() throws Exception {
        Database database = database(() -> false);
        ModMigrations migrations = database.migrations(MODID);

        assertEquals(0, migrations.currentVersion());
        assertEquals(
            Integer.valueOf(2),
            migrations.apply(steps())
                .get(5, TimeUnit.SECONDS));
        assertEquals(2, migrations.currentVersion());
        assertEquals(
            Integer.valueOf(0),
            migrations.apply(steps())
                .get(5, TimeUnit.SECONDS),
            "повторный проход не должен исполнять ничего");
    }

    @Test
    @DisplayName("подъём при старте применяет шаги прямо в своём потоке, без обещаний")
    void applyNowRunsHere() {
        Database database = database(() -> false);
        ModMigrations migrations = database.migrations(MODID);

        assertEquals(2, migrations.applyNow(steps()));
        assertEquals(2, migrations.currentVersion());
        assertEquals(0, migrations.applyNow(steps()), "повторный проход не должен исполнять ничего");
    }

    @Test
    @DisplayName("упавший шаг откатывается, останавливает применение и называет свою версию")
    void aFailedStepStopsEverything() throws Exception {
        Database database = database(() -> false);
        ModMigrations migrations = database.migrations(MODID);
        migrations.apply(steps())
            .get(5, TimeUnit.SECONDS);

        List<MigrationStep> broken = Arrays.asList(
            MigrationStep.of(3, "create table if not exists notes (id int)", "this is not sql at all"),
            MigrationStep.of(4, "create table if not exists never (id int)"));
        ExecutionException failure = assertThrows(
            ExecutionException.class,
            () -> migrations.apply(broken)
                .get(5, TimeUnit.SECONDS));

        assertTrue(failure.getCause() instanceof DatabaseException, String.valueOf(failure.getCause()));
        assertTrue(
            failure.getCause()
                .getMessage()
                .contains("3"),
            failure.getCause()
                .getMessage());
        assertEquals(2, migrations.currentVersion(), "версия не должна была вырасти");
        assertEquals(0, tables(database, "never"), "шаги после упавшего не исполняются");
    }

    @Test
    @DisplayName("исключение внутри транзакции откатывает записанное")
    void aTransactionRollsBack() throws Exception {
        Database database = database(() -> false);
        database.migrations(MODID)
            .apply(steps())
            .get(5, TimeUnit.SECONDS);

        ExecutionException failure = assertThrows(
            ExecutionException.class,
            () -> database.asyncInTransaction(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("insert into homes (owner, name) values ('alice', 'base')");
                }
                throw new IllegalStateException("мод передумал");
            })
                .get(5, TimeUnit.SECONDS));

        assertTrue(failure.getCause() instanceof DatabaseException);
        assertEquals(Integer.valueOf(0), database.async(connection -> {
            try (Statement statement = connection.createStatement();
                ResultSet answer = statement.executeQuery("select count(*) from homes")) {
                return Integer.valueOf(answer.next() ? answer.getInt(1) : -1);
            }
        })
            .get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("из главного потока база отказывает сразу, а async работает")
    void theMainThreadIsRefused() throws Exception {
        Database database = database(() -> true);

        assertThrows(IllegalStateException.class, () -> database.sync(connection -> null));
        assertThrows(IllegalStateException.class, database::connection);
        assertThrows(IllegalStateException.class, () -> database.syncInTransaction(connection -> null));
        assertEquals(Integer.valueOf(1), database.async(connection -> {
            try (Statement statement = connection.createStatement();
                ResultSet answer = statement.executeQuery("select 1")) {
                return Integer.valueOf(answer.next() ? answer.getInt(1) : -1);
            }
        })
            .get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("проверка при старте пишет состояние, которое потом отдаёт status")
    void theStartupCheckFillsStatus() throws Exception {
        Database database = database(() -> false);

        service.checkAll();
        database.async(connection -> null)
            .get(5, TimeUnit.SECONDS);

        assertTrue(
            database.status()
                .reachable());
        assertTrue(
            database.status()
                .pingMs() >= 0L);
    }

    private static int tables(Database database, String name) throws Exception {
        return database.async(connection -> {
            try (Statement statement = connection.createStatement();
                ResultSet answer = statement.executeQuery(
                    "select count(*) from sqlite_master where type = 'table' and name = '" + name + "'")) {
                return Integer.valueOf(answer.next() ? answer.getInt(1) : -1);
            }
        })
            .get(5, TimeUnit.SECONDS)
            .intValue();
    }
}
