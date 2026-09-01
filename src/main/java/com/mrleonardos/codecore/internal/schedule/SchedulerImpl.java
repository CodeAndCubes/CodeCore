package com.mrleonardos.codecore.internal.schedule;

import java.util.function.BooleanSupplier;

import com.mrleonardos.codecore.api.util.Scheduler;

import cpw.mods.fml.relauncher.Side;

/** Выбирает очередь по стороне, которой принадлежит текущий поток. */
public final class SchedulerImpl implements Scheduler {

    private final MainThreadQueue server;
    private final MainThreadQueue client;
    private final BooleanSupplier serverBound;

    public SchedulerImpl(MainThreadQueue server, MainThreadQueue client) {
        this(server, client, RunningSide::serverBound);
    }

    public SchedulerImpl(MainThreadQueue server, MainThreadQueue client, BooleanSupplier serverBound) {
        this.server = server;
        this.client = client;
        this.serverBound = serverBound;
    }

    @Override
    public void onMainThread(Runnable task) {
        queue().submit(task);
    }

    @Override
    public void onServerThread(Runnable task) {
        server.submit(task);
    }

    @Override
    public void onClientThread(Runnable task) {
        client.submit(task);
    }

    @Override
    public void afterTicks(int ticks, Runnable task) {
        queue().submitAfter(ticks, task);
    }

    /** Очередь конкретной стороны: нужна сети, где сторона известна из контекста пакета. */
    public MainThreadQueue queue(Side side) {
        return side == Side.CLIENT ? client : server;
    }

    private MainThreadQueue queue() {
        return serverBound.getAsBoolean() ? server : client;
    }
}
