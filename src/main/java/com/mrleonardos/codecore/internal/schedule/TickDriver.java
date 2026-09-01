package com.mrleonardos.codecore.internal.schedule;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Крутит очереди главного потока: серверную в серверном тике, клиентскую в клиентском.
 *
 * <p>
 * Обе подписки живут в одном классе намеренно. На выделенном сервере клиентское событие просто никогда не
 * приходит, а разносить это по сторонам значило бы городить две почти одинаковые пары классов.
 */
public final class TickDriver {

    private final MainThreadQueue server;
    private final MainThreadQueue client;

    public TickDriver(MainThreadQueue server, MainThreadQueue client) {
        this.server = server;
        this.client = client;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            server.drain();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            client.drain();
        }
    }
}
