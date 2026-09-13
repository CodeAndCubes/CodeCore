package com.mrleonardos.codecore.internal.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.db.DatabaseBusyException;
import com.mrleonardos.codecore.api.db.DatabaseUnavailableException;

class ConnectionPoolTest {

    private static final Logger LOG = LogManager.getLogger(ConnectionPoolTest.class);

    private final FakeConnections source = new FakeConnections();
    private final AtomicLong clock = new AtomicLong(1000L);

    private ConnectionPool pool(int size, long borrowTimeoutMs, long idleSeconds) {
        return new ConnectionPool("test", source, size, borrowTimeoutMs, 5000, idleSeconds, clock::get, LOG);
    }

    @Test
    @DisplayName("закрытие соединения возвращает его в пул, а не рвёт связь")
    void closingGivesTheConnectionBack() throws SQLException {
        ConnectionPool pool = pool(2, 1000L, 60L);

        Connection first = pool.borrow();
        first.close();
        Connection second = pool.borrow();

        assertEquals(1, source.opened(), "второе соединение брать было неоткуда");
        assertFalse(source.closed(0), "соединение закрыто по-настоящему, хотя должно было лечь в пул");
        assertEquals(1, pool.leased());
        second.close();
        assertEquals(0, pool.leased());
    }

    @Test
    @DisplayName("исчерпанный пул ждёт своё время и отвечает занятостью")
    void anExhaustedPoolAnswersBusy() throws Exception {
        ConnectionPool pool = new ConnectionPool("test", source, 1, 120L, 5000, 60L, System::currentTimeMillis, LOG);
        Connection held = pool.borrow();

        long started = System.currentTimeMillis();
        assertThrows(DatabaseBusyException.class, pool::borrow);
        long waited = System.currentTimeMillis() - started;

        assertTrue(waited >= 100L, "ждать надо всё отведённое время, ждали " + waited + " мс");
        assertEquals(1, pool.leased(), "занятое соединение продолжает работать");

        held.close();

        Connection again = pool.borrow();
        assertNotNull(again, "освободившееся соединение выдаётся следующему");
        again.close();
    }

    @Test
    @DisplayName("соединение, простоявшее дольше срока, закрывается, а взамен открывается свежее")
    void anIdleConnectionIsClosed() throws SQLException {
        ConnectionPool pool = pool(2, 1000L, 5L);

        Connection first = pool.borrow();
        first.close();
        clock.addAndGet(6000L);
        Connection second = pool.borrow();

        assertEquals(2, source.opened());
        assertTrue(source.closed(0), "простоявшее соединение должно было закрыться");
        second.close();
    }

    @Test
    @DisplayName("мёртвое соединение из пула не выдаётся: берётся следующее")
    void aDeadPooledConnectionIsSkipped() throws SQLException {
        ConnectionPool pool = pool(2, 1000L, 60L);

        Connection first = pool.borrow();
        first.close();
        source.kill(0);
        Connection second = pool.borrow();

        assertEquals(2, source.opened());
        assertTrue(source.closed(0));
        assertNotSame(first, second);
        second.close();
    }

    @Test
    @DisplayName("две неудачи подряд при открытии дают недоступность, а не молчание")
    void twoFailuresGiveUnavailable() {
        ConnectionPool pool = pool(1, 100L, 60L);
        source.failNext("сеть недоступна");
        source.openDeadNext();

        assertThrows(DatabaseUnavailableException.class, pool::borrow);
    }

    @Test
    @DisplayName("закрытый пул закрывает свободные соединения и больше ничего не выдаёт")
    void aClosedPoolHandsOutNothing() throws SQLException {
        ConnectionPool pool = pool(2, 100L, 60L);
        Connection first = pool.borrow();
        first.close();

        pool.close();

        assertTrue(source.closed(0));
        assertThrows(DatabaseUnavailableException.class, pool::borrow);
    }
}
