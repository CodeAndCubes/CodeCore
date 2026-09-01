package com.mrleonardos.codecore.internal.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainThreadQueueTest {

    private static final Logger LOG = LogManager.getLogger(MainThreadQueueTest.class);

    @Test
    @DisplayName("накопленные задачи выполняются в порядке постановки")
    void tasksRunInOrder() {
        MainThreadQueue queue = new MainThreadQueue("server", LOG);
        List<String> done = new ArrayList<>();

        queue.submit(() -> done.add("first"));
        queue.submit(() -> done.add("second"));
        assertTrue(done.isEmpty(), "до тика ничего не выполняется");

        queue.drain();

        assertEquals(Arrays.asList("first", "second"), done);
    }

    @Test
    @DisplayName("упавшая задача не мешает следующей")
    void failureDoesNotStopTheQueue() {
        MainThreadQueue queue = new MainThreadQueue("server", LOG);
        AtomicInteger ran = new AtomicInteger();

        queue.submit(() -> { throw new IllegalStateException("нарочно"); });
        queue.submit(ran::incrementAndGet);
        queue.drain();

        assertEquals(1, ran.get());
    }

    @Test
    @DisplayName("отложенная задача ждёт свой тик")
    void delayedTaskWaitsForItsTick() {
        MainThreadQueue queue = new MainThreadQueue("server", LOG);
        AtomicInteger ran = new AtomicInteger();

        queue.submitAfter(3, ran::incrementAndGet);
        queue.drain();
        queue.drain();
        assertEquals(0, ran.get(), "два тика прошло, третий ещё нет");

        queue.drain();
        assertEquals(1, ran.get());

        queue.drain();
        assertEquals(1, ran.get(), "задача выполняется один раз");
    }

    @Test
    @DisplayName("нулевая задержка равнозначна обычной постановке")
    void zeroDelayIsImmediate() {
        MainThreadQueue queue = new MainThreadQueue("server", LOG);
        AtomicInteger ran = new AtomicInteger();

        queue.submitAfter(0, ran::incrementAndGet);
        queue.submitAfter(-5, ran::incrementAndGet);
        queue.drain();

        assertEquals(2, ran.get());
    }

    @Test
    @DisplayName("фоновый поток видит текущий тик, а не закэшированный ноль")
    void backgroundThreadSeesTheCurrentTick() throws InterruptedException {
        MainThreadQueue queue = new MainThreadQueue("server", LOG);
        for (int tick = 0; tick < 1000; tick++) {
            queue.drain();
        }

        AtomicInteger ran = new AtomicInteger();
        Thread background = new Thread(() -> queue.submitAfter(10, ran::incrementAndGet));
        background.start();
        background.join();

        for (int tick = 0; tick < 9; tick++) {
            queue.drain();
        }
        assertEquals(0, ran.get(), "задача поставлена на 1010-й тик, а не на 10-й");

        queue.drain();
        assertEquals(1, ran.get());
    }

    @Test
    @DisplayName("очистка выбрасывает и обычные, и отложенные задачи")
    void clearForgetsEverything() {
        MainThreadQueue queue = new MainThreadQueue("server", LOG);
        AtomicInteger ran = new AtomicInteger();

        queue.submit(ran::incrementAndGet);
        queue.submitAfter(2, ran::incrementAndGet);
        queue.clear();
        queue.drain();
        queue.drain();
        queue.drain();

        assertEquals(0, ran.get());
    }
}
