package com.chunklite.optimizer;

import com.chunklite.ChuckLiteConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChunkLoadThrottle {

    private final AtomicInteger loadsThisTick = new AtomicInteger(0);

    private volatile int perTickLimit = 12;

    private final Deque<Runnable> pending = new ArrayDeque<>();

    public void tick() {
        loadsThisTick.set(0);
        perTickLimit = ChuckLiteConfig.maxChunkLoadsPerTick();

        if (!ChuckLiteConfig.throttleEnabled()) {
            drainAll();
            return;
        }

        int processed = 0;
        while (processed < perTickLimit && !pending.isEmpty()) {
            Runnable task = pending.poll();
            if (task != null) {
                task.run();
                processed++;
            }
        }
        loadsThisTick.set(processed);
    }

    public void schedule(Runnable work) {
        if (!ChuckLiteConfig.throttleEnabled()) {
            work.run();
            return;
        }

        int current = loadsThisTick.incrementAndGet();
        if (current <= perTickLimit) {
            work.run();
        } else {
            pending.offer(work);
        }
    }

    public boolean hasPending() {
        return !pending.isEmpty();
    }

    public int pendingCount() {
        return pending.size();
    }

    public void clear() {
        pending.clear();
        loadsThisTick.set(0);
    }

    private void drainAll() {
        Runnable task;
        while ((task = pending.poll()) != null) {
            task.run();
        }
    }
}
