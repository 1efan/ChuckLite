package com.chunklite.optimizer;

import com.chunklite.ChuckLiteConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Smooths out chunk-loading spikes by capping how many chunks the
 * client processes per tick.
 *
 * <p>When joining a server the client receives a burst of chunk-data
 * packets. Processing them all in a single tick causes frame-time
 * spikes. This throttle queues surplus work and spreads it across
 * subsequent ticks.</p>
 */
public final class ChunkLoadThrottle {

    /** How many loads we have allowed so far in the current tick. */
    private final AtomicInteger loadsThisTick = new AtomicInteger(0);

    /** Maximum loads per tick, refreshed from config each tick. */
    private volatile int perTickLimit = 12;

    /**
     * Pending chunk operations that couldn't be serviced this tick.
     * Each entry is a {@code Runnable} that performs the actual work.
     */
    private final Deque<Runnable> pending = new ArrayDeque<>();

    /** Call once per client tick to drain pending work. */
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

    /**
     * Attempt to run {@code work} immediately. If this tick's budget is
     * exhausted the work is queued for a later tick.
     */
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

    /** Returns true if there is queued work waiting to be processed. */
    public boolean hasPending() {
        return !pending.isEmpty();
    }

    /** Returns the number of queued operations. */
    public int pendingCount() {
        return pending.size();
    }

    /** Discard all queued operations (e.g. when disconnecting). */
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
