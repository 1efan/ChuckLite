package com.chunklite.optimizer;

/**
 * Turns the recent average frame time into a per-frame allowance of chunk work. When frames sit
 * comfortably under the target time there is spare headroom, so the allowance grows and more chunk
 * work is permitted; when frames run long the allowance shrinks so chunk work stops making a slow
 * frame worse. The allowance eases one step at a time rather than jumping, so the rate of chunk work
 * changes smoothly instead of oscillating.
 */
public final class AdaptiveBudget {

    private double allowance;

    public AdaptiveBudget(int initial) {
        this.allowance = initial;
    }

    /**
     * Nudge the allowance toward what the current frame time can support.
     *
     * @param averageFrameMillis recent average frame time
     * @param targetFrameMillis  the frame time we are trying to stay under
     * @param min                lowest allowance permitted
     * @param max                highest allowance permitted
     */
    public void update(double averageFrameMillis, double targetFrameMillis, int min, int max) {
        double headroom = targetFrameMillis - averageFrameMillis;
        // One eased step, sized by how far off target we are but capped to +/-1 so it never snaps.
        double step = headroom / Math.max(1.0, targetFrameMillis);
        step = Math.max(-1.0, Math.min(1.0, step));
        allowance += step;

        if (allowance < min) {
            allowance = min;
        } else if (allowance > max) {
            allowance = max;
        }
    }

    /** How many units of chunk work this frame is allowed to do. */
    public int perFrame() {
        return (int) Math.round(allowance);
    }

    public void snap(int value) {
        this.allowance = value;
    }
}
