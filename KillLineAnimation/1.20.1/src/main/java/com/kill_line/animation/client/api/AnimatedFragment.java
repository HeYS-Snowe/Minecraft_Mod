package com.kill_line.animation.client.api;

public class AnimatedFragment {

    private double offsetX, offsetY, offsetZ;
    private float rotationX, rotationY, rotationZ;
    private float scale = 1.0f;
    private float alpha = 1.0f;
    private double velocityX, velocityY, velocityZ;
    private float angularVelocityX, angularVelocityY, angularVelocityZ;
    private final String partName;
    private boolean gravity = true;
    private float groundY = 0.0f;

    public AnimatedFragment(String partName) {
        this.partName = partName;
    }

    public void tick() {
        offsetX += velocityX;
        offsetY += velocityY;
        offsetZ += velocityZ;

        if (gravity) {
            velocityY -= 0.04;
            if (offsetY < groundY) {
                offsetY = groundY;
                velocityY *= -0.3;
                velocityX *= 0.8;
                velocityZ *= 0.8;
                if (Math.abs(velocityY) < 0.02) velocityY = 0;
            }
        }

        rotationX += angularVelocityX;
        rotationY += angularVelocityY;
        rotationZ += angularVelocityZ;

        angularVelocityX *= 0.98;
        angularVelocityY *= 0.98;
        angularVelocityZ *= 0.98;
    }

    public AnimatedFragment offset(double x, double y, double z) {
        this.offsetX = x; this.offsetY = y; this.offsetZ = z;
        return this;
    }

    public AnimatedFragment rotation(float x, float y, float z) {
        this.rotationX = x; this.rotationY = y; this.rotationZ = z;
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

    public String getPartName() { return partName; }
    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public float getRotationX() { return rotationX; }
    public float getRotationY() { return rotationY; }
    public float getRotationZ() { return rotationZ; }
    public float getScale() { return scale; }
    public float getAlpha() { return alpha; }
}
