package com.mrleonardos.codecore.internal.schedule;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Крутит очереди главного потока: серверную в серверном тике, клиентскую в клиентском.
 *
 * <p>
 * Обе подписки живут в одном классе намеренно. На выделенном сервере клиентское событие просто никогда не
 * приходит, а разносить это по сторонам значило бы городить две почти одинаковые пары классов.
 *
 * <p>
 * Здесь же видно, идёт ли тик вообще. Это нужно слою баз: держать тик запросом к базе нельзя, а вот
 * подъём при старте и запись при остановке блокируют поток, в котором тика ещё (или уже) нет.
 */
public final class TickDriver {

    private final MainThreadQueue server;
    private final MainThreadQueue client;

    private volatile boolean ticking;

    public TickDriver(MainThreadQueue server, MainThreadQueue client) {
        this.server = server;
        this.client = client;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ticking = true;
            server.drain();
        }
    }

    /** Идёт ли серверный тик прямо сейчас: между первым тиком и остановкой сервера. */
    public boolean ticking() {
        return ticking;
    }

    /** Сервер остановился: тика больше нет, и блокировать главный поток снова можно. */
    public void stopped() {
        ticking = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            client.drain();
        }
    }
}
