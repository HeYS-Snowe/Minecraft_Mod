package com.kill_line.animation.api;

import net.minecraft.util.Identifier;

/**
 * Represents a registered death animation type.
 * Mods create instances via {@link DeathAnimationRegistry#register}.
 */
public class DeathAnimationType {

    private final Identifier id;
    private final int networkId;
    private final int defaultDurationTicks;
    private final DeathAnimationFactory factory;

    public DeathAnimationType(Identifier id, int networkId, int defaultDurationTicks, DeathAnimationFactory factory) {
        this.id = id;
        this.networkId = networkId;
        this.defaultDurationTicks = defaultDurationTicks;
        this.factory = factory;
    }

    public Identifier getId() {
        return id;
    }

    public int getNetworkId() {
        return networkId;
    }

    public int getDefaultDurationTicks() {
        return defaultDurationTicks;
    }

    public DeathAnimationFactory getFactory() {
        return factory;
    }

    @Override
    public String toString() {
        return "DeathAnimationType{" + id + "}";
    }
}
