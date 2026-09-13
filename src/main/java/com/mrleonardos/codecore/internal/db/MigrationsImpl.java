package com.mrleonardos.codecore.internal.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.mrleonardos.codecore.api.db.DatabaseException;
import com.mrleonardos.codecore.api.db.DbWork;
import com.mrleonardos.codecore.api.db.MigrationStep;
import com.mrleonardos.codecore.api.db.ModMigrations;

/**
 * Версии схемы одного мода в одной базе.
 *
 * <p>
 * Единственная таблица, которую ядро заводит само. Колонка зовётся {@code mod_id}, а не {@code mod}:
 * {@code mod} в MariaDB зарезервировано под оператор остатка, и таблица с такой колонкой там просто не
 * создаётся.
 *
 * <p>
 * Шаг применяется одной транзакцией вместе со строкой о версии, поэтому наполовину применённого шага не
 * бывает. Оговорка от СУБД: MariaDB завершает транзакцию на каждом DDL сама, и шаг со схемой может
 * оказаться применённым частично. Об этом пишется в лог, а сами шаги со схемой обязаны быть
 * идемпотентными.
 */
final class MigrationsImpl implements ModMigrations {

    private static final String CREATE = "create table if not exists schema_migrations ("
        + "mod_id varchar(64) not null, version int not null, applied_at bigint not null, "
        + "primary key (mod_id, version))";

    private static final String READ = "select max(version) from schema_migrations where mod_id = ?";

    private static final String WRITE = "insert into schema_migrations (mod_id, version, applied_at) values (?, ?, ?)";

    private final DatabaseImpl database;
    private final String modid;

    MigrationsImpl(DatabaseImpl database, String modid) {
        this.database = database;
        this.modid = modid;
    }

    @Override
    public int currentVersion() {
        return database.sync(connection -> {
            ensureTable(connection);
            return Integer.valueOf(version(connection));
        })
            .intValue();
    }

    @Override
    public CompletableFuture<Integer> apply(List<MigrationStep> steps) {
        List<MigrationStep> ordered = ordered(steps);
        return database.submit(() -> Integer.valueOf(applyAll(ordered, false)));
    }

    @Override
    public int applyNow(List<MigrationStep> steps) {
        return applyAll(ordered(steps), true);
    }

    private int applyAll(List<MigrationStep> steps, boolean here) {
        DbWork<Integer> work = connection -> {
            ensureTable(connection);
            int current = version(connection);
            int applied = 0;
            for (MigrationStep step : steps) {
                if (step.version() <= current) {
                    continue;
                }
                applyStep(connection, step);
                current = step.version();
                applied++;
            }
            return Integer.valueOf(applied);
        };
        return (here ? database.sync(work) : database.runHere(work, false)).intValue();
    }

    private void applyStep(Connection connection, MigrationStep step) throws SQLException {
        boolean previous = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (Statement statement = connection.createStatement()) {
                for (String sql : step.statements()) {
                    statement.execute(sql);
                }
            }
            try (PreparedStatement written = connection.prepareStatement(WRITE)) {
                written.setString(1, modid);
                written.setInt(2, step.version());
                written.setLong(3, System.currentTimeMillis());
                written.executeUpdate();
            }
            connection.commit();
        } catch (SQLException failure) {
            rollback(connection);
            throw new DatabaseException(
                "Migration step " + step.version()
                    + " of "
                    + modid
                    + " failed: "
                    + failure.getMessage()
                    + ". Schema statements of the step may already be applied, a retry must be idempotent",
                failure);
        } finally {
            restore(connection, previous);
        }
    }

    private void ensureTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE);
        }
    }

    private int version(Connection connection) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(READ)) {
            query.setString(1, modid);
            try (ResultSet answer = query.executeQuery()) {
                return answer.next() ? answer.getInt(1) : 0;
            }
        }
    }

    private static List<MigrationStep> ordered(List<MigrationStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> seen = new HashSet<>();
        for (MigrationStep step : steps) {
            if (!seen.add(Integer.valueOf(step.version()))) {
                throw new IllegalArgumentException("Two migration steps share version " + step.version());
            }
        }
        List<MigrationStep> sorted = new ArrayList<>(steps);
        sorted.sort(Comparator.comparingInt(MigrationStep::version));
        return sorted;
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            return;
        }
    }

    private static void restore(Connection connection, boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ignored) {
            return;
        }
    }
}
