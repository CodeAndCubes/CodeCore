package com.mrleonardos.codecore.internal.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;

/**
 * Очередь задач главного потока одной стороны.
 *
 * <p>
 * Приём задач потокобезопасен, выполнение происходит в тике. Упавшая задача пишется в лог и не мешает
 * остальным: одна ошибка в обработчике пакета не должна ронять тик сервера.
 *
 * <p>
 * Счётчик тиков пишет главный поток, а читают его фоновые, поэтому он {@code volatile}: иначе фоновый
 * поток видел бы значение часовой давности и отложенная задача выполнялась бы в ближайшем тике.
 */
public final class MainThreadQueue {

    private final Queue<Runnable> immediate = new ConcurrentLinkedQueue<>();
    private final Queue<DelayedTask> delayed = new ConcurrentLinkedQueue<>();
    private final Logger log;
    private final String name;
    private volatile long currentTick;

    public MainThreadQueue(String name, Logger log) {
        this.name = name;
        this.log = log;
    }

    public void submit(Runnable task) {
        immediate.add(task);
    }

    public void submitAfter(int ticks, Runnable task) {
        if (ticks <= 0) {
            submit(task);
            return;
        }
        delayed.add(new DelayedTask(currentTick + ticks, task));
    }

    /** Выполнить всё, что накопилось к этому тику. Вызывается только из главного потока. */
    public void drain() {
        long tick = currentTick + 1;
        currentTick = tick;

        Runnable task;
        while ((task = immediate.poll()) != null) {
            run(task);
        }

        if (delayed.isEmpty()) {
            return;
        }
        List<Runnable> due = new ArrayList<>();
        for (Iterator<DelayedTask> iterator = delayed.iterator(); iterator.hasNext();) {
            DelayedTask candidate = iterator.next();
            if (candidate.tick <= tick) {
                due.add(candidate.task);
                iterator.remove();
            }
        }
        for (Runnable ready : due) {
            run(ready);
        }
    }

    /** Забыть всё несделанное: сторона выключается. */
    public void clear() {
        immediate.clear();
        delayed.clear();
    }

    /** Сколько тиков очередь уже отработала. */
    public long tick() {
        return currentTick;
    }

    private void run(Runnable task) {
        try {
            task.run();
        } catch (Throwable failure) {
            log.error("Task on the {} main thread failed", name, failure);
        }
    }

    private static final class DelayedTask {

        private final long tick;
        private final Runnable task;

        private DelayedTask(long tick, Runnable task) {
            this.tick = tick;
            this.task = task;
        }
    }
}
