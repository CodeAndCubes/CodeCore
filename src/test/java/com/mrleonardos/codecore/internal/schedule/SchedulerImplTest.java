package com.mrleonardos.codecore.internal.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cpw.mods.fml.relauncher.Side;

class SchedulerImplTest {

    private static final Logger LOG = LogManager.getLogger(SchedulerImplTest.class);

    private final MainThreadQueue server = new MainThreadQueue("server", LOG);
    private final MainThreadQueue client = new MainThreadQueue("client", LOG);
    private final AtomicInteger onServer = new AtomicInteger();
    private final AtomicInteger onClient = new AtomicInteger();

    @Test
    @DisplayName("на выделенном сервере задача из фонового потока идёт в серверную очередь")
    void backgroundThreadOnDedicatedServerGoesToTheServerQueue() {
        SchedulerImpl scheduler = new SchedulerImpl(server, client, () -> true);

        scheduler.onMainThread(onServer::incrementAndGet);
        scheduler.afterTicks(1, onServer::incrementAndGet);
        drain();

        assertEquals(2, onServer.get());
        assertEquals(0, onClient.get(), "клиентскую очередь на выделенном сервере никто не крутит");
    }

    @Test
    @DisplayName("на клиенте задача из фонового потока идёт в клиентскую очередь")
    void backgroundThreadOnClientGoesToTheClientQueue() {
        SchedulerImpl scheduler = new SchedulerImpl(server, client, () -> false);

        scheduler.onMainThread(onClient::incrementAndGet);
        drain();

        assertEquals(1, onClient.get());
        assertEquals(0, onServer.get());
    }

    @Test
    @DisplayName("явное указание стороны сильнее того, откуда сделан вызов")
    void explicitSideWins() {
        SchedulerImpl scheduler = new SchedulerImpl(server, client, () -> false);

        scheduler.onServerThread(onServer::incrementAndGet);
        scheduler.onClientThread(onClient::incrementAndGet);
        scheduler.queue(Side.SERVER)
            .submit(onServer::incrementAndGet);
        scheduler.queue(Side.CLIENT)
            .submit(onClient::incrementAndGet);
        drain();

        assertEquals(2, onServer.get());
        assertEquals(2, onClient.get());
    }

    private void drain() {
        server.drain();
        client.drain();
    }
}
