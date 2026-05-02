package com.kill_line.animation.client.api;

import com.kill_line.animation.client.math.PhysicsConstants;

/**
 * An animated model fragment with configurable physics, trail history, and color overlay.
 */
public class AnimatedFragment {

    // --- Position & Transform ---
    private double offsetX, offsetY, offsetZ;
    private float rotationX, rotationY, rotationZ;
    private float scale = 1.0f;
    private float alpha = 1.0f;

    // --- Previous frame (for tickDelta interpolation) ---
    private double prevOffsetX, prevOffsetY, prevOffsetZ;
    private float prevRotationX, prevRotationY, prevRotationZ;

    // --- Velocity ---
    private double velocityX, velocityY, velocityZ;
    private float angularVelocityX, angularVelocityY, angularVelocityZ;

    // --- Physics config ---
    private boolean gravity = true;
    private double gravityAccel = PhysicsConstants.GRAVITY_NORMAL;
    private double bounceFactor = PhysicsConstants.BOUNCE_STANDARD;
    private double drag = PhysicsConstants.DRAG_STANDARD;
    private double angularDrag = PhysicsConstants.ANGULAR_DRAG_STANDARD;
    private double groundFriction = PhysicsConstants.GROUND_FRICTION;
    private float groundY = 0.0f;

    // --- Trail history (circular buffer of recent positions) ---
    private static final int TRAIL_LENGTH = 5;
    private final double[] trailX = new double[TRAIL_LENGTH];
    private final double[] trailY = new double[TRAIL_LENGTH];
    private final double[] trailZ = new double[TRAIL_LENGTH];
    private int trailCount = 0;
    private boolean trailEnabled = false;

    // --- Color overlay (ARGB) ---
    private int colorOverlay = 0xFFFFFFFF; // no tint by default

    private final String partName;

    public AnimatedFragment(String partName) {
        this.partName = partName;
    }

    // --- Physics tick ---

    public void tick() {
        // Save previous frame state for interpolation
        prevOffsetX = offsetX;
        prevOffsetY = offsetY;
        prevOffsetZ = offsetZ;
        prevRotationX = rotationX;
        prevRotationY = rotationY;
        prevRotationZ = rotationZ;

        // Record trail position before moving
        if (trailEnabled) {
            if (trailCount < TRAIL_LENGTH) trailCount++;
            System.arraycopy(trailX, 0, trailX, 1, trailCount - 1);
            System.arraycopy(trailY, 0, trailY, 1, trailCount - 1);
            System.arraycopy(trailZ, 0, trailZ, 1, trailCount - 1);
            trailX[0] = offsetX;
            trailY[0] = offsetY;
            trailZ[0] = offsetZ;
        }

        // Apply velocity
        offsetX += velocityX;
        offsetY += velocityY;
        offsetZ += velocityZ;

        // Gravity + ground collision
        if (gravity) {
            velocityY += gravityAccel;

            // Air drag
            velocityX *= drag;
            velocityZ *= drag;

            if (offsetY < groundY) {
                offsetY = groundY;
                velocityY *= -bounceFactor;
                velocityX *= groundFriction;
                velocityZ *= groundFriction;
                if (Math.abs(velocityY) < PhysicsConstants.BOUNCE_VELOCITY_THRESHOLD) {
                    velocityY = 0;
                }
            }
        }

        // Angular velocity
        rotationX += angularVelocityX;
        rotationY += angularVelocityY;
        rotationZ += angularVelocityZ;

        angularVelocityX *= angularDrag;
        angularVelocityY *= angularDrag;
        angularVelocityZ *= angularDrag;
    }

    // --- Interpolated getters for render ---

    public double getLerpedOffsetX(float tickDelta) {
        return net.minecraft.util.Mth.lerp(tickDelta, prevOffsetX, offsetX);
    }

    public double getLerpedOffsetY(float tickDelta) {
        return net.minecraft.util.Mth.lerp(tickDelta, prevOffsetY, offsetY);
    }

