package com.chunklite.optimizer;

/**
 * A rolling measure of how long recent client frames have been taking. Fed one sample per rendered
 * frame, it keeps an exponential moving average so a single hitch does not swing the value, which is
 * what the adaptive budget reads to decide how much chunk work a frame can afford.
 */
public final class FrameTimeTracker {

    private static final double DEFAULT_MILLIS = 1000.0 / 60.0;
    private static final double SMOOTHING = 0.1;

    private double averageMillis = DEFAULT_MILLIS;
    private long lastFrameNanos = 0L;

    /** Record the current frame. Call once per rendered frame with {@code System.nanoTime()}. */
    public void onFrame(long nowNanos) {
        if (lastFrameNanos != 0L) {
            double frameMillis = (nowNanos - lastFrameNanos) / 1_000_000.0;
            // Ignore non-positive deltas and long pauses (loading screens, alt-tab) so they do not
            // poison the average.
            if (frameMillis > 0.0 && frameMillis < 1000.0) {
                averageMillis += (frameMillis - averageMillis) * SMOOTHING;
            }
        }
        lastFrameNanos = nowNanos;
    }

    public double averageMillis() {
        return averageMillis;
    }

    public double averageFps() {
        return averageMillis <= 0.0 ? 0.0 : 1000.0 / averageMillis;
    }

    public void reset() {
        lastFrameNanos = 0L;
        averageMillis = DEFAULT_MILLIS;
    }
}
