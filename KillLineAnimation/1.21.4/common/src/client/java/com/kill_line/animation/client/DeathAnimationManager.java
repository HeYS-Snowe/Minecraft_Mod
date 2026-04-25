package com.kill_line.animation.client;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for active death animations.
 * Singleton — accessed by renderer and mixin.
 */
public class DeathAnimationManager {

    private static final DeathAnimationManager INSTANCE = new DeathAnimationManager();
    private static final int MAX_CONCURRENT = 20;

    private final Map<Integer, com.kill_line.animation.client.api.AbstractDeathAnimation> activeAnimations = new ConcurrentHashMap<>();

    public static DeathAnimationManager getInstance() {
        return INSTANCE;
    }

    private DeathAnimationManager() {}

    /**
     * Start a new death animation for an entity.
     * Replaces any existing animation for the same entity.
     */
    public void startAnimation(int entityId, com.kill_line.animation.client.api.AbstractDeathAnimation animation) {
        // If at capacity, remove the oldest
        if (activeAnimations.size() >= MAX_CONCURRENT) {
            int oldestId = activeAnimations.entrySet().stream()
                    .min((a, b) -> Integer.compare(a.getValue().getTick(), b.getValue().getTick()))
                    .map(Map.Entry::getKey)
                    .orElse(-1);
            if (oldestId >= 0) activeAnimations.remove(oldestId);
        }
        activeAnimations.put(entityId, animation);
    }

    /**
     * Check if an entity has an active (not finished) death animation.
     * Used by mixin to suppress default death rendering.
     */
    public boolean isAnimating(int entityId) {
        return activeAnimations.containsKey(entityId);
    }

    /**
     * Tick all active animations. Remove finished ones.
     */
    public void tick() {
        activeAnimations.entrySet().removeIf(entry -> {
            entry.getValue().tick();
            return entry.getValue().isFinished();
        });
    }

    /**
     * Get all active animations for rendering.
     */
    public Collection<com.kill_line.animation.client.api.AbstractDeathAnimation> getActiveAnimations() {
        return activeAnimations.values();
    }

    /**
     * Remove all animations (e.g., on world change).
     */
    public void clear() {
        activeAnimations.clear();
    }
}