    public double getLerpedOffsetZ(float tickDelta) {
        return net.minecraft.util.Mth.lerp(tickDelta, prevOffsetZ, offsetZ);
    }

    public float getLerpedRotationX(float tickDelta) {
        return net.minecraft.util.Mth.lerp(tickDelta, prevRotationX, rotationX);
    }

    public float getLerpedRotationY(float tickDelta) {
        return net.minecraft.util.Mth.lerp(tickDelta, prevRotationY, rotationY);
    }

    public float getLerpedRotationZ(float tickDelta) {
        return net.minecraft.util.Mth.lerp(tickDelta, prevRotationZ, rotationZ);
    }

    // --- Setters (chainable) ---

    public AnimatedFragment offset(double x, double y, double z) {
        this.offsetX = x; this.offsetY = y; this.offsetZ = z;
        this.prevOffsetX = x; this.prevOffsetY = y; this.prevOffsetZ = z;
        return this;
    }

    public AnimatedFragment rotation(float x, float y, float z) {
        this.rotationX = x; this.rotationY = y; this.rotationZ = z;
        this.prevRotationX = x; this.prevRotationY = y; this.prevRotationZ = z;
        return this;
    }

    public AnimatedFragment scale(float scale) { this.scale = scale; return this; }
    public AnimatedFragment alpha(float alpha) { this.alpha = alpha; return this; }

    public AnimatedFragment velocity(double x, double y, double z) {
        this.velocityX = x; this.velocityY = y; this.velocityZ = z;
        return this;
    }

    public AnimatedFragment angularVelocity(float x, float y, float z) {
        this.angularVelocityX = x; this.angularVelocityY = y; this.angularVelocityZ = z;
        return this;
    }

    public AnimatedFragment gravity(boolean gravity) { this.gravity = gravity; return this; }

    public AnimatedFragment groundY(float y) { this.groundY = y; return this; }

    public AnimatedFragment physics(double gravityAccel, double bounceFactor, double drag, double angularDrag) {
        this.gravityAccel = gravityAccel;
        this.bounceFactor = bounceFactor;
        this.drag = drag;
        this.angularDrag = angularDrag;
        return this;
    }

    public AnimatedFragment physics(double gravityAccel, double bounceFactor, double drag, double angularDrag, double groundFriction) {
        this.gravityAccel = gravityAccel;
        this.bounceFactor = bounceFactor;
        this.drag = drag;
        this.angularDrag = angularDrag;
        this.groundFriction = groundFriction;
        return this;
    }

    public AnimatedFragment trailEnabled(boolean enabled) { this.trailEnabled = enabled; return this; }

    public AnimatedFragment colorOverlay(int argb) { this.colorOverlay = argb; return this; }

    /** Convenience setter: update only bounce factor */
    public AnimatedFragment bounceFactor(double bounceFactor) { this.bounceFactor = bounceFactor; return this; }

    /** Convenience setter: update only gravity acceleration */
    public AnimatedFragment gravityAccel(double gravityAccel) { this.gravityAccel = gravityAccel; return this; }

    // --- Getters ---

    public String getPartName() { return partName; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public float getRotationX() { return rotationX; }
    public float getRotationY() { return rotationY; }
    public float getRotationZ() { return rotationZ; }
    public float getScale() { return scale; }
    public float getAlpha() { return alpha; }
    public int getColorOverlay() { return colorOverlay; }
    public boolean hasColorOverlay() { return colorOverlay != 0xFFFFFFFF; }

    // Trail getters
    public boolean isTrailEnabled() { return trailEnabled; }
    public int getTrailCount() { return trailCount; }
    public double[] getTrailX() { return trailX; }
    public double[] getTrailY() { return trailY; }
    public double[] getTrailZ() { return trailZ; }

    // Physics getters
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public float getGroundY() { return groundY; }
}
