package com.mrleonardos.codecore.internal.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.db.MigrationStep;
import com.mrleonardos.codecore.api.db.ModMigrations;

/**
 * Диалект mariadb на H2 в режиме совместимости MySQL.
 *
 * <p>
 * Это эмуляция, и звать её иначе нельзя: блокировок строк, поведения DDL в транзакции и прочих
 * тонкостей MariaDB H2 не повторяет. Здесь проверяется ровно то, что проверяемо без сервера СУБД: наши
 * стейтменты синтаксически годятся для семейства MySQL и таблица версий заводится там же, где в sqlite.
 */
class MariadbEmulationTest {

    private static final Logger LOG = LogManager.getLogger(MariadbEmulationTest.class);
    private static final String MODID = "codetest";

    private DatabaseImpl database;

    @BeforeEach
    void open() {
        String url = "jdbc:h2:mem:emul-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        ConnectionPool pool = new ConnectionPool(
            "emul",
            DriverConnections.of("org.h2.Driver", url, "sa", ""),
            2,
            1000L,
            5000,
            60L,
            System::currentTimeMillis,
            LOG);
        database = new DatabaseImpl(
            "emul",
            "global",
            Dialects.MARIADB,
            pool,
            Runnable::run,
            () -> false,
            System::currentTimeMillis,
            LOG);
    }

    @AfterEach
    void close() {
        database.close();
    }

    @Test
    @DisplayName("таблица версий и шаги со схемой проходят на семействе MySQL")
    void migrationsRunOnMysqlFamily() throws Exception {
        ModMigrations migrations = database.migrations(MODID);
        List<MigrationStep> steps = Arrays.asList(
            MigrationStep.of(1, "create table if not exists accounts (owner varchar(64) not null, amount bigint)"),
            MigrationStep.of(2, "create index if not exists accounts_owner on accounts (owner)"));

        assertEquals(
            Integer.valueOf(2),
            migrations.apply(steps)
                .get(5, TimeUnit.SECONDS));
        assertEquals(2, migrations.currentVersion());
        assertEquals(
            Integer.valueOf(0),
            migrations.apply(steps)
                .get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("колонка версий зовётся mod_id: имя mod в MySQL занято оператором остатка")
    void theColumnIsNotNamedMod() throws Exception {
        database.migrations(MODID)
            .apply(Arrays.asList(MigrationStep.of(1, "create table if not exists notes (id int)")))
            .get(5, TimeUnit.SECONDS);

        assertTrue(database.async(connection -> {
            try (Statement statement = connection.createStatement();
                ResultSet answer = statement.executeQuery("select mod_id, version from schema_migrations")) {
                return Boolean.valueOf(answer.next());
            }
        })
            .get(5, TimeUnit.SECONDS)
            .booleanValue());
    }
}
