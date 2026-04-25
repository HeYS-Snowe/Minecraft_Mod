package com.kill_line.animation.client;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeathAnimationManager {

    private static final DeathAnimationManager INSTANCE = new DeathAnimationManager();
    private static final int MAX_CONCURRENT = 20;

    private final Map<Integer, com.kill_line.animation.client.api.AbstractDeathAnimation> activeAnimations = new ConcurrentHashMap<>();
    private volatile com.kill_line.animation.api.EntityIdProvider entityIdProvider;

    public static DeathAnimationManager getInstance() { return INSTANCE; }
    private DeathAnimationManager() {}

    public void setEntityIdProvider(com.kill_line.animation.api.EntityIdProvider provider) {
        this.entityIdProvider = provider;
    }

    public int getEntityId(net.minecraft.world.entity.Entity entity) {
        if (entityIdProvider != null) return entityIdProvider.getEntityId(entity);
        throw new IllegalStateException("EntityIdProvider not registered! Call DeathAnimationManager.setEntityIdProvider() during client setup.");
    }

    public void startAnimation(int entityId, com.kill_line.animation.client.api.AbstractDeathAnimation animation) {
        if (activeAnimations.size() >= MAX_CONCURRENT) {
            int oldestId = activeAnimations.entrySet().stream()
                    .min((a, b) -> Integer.compare(a.getValue().getTick(), b.getValue().getTick()))
                    .map(Map.Entry::getKey)
                    .orElse(-1);
            if (oldestId >= 0) activeAnimations.remove(oldestId);
        }
        activeAnimations.put(entityId, animation);
    }

    public boolean isAnimating(int entityId) {
        return activeAnimations.containsKey(entityId);
    }

    public void tick() {
        activeAnimations.entrySet().removeIf(entry -> {
            entry.getValue().tick();
            return entry.getValue().isFinished();
        });
    }

    public Collection<com.kill_line.animation.client.api.AbstractDeathAnimation> getActiveAnimations() {
        return activeAnimations.values();
    }

    public void clear() { activeAnimations.clear(); }
}
