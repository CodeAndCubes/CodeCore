package com.mrleonardos.codecore.internal.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongSupplier;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.db.DatabaseBusyException;
import com.mrleonardos.codecore.api.db.DatabaseUnavailableException;

/**
 * Пул соединений одной базы.
 *
 * <p>
 * Своими руками, без сторонней библиотеки: на нагрузке линейки (сотни операций в час) двести строк
 * понятнее зависимости, и в jar ядра не едет ничего лишнего.
 *
 * <p>
 * Мод получает не само соединение, а обёртку: закрытие возвращает соединение в пул, а не рвёт связь,
 * поэтому {@code try-with-resources} остаётся правильным способом работы. Каждый созданный оператор
 * получает таймаут из настроек записи, чтобы зависший запрос не держал соединение вечно.
 *
 * <p>
 * Новое соединение открывается под замком пула. На один рабочий поток базы это ничего не стоит, а
 * простота важнее: очередь на открытие бывает разве что в первые секунды после старта.
 */
public final class ConnectionPool implements AutoCloseable {

    private static final int VALID_TIMEOUT_SECONDS = 1;
    private static final int OPEN_ATTEMPTS = 2;
    private static final Class<?>[] CONNECTION_INTERFACE = { Connection.class };

    private final String id;
    private final ConnectionSource source;
    private final int size;
    private final long borrowTimeoutMs;
    private final int queryTimeoutSeconds;
    private final long idleTimeoutMs;
    private final LongSupplier clock;
    private final Logger log;

    private final Deque<Idle> free = new ArrayDeque<>();
    private int leased;
    private boolean closed;

    public ConnectionPool(String id, ConnectionSource source, int size, long borrowTimeoutMs, int queryTimeoutMs,
        long idleTimeoutSeconds, LongSupplier clock, Logger log) {
        this.id = id;
        this.source = source;
        this.size = size;
        this.borrowTimeoutMs = borrowTimeoutMs;
        this.queryTimeoutSeconds = Math.max(1, queryTimeoutMs / 1000);
        this.idleTimeoutMs = idleTimeoutSeconds * 1000L;
        this.clock = clock;
        this.log = log;
    }

    /**
     * Соединение из пула.
     *
     * @throws DatabaseBusyException        если за отведённое время свободного не нашлось
     * @throws DatabaseUnavailableException если база не отвечает
     */
    public Connection borrow() {
        long deadline = clock.getAsLong() + borrowTimeoutMs;
        synchronized (this) {
            while (true) {
                if (closed) {
                    throw new DatabaseUnavailableException("Database " + id + " is closed");
                }
                Connection alive = takeAlive();
                if (alive != null) {
                    leased++;
                    return handed(alive);
                }
                if (free.size() + leased < size) {
                    Connection fresh = open();
                    leased++;
                    return handed(fresh);
                }
                long left = deadline - clock.getAsLong();
                if (left <= 0) {
                    log.warn(
                        "Database {}: all {} connections are busy for longer than {} ms",
                        id,
                        Integer.valueOf(size),
                        Long.valueOf(borrowTimeoutMs));
                    throw new DatabaseBusyException(
                        "Database " + id
                            + ": a pool of "
                            + size
                            + " connections stayed busy for "
                            + borrowTimeoutMs
                            + " ms");
                }
                await(left);
            }
        }
    }

    /** Сколько соединений сейчас на руках: смотрят тесты и сводка. */
    public synchronized int leased() {
        return leased;
    }

    /** Сколько соединений лежит свободными. */
    public synchronized int idle() {
        return free.size();
    }

    @Override
    public synchronized void close() {
        closed = true;
        for (Idle idle : free) {
            quietClose(idle.connection);
        }
        free.clear();
        notifyAll();
    }

    private void await(long millis) {
        try {
            wait(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread()
                .interrupt();
            throw new DatabaseUnavailableException(
                "Database " + id + ": waiting for a connection was interrupted",
                interrupted);
        }
    }

    private Connection takeAlive() {
        while (!free.isEmpty()) {
            Idle idle = free.poll();
            if (clock.getAsLong() - idle.since > idleTimeoutMs) {
                quietClose(idle.connection);
                continue;
            }
            if (!valid(idle.connection)) {
                log.warn("Database {}: a pooled connection is dead, taking another one", id);
                quietClose(idle.connection);
                continue;
            }
            return idle.connection;
        }
        return null;
    }

    private Connection open() {
        SQLException last = null;
        for (int attempt = 0; attempt < OPEN_ATTEMPTS; attempt++) {
            try {
                Connection fresh = source.open();
                if (valid(fresh)) {
                    return fresh;
                }
                quietClose(fresh);
                last = null;
            } catch (SQLException failure) {
                last = failure;
            }
        }
        throw new DatabaseUnavailableException("Database " + id + " does not answer", last);
    }

    private boolean valid(Connection connection) {
        try {
            return connection.isValid(VALID_TIMEOUT_SECONDS);
        } catch (SQLException failure) {
            return false;
        }
    }

    private void quietClose(Connection connection) {
        try {
            connection.close();
        } catch (SQLException failure) {
            log.warn("Database {}: a connection did not close, {}", id, failure.getMessage());
        }
    }

    private synchronized void giveBack(Connection raw) {
        leased--;
        if (closed) {
            quietClose(raw);
        } else {
            free.addFirst(new Idle(raw, clock.getAsLong()));
        }
        notifyAll();
    }

    private Connection handed(Connection raw) {
        return (Connection) Proxy
            .newProxyInstance(Connection.class.getClassLoader(), CONNECTION_INTERFACE, new Handed(raw));
    }

    private static final class Idle {

        private final Connection connection;
        private final long since;

        private Idle(Connection connection, long since) {
            this.connection = connection;
            this.since = since;
        }
    }

    private final class Handed implements InvocationHandler {

        private final Connection raw;
        private boolean returned;

        private Handed(Connection raw) {
            this.raw = raw;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                if (!returned) {
                    returned = true;
                    giveBack(raw);
                }
                return null;
            }
            if ("isClosed".equals(name)) {
                return Boolean.valueOf(returned || raw.isClosed());
            }
            if (returned) {
                throw new SQLException("This connection is already back in the pool of database " + id);
            }
            try {
                Object result = method.invoke(raw, args);
                if (result instanceof Statement) {
                    ((Statement) result).setQueryTimeout(queryTimeoutSeconds);
                }
                return result;
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
        }
    }
}
