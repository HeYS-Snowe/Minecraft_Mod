package com.kill_line.animation.api;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeathAnimationRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line_animation");

    private static final Map<ResourceLocation, DeathAnimationType> BY_IDENTIFIER = new ConcurrentHashMap<>();
    private static final List<DeathAnimationType> BY_INDEX = new ArrayList<>();

    public static synchronized DeathAnimationType register(ResourceLocation id, int durationTicks, DeathAnimationFactory factory) {
        if (BY_IDENTIFIER.containsKey(id)) {
            throw new IllegalArgumentException("Death animation type already registered: " + id);
        }
        int networkId = BY_INDEX.size();
        DeathAnimationType type = new DeathAnimationType(id, networkId, durationTicks, factory);
        BY_IDENTIFIER.put(id, type);
        BY_INDEX.add(type);
        LOGGER.debug("Registered death animation type: {} (networkId={})", id, networkId);
        return type;
    }

    public static DeathAnimationType fromNetworkId(int networkId) {
        if (networkId < 0 || networkId >= BY_INDEX.size()) return null;
        return BY_INDEX.get(networkId);
    }

    public static DeathAnimationType get(ResourceLocation id) {
        return BY_IDENTIFIER.get(id);
    }

    public static List<DeathAnimationType> getAll() {
        return List.copyOf(BY_INDEX);
    }
}
