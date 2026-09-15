package com.mrleonardos.codecore.internal.db;

import java.util.function.BooleanSupplier;

import com.mrleonardos.codecore.internal.ThreadNames;

/**
 * Тот ли это поток, в котором идёт тик.
 *
 * <p>
 * Правило по имени потока, а не по классам игры: слой баз обходится java.sql и jdk, а имена главных
 * потоков в 1.7.10 постоянны. Рабочие потоки баз зовутся {@code codecore-db-<имя>} и под правило не
 * попадают.
 */
public final class MainThreads {

    private MainThreads() {}

    public static boolean isMain(String threadName) {
        return ThreadNames.SERVER.equals(threadName) || ThreadNames.CLIENT.equals(threadName);
    }

    /** Проверка для текущего потока: её и держит у себя каждая база. */
    public static BooleanSupplier current() {
        return () -> isMain(
            Thread.currentThread()
                .getName());
    }
}
