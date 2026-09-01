package com.mrleonardos.codecore.internal.schedule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RunningSideTest {

    private static final boolean DEDICATED = true;
    private static final boolean CLIENT_PROCESS = false;

    @Test
    @DisplayName("на выделенном сервере серверная очередь достаётся любому потоку")
    void everyThreadOnDedicatedServerIsServerBound() {
        assertTrue(RunningSide.serverBound(DEDICATED, "pool-3-thread-1"), "поток пула JDBC");
        assertTrue(RunningSide.serverBound(DEDICATED, "Netty IO #1"));
        assertTrue(RunningSide.serverBound(DEDICATED, "Server thread"));
    }

    @Test
    @DisplayName("на клиенте серверная очередь достаётся только серверному потоку")
    void onlyTheServerThreadIsServerBoundOnClient() {
        assertTrue(RunningSide.serverBound(CLIENT_PROCESS, "Server thread"));
        assertFalse(RunningSide.serverBound(CLIENT_PROCESS, "Client thread"));
        assertFalse(RunningSide.serverBound(CLIENT_PROCESS, "CodeCore client worker 1"));
    }
}
