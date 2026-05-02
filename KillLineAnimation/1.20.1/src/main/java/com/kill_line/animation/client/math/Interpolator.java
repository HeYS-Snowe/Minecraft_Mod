package com.kill_line.animation.client.math;

import net.minecraft.util.Mth;

/**
 * Time-based interpolator that applies easing to a tick range.
 * Usage: Interpolator.of(Easing::easeOutCubic, 5, 30).getProgress(tick, tickDelta)
 */
public class Interpolator {

    private final EasingFunc easing;
    private final int startTick;
    private final int endTick;

    @FunctionalInterface
    public interface EasingFunc {
        float ease(float t);
    }

    private Interpolator(EasingFunc easing, int startTick, int endTick) {
        this.easing = easing;
        this.startTick = startTick;
        this.endTick = endTick;
    }

    public static Interpolator of(EasingFunc easing, int startTick, int endTick) {
        return new Interpolator(easing, startTick, endTick);
    }

    /** Linear interpolator from startTick to endTick */
    public static Interpolator linear(int startTick, int endTick) {
        return new Interpolator(Easing::linear, startTick, endTick);
    }

    /**
     * Returns eased progress [0, 1] for the current tick + partial tick.
     * Before startTick -> 0, after endTick -> 1.
     */
    public float getProgress(int tick, float tickDelta) {
        float rawProgress = (tick - startTick + tickDelta) / (float) (endTick - startTick);
        rawProgress = Mth.clamp(rawProgress, 0.0f, 1.0f);
        return easing.ease(rawProgress);
    }

    /** Get eased progress using integer tick only (no interpolation) */
    public float getProgress(int tick) {
        return getProgress(tick, 0);
    }

    /** Whether this interpolator is currently in its active range */
    public boolean isActive(int tick) {
        return tick >= startTick && tick < endTick;
    }

    /** Whether this interpolator has completed */
    public boolean isFinished(int tick) {
        return tick >= endTick;
    }

    public int getStartTick() { return startTick; }
    public int getEndTick() { return endTick; }
}
