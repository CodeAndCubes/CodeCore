package com.mrleonardos.codecore.internal.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.db.Database;
import com.mrleonardos.codecore.api.db.DatabaseException;
import com.mrleonardos.codecore.api.db.DatabaseService;
import com.mrleonardos.codecore.api.db.UnknownDatabaseException;

/**
 * Встроенный сервис баз: читает настройки, поднимает записи, отдаёт их по имени и по роли.
 *
 * <p>
 * Запись, которую не удалось поднять, остаётся в перечне и объясняет причину при каждом обращении.
 * Соседние базы от неё не страдают: недостающий драйвер одной не должен лишать сервер остальных.
 *
 * <p>
 * Молчаливого выбора первой подходящей записи нет нигде. База, выбранная за администратора, однажды
 * окажется не той, и узнают об этом по перепутанным данным, а не по строке в логе.
 */
public final class DatabaseServiceImpl implements DatabaseService, AutoCloseable {

    private static final String ROLE_GLOBAL = "global";
    private static final String ROLE_SERVER = "server";

    private final Map<String, Database> databases;
    private final Map<String, String> roleDefaults;
    private final Supplier<String> serverId;
    private final Logger log;

    private DatabaseServiceImpl(Map<String, Database> databases, Map<String, String> roleDefaults,
        Supplier<String> serverId, Logger log) {
        this.databases = databases;
        this.roleDefaults = roleDefaults;
        this.serverId = serverId;
        this.log = log;
    }

    /**
     * Поднять сервис по настройкам.
     *
     * @param serverId     имя сервера из главного файла линейки
     * @param mainThread   куда отдавать завершение обещаний
     * @param onMainThread проверка «этот вызов из тика»
     */
    public static DatabaseServiceImpl of(DatabasesFile file, Supplier<String> serverId, Consumer<Runnable> mainThread,
        BooleanSupplier onMainThread, LongSupplier clock, Logger log) {
        Map<String, Database> databases = new LinkedHashMap<>();
        for (Map.Entry<String, DatabaseEntry> configured : file.databases.entrySet()) {
            Database database = build(configured.getKey(), configured.getValue(), mainThread, onMainThread, clock, log);
            if (database != null) {
                databases.put(database.id(), database);
            }
        }
        return new DatabaseServiceImpl(databases, defaults(file, databases, log), serverId, log);
    }

    private static Database build(String id, DatabaseEntry entry, Consumer<Runnable> mainThread,
        BooleanSupplier onMainThread, LongSupplier clock, Logger log) {
        if (id == null || id.trim()
            .isEmpty()) {
            log.warn("Database entry without a name is skipped");
            return null;
        }
        if (entry == null || entry.driverClass.trim()
            .isEmpty()
            || entry.url.trim()
                .isEmpty()) {
            log.warn("Database {} has no driverClass or url, it is skipped", id);
            return null;
        }
        String dialect = Dialects.of(entry.url);
        if (dialect == null) {
            log.warn("Database {} has url scheme this core does not know, supported are {}", id, Dialects.schemes());
            return null;
        }
        String role = role(id, entry.role, log);
        int poolSize = poolSize(id, entry, dialect, log);
        if (Dialects.SQLITE.equals(dialect)) {
            SqliteFiles.ensureFolder(entry.url, log);
        }
        try {
            ConnectionPool pool = new ConnectionPool(
                id,
                DriverConnections.of(entry.driverClass, entry.url, entry.user, entry.password),
                poolSize,
                DbLimits.clamp(
                    entry.connectionTimeoutMs,
                    DbLimits.CONNECTION_TIMEOUT_MIN,
                    DbLimits.CONNECTION_TIMEOUT_MAX,
                    DbLimits.CONNECTION_TIMEOUT_DEFAULT),
                DbLimits.clamp(
                    entry.queryTimeoutMs,
                    DbLimits.QUERY_TIMEOUT_MIN,
                    DbLimits.QUERY_TIMEOUT_MAX,
                    DbLimits.QUERY_TIMEOUT_DEFAULT),
                DbLimits.clamp(
                    entry.idleTimeoutSeconds,
                    DbLimits.IDLE_TIMEOUT_MIN,
                    DbLimits.IDLE_TIMEOUT_MAX,
                    DbLimits.IDLE_TIMEOUT_DEFAULT),
                clock,
                log);
            return new DatabaseImpl(id, role, dialect, pool, mainThread, onMainThread, clock, log);
        } catch (DatabaseException failure) {
            log.warn("Database {} did not start: {}", id, failure.getMessage());
            return new BrokenDatabase(id, role, dialect, failure);
        }
    }

    private static String role(String id, String configured, Logger log) {
        String role = configured == null ? "" : configured.trim();
        if (role.length() > DbLimits.ROLE_MAX_LENGTH) {
            log.warn(
                "Database {} has a role label longer than {} characters, the label is dropped",
                id,
                Integer.valueOf(DbLimits.ROLE_MAX_LENGTH));
            return "";
        }
        return role;
    }

