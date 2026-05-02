package com.kill_line.animation.client.math;

/**
 * Centralized physics constants for animation fragments.
 * Replaces magic numbers scattered throughout animation code.
 */
public final class PhysicsConstants {

    private PhysicsConstants() {}

    // --- Gravity (MC units per tick^2) ---
    public static final double GRAVITY_NORMAL = -0.04;
    public static final double GRAVITY_LIGHT = -0.02;     // feathers, ash, small debris
    public static final double GRAVITY_HEAVY = -0.06;     // ice chunks, heavy body parts

    // --- Bounce (velocity multiplier on ground hit) ---
    public static final double BOUNCE_STANDARD = 0.3;
    public static final double BOUNCE_HIGH = 0.5;          // elastic fragments
    public static final double BOUNCE_LOW = 0.15;          // ice, brittle materials

    // --- Air drag (velocity multiplied by this each tick) ---
    public static final double DRAG_STANDARD = 0.98;
    public static final double DRAG_HIGH = 0.96;           // large surface area fragments
    public static final double DRAG_LOW = 0.995;           // small / light fragments

    // --- Angular velocity drag ---
    public static final double ANGULAR_DRAG_STANDARD = 0.97;
    public static final double ANGULAR_DRAG_FAST = 0.93;   // ice shards (spin fast then stop)

    // --- Ground friction ---
    public static final double GROUND_FRICTION = 0.8;
    public static final double GROUND_FRICTION_LOW = 0.6;  // ice (slides more)

    // --- Velocity threshold to stop bouncing ---
    public static final double BOUNCE_VELOCITY_THRESHOLD = 0.02;
}
