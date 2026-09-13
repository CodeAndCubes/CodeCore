package com.mrleonardos.codecore.internal.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Подставной источник соединений.
 *
 * <p>
 * Пул проверяется без СУБД и без сети: ожидание, таймаут, возврат соединения и закрытие простоявших
 * видны на заглушке, а настоящая база тут ничего не добавила бы, кроме секунд прогона.
 */
final class FakeConnections implements ConnectionSource {

    private final List<Fake> opened = new ArrayList<>();
    private boolean nextIsDead;
    private SQLException nextFailure;

    @Override
    public Connection open() throws SQLException {
        if (nextFailure != null) {
            SQLException failure = nextFailure;
            nextFailure = null;
            throw failure;
        }
        Fake fake = new Fake(!nextIsDead);
        nextIsDead = false;
        opened.add(fake);
        return (Connection) Proxy
            .newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, fake);
    }

    /** Сколько раз источник открывал соединение. */
    int opened() {
        return opened.size();
    }

    /** Закрыто ли соединение с этим номером. */
    boolean closed(int index) {
        return opened.get(index).closed;
    }

    /** Сделать уже выданное соединение мёртвым: так ведёт себя разорванная связь. */
    void kill(int index) {
        opened.get(index).valid = false;
    }

    /** Следующее соединение откроется мёртвым. */
    void openDeadNext() {
        nextIsDead = true;
    }

    /** Следующая попытка открыть соединение отказывает. */
    void failNext(String reason) {
        nextFailure = new SQLException(reason);
    }

    private static final class Fake implements InvocationHandler {

        private boolean valid;
        private boolean closed;

        private Fake(boolean valid) {
            this.valid = valid;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "isValid":
                    return Boolean.valueOf(valid && !closed);
                case "close":
                    closed = true;
                    return null;
                case "isClosed":
                    return Boolean.valueOf(closed);
                case "getAutoCommit":
                    return Boolean.TRUE;
                case "hashCode":
                    return Integer.valueOf(System.identityHashCode(proxy));
                case "equals":
                    return Boolean.valueOf(proxy == args[0]);
                case "toString":
                    return "fake connection";
                default:
                    return null;
            }
        }
    }
}
