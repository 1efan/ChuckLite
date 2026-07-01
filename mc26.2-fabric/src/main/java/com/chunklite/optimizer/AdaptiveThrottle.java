package com.chunklite.optimizer;

import com.chunklite.ChuckLiteConfig;

// adaptive throttle for 26.1. static so the mixin-only neoforge build can reach it (no entrypoint).
// 26.1 reworked the render pipeline (no toUpload queue), so instead of capping uploads we make the
// existing chunk-batch throttle adaptive: frame time feeds a budget, ChunkBatchThrottleMixin caps
// getDesiredChunksPerTick to it.
public final class AdaptiveThrottle {

    private AdaptiveThrottle() {
    }

    private static final int INITIAL_CAP = 12;

    private static final FrameTimeTracker frameTime = new FrameTimeTracker();
    private static final AdaptiveBudget budget = new AdaptiveBudget(INITIAL_CAP);

    public static void onFrame(long nowNanos) {
        frameTime.onFrame(nowNanos);
        if (ChuckLiteConfig.adaptiveThrottle()) {
            double targetMillis = 1000.0 / Math.max(1, ChuckLiteConfig.throttleTargetFps());
            budget.update(
                frameTime.averageMillis(),
                targetMillis,
                ChuckLiteConfig.throttleMinPerFrame(),
                ChuckLiteConfig.throttleMaxPerFrame()
            );
        }
    }

    public static int chunkPerTickCap() {
        return budget.perFrame();
    }

    public static double averageMillis() {
        return frameTime.averageMillis();
    }

    public static double averageFps() {
        return frameTime.averageFps();
    }

    public static void reset() {
        frameTime.reset();
        budget.snap(INITIAL_CAP);
    }
}
