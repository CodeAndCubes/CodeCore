package com.mrleonardos.codecore.internal.db;

import java.util.function.BooleanSupplier;

/**
 * Тот ли это поток, в котором идёт тик.
 *
 * <p>
 * Правило по имени потока, а не по классам игры: слой баз обходится java.sql и jdk, а имена главных
 * потоков в 1.7.10 постоянны. Рабочие потоки баз зовутся {@code codecore-db-<имя>} и под правило не
 * попадают.
 */
public final class MainThreads {

    private static final String SERVER = "Server thread";
    private static final String CLIENT = "Client thread";

    private MainThreads() {}

    public static boolean isMain(String threadName) {
        return SERVER.equals(threadName) || CLIENT.equals(threadName);
    }

    /** Проверка для текущего потока: её и держит у себя каждая база. */
    public static BooleanSupplier current() {
        return () -> isMain(
            Thread.currentThread()
                .getName());
    }
}
