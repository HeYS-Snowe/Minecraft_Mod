package com.kill_line.animation.client.math;

/**
 * Easing functions for smooth animation curves.
 * All functions map [0,1] -> [0,1].
 */
public final class Easing {

    private Easing() {}

    // --- Basic ---

    public static float linear(float t) {
        return t;
    }

    // --- Quad ---

    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    // --- Cubic ---

    public static float easeInCubic(float t) {
        return t * t * t;
    }

    public static float easeOutCubic(float t) {
        float t1 = t - 1;
        return t1 * t1 * t1 + 1;
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f
                ? 4 * t * t * t
                : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    // --- Elastic ---

    public static float easeOutElastic(float t) {
        if (t == 0 || t == 1) return t;
        float p = 0.3f;
        float s = p / 4;
        return (float) Math.pow(2, -10 * t) * (float) Math.sin((t - s) * (2 * Math.PI) / p) + 1;
    }

    // --- Bounce ---

    public static float easeOutBounce(float t) {
        if (t < 1 / 2.75f) {
            return 7.5625f * t * t;
        } else if (t < 2 / 2.75f) {
            t -= 1.5f / 2.75f;
            return 7.5625f * t * t + 0.75f;
        } else if (t < 2.5f / 2.75f) {
            t -= 2.25f / 2.75f;
            return 7.5625f * t * t + 0.9375f;
        } else {
            t -= 2.625f / 2.75f;
            return 7.5625f * t * t + 0.984375f;
        }
    }

    // --- Back ---

    public static float easeOutBack(float t) {
        float s = 1.70158f;
        float t1 = t - 1;
        return t1 * t1 * ((s + 1) * t1 + s) + 1;
    }

    // --- Expo ---

    public static float easeInExpo(float t) {
        return t == 0 ? 0 : (float) Math.pow(2, 10 * (t - 1));
    }

    public static float easeOutExpo(float t) {
        return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
    }

    // --- Utility ---

    /** Hermite smoothstep interpolation */
    public static float smoothstep(float t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    /** Remap value from [inStart, inEnd] to [0, 1] and clamp */
    public static float remap01(float value, float inStart, float inEnd) {
        if (inEnd == inStart) return 0;
        return Math.max(0, Math.min(1, (value - inStart) / (inEnd - inStart)));
    }
}
