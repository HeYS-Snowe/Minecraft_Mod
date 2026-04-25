package com.kill_line.animation.api;

import net.minecraft.resources.ResourceLocation;

public class DeathAnimationType {

    private final ResourceLocation id;
    private final int networkId;
    private final int defaultDurationTicks;
    private final DeathAnimationFactory factory;

    public DeathAnimationType(ResourceLocation id, int networkId, int defaultDurationTicks, DeathAnimationFactory factory) {
        this.id = id;
        this.networkId = networkId;
        this.defaultDurationTicks = defaultDurationTicks;
        this.factory = factory;
    }

    public ResourceLocation getId() { return id; }
    public int getNetworkId() { return networkId; }
    public int getDefaultDurationTicks() { return defaultDurationTicks; }
    public DeathAnimationFactory getFactory() { return factory; }

    @Override
    public String toString() { return "DeathAnimationType{" + id + "}"; }
}