    private static int poolSize(String id, DatabaseEntry entry, String dialect, Logger log) {
        int configured = DbLimits
            .clamp(entry.poolSize, DbLimits.POOL_SIZE_MIN, DbLimits.POOL_SIZE_MAX, DbLimits.POOL_SIZE_DEFAULT);
        if (!Dialects.SQLITE.equals(dialect) || configured == 1) {
            return configured;
        }
        log.info("Database {} is sqlite, pool size is forced to 1: the driver writes the file one at a time", id);
        return 1;
    }

    private static Map<String, String> defaults(DatabasesFile file, Map<String, Database> databases, Logger log) {
        Map<String, String> chosen = new LinkedHashMap<>();
        for (Map.Entry<String, String> pair : file.roleDefaults.entrySet()) {
            String role = pair.getKey();
            Database picked = databases.get(pair.getValue());
            if (picked == null) {
                log.warn("roleDefaults sends role {} to database {} which is not in the file", role, pair.getValue());
                continue;
            }
            if (!picked.role()
                .equals(role)) {
                log.warn(
                    "roleDefaults sends role {} to database {} whose role is {}",
                    role,
                    picked.id(),
                    picked.role());
                continue;
            }
            chosen.put(role, picked.id());
        }
        return chosen;
    }

    @Override
    public Database database(String id) {
        Database found = databases.get(id);
        if (found == null) {
            throw new UnknownDatabaseException("There is no database " + id + " in the config, known are " + ids());
        }
        return found;
    }

    @Override
    public Database byRole(String role) {
        List<Database> matching = new ArrayList<>();
        for (Database database : databases.values()) {
            if (database.role()
                .equals(role)) {
                matching.add(database);
            }
        }
        if (matching.size() == 1) {
            return matching.get(0);
        }
        String chosen = roleDefaults.get(role);
        if (chosen != null) {
            return database(chosen);
        }
        if (matching.isEmpty()) {
            throw new UnknownDatabaseException("No database carries role " + role + ", known roles are " + roles());
        }
        throw new UnknownDatabaseException(
            "Role " + role + " is carried by " + names(matching) + ", pick one with a roleDefaults key");
    }

    @Override
    public Database global() {
        return byRole(ROLE_GLOBAL);
    }

    @Override
    public Database server() {
        return byRole(ROLE_SERVER);
    }

    @Override
    public List<String> ids() {
        return Collections.unmodifiableList(new ArrayList<>(databases.keySet()));
    }

    @Override
    public List<String> roles() {
        List<String> roles = new ArrayList<>();
        for (Database database : databases.values()) {
            if (!database.role()
                .isEmpty() && !roles.contains(database.role())) {
                roles.add(database.role());
            }
        }
        return Collections.unmodifiableList(roles);
    }

    @Override
    public String serverId() {
        return serverId.get();
    }

    /**
     * Спросить каждую базу тестовым запросом и написать сводку.
     *
     * <p>
     * Главный поток не ждёт никого: каждая база отвечает в своём рабочем потоке, строка сводки пишется,
     * когда ответили все. Отказ теста базу не помечает навсегда, следующий запрос снова пытается
     * соединиться.
     */
    public void checkAll() {
        if (databases.isEmpty()) {
            return;
        }
        List<CompletableFuture<?>> answers = new ArrayList<>();
        for (Database database : databases.values()) {
            if (database instanceof DatabaseImpl) {
                answers.add(
                    ((DatabaseImpl) database).check()
                        .thenAccept(status -> report(database, status)));
            } else {
                report(database, database.status());
            }
        }
        CompletableFuture.allOf(answers.toArray(new CompletableFuture<?>[0]))
            .thenRun(
                () -> log.info(
                    "Databases are up: {} of {} reachable, server id {}",
                    Integer.valueOf(reachable()),
                    Integer.valueOf(databases.size()),
                    serverId()));
    }

    private int reachable() {
        int count = 0;
        for (Database database : databases.values()) {
            if (database.status()
                .reachable()) {
                count++;
            }
        }
        return count;
    }

    private void report(Database database, com.mrleonardos.codecore.api.db.DbStatus status) {
        if (status.reachable()) {
            log.info(
                "Database {} ({}, {}): ok, {} ms",
                database.id(),
                label(database.role()),
                database.dialect(),
                Long.valueOf(status.pingMs()));
            return;
        }
        log.warn(
            "Database {} ({}, {}): unreachable, {}",
            database.id(),
            label(database.role()),
            database.dialect(),
            status.detail());
    }

    private static String label(String role) {
        return role.isEmpty() ? "no role" : role;
    }

    private static String names(List<Database> databases) {
        List<String> ids = new ArrayList<>();
        for (Database database : databases) {
            ids.add(database.id());
        }
        return String.join(", ", ids);
    }

    @Override
    public void close() {
        for (Database database : databases.values()) {
            if (database instanceof DatabaseImpl) {
                ((DatabaseImpl) database).close();
            }
        }
    }
}
