package com.kill_line.animation.api;

/**
 * Factory interface for creating death animation instances.
 * Consumer mods implement this to provide their animation classes.
 * <p>
 * The factory receives client-side snapshot data, so the implementation
 * must be in the client source set.
 */
@FunctionalInterface
public interface DeathAnimationFactory {
    /**
     * Create a new death animation instance.
     * Note: snapshot contains client-only data; this method is only called on the client.
     *
     * @param snapshot captured entity data (will be null on server side - use the other overload)
     * @param type     the animation type
     * @return a new animation instance
     */
    Object create(Object snapshot, DeathAnimationType type);
}
