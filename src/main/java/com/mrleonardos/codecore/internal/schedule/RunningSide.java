package com.mrleonardos.codecore.internal.schedule;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * Какая очередь главного потока обслуживает текущий поток.
 *
 * <p>
 * Спрашивать {@code getEffectiveSide()} здесь нельзя: в 1.7.10 он узнаёт сервер по имени потока
 * {@code Server thread} и любому другому потоку отвечает CLIENT. На выделенном сервере клиентскую очередь
 * никто не крутит, поэтому задача из пула JDBC или из потока загрузки просто исчезала бы.
 *
 * <p>
 * Правило простое: на выделенном сервере серверная очередь всегда; на клиенте серверная только тому
 * потоку, который и есть серверный, остальным клиентская.
 */
final class RunningSide {

    private static final String SERVER_THREAD = "Server thread";

    private RunningSide() {}

    static boolean serverBound() {
        return serverBound(
            dedicated(),
            Thread.currentThread()
                .getName());
    }

    /** Правило целиком, без обращения к игре: сюда и смотрят тесты. */
    static boolean serverBound(boolean dedicated, String threadName) {
        return dedicated || SERVER_THREAD.equals(threadName);
    }

    private static boolean dedicated() {
        return FMLCommonHandler.instance()
            .getSide() == Side.SERVER;
    }
}
