package com.mrleonardos.codecore.internal;

/**
 * Имена главных потоков игры.
 *
 * <p>
 * Правило одного места: очередь планировщика и слой баз узнают главный поток по имени, и имена эти в
 * 1.7.10 постоянны. Второй источник рано или поздно разошёлся бы с первым молча.
 */
public final class ThreadNames {

    /** Поток, в котором тикает выделенный и встроенный сервер. */
    public static final String SERVER = "Server thread";

    /** Поток, в котором тикает клиент. */
    public static final String CLIENT = "Client thread";

    private ThreadNames() {}
}
