package com.mrleonardos.codecore.internal.client;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Потоки для всего, что нельзя делать в отрисовке.
 *
 * <p>
 * Один пул на клиент: и загрузка картинок, и запросы к сервисам аватаров ждут сеть, а не процессор, так
 * что разводить им отдельные пулы незачем. Потоки фоновые и с низким приоритетом — игра важнее.
 */
public final class ClientWorkers implements Executor {

    private static final String THREAD_NAME = "CodeCore client worker ";
    private static final int SIZE = 2;

    private final ExecutorService executor = Executors.newFixedThreadPool(SIZE, threadFactory());

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private static ThreadFactory threadFactory() {
        AtomicInteger counter = new AtomicInteger(1);
        return task -> {
            Thread thread = new Thread(task, THREAD_NAME + counter.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        };
    }
}
