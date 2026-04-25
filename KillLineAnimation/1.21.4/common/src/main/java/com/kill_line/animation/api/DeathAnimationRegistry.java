package com.kill_line.animation.api;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for death animation types.
 * Consumer mods call {@link #register} during initialization.
 */
public class DeathAnimationRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line_animation");

    private static final Map<Identifier, DeathAnimationType> BY_IDENTIFIER = new ConcurrentHashMap<>();
    private static final List<DeathAnimationType> BY_INDEX = new ArrayList<>();

    /**
     * Register a new death animation type.
     *
     * @param id            unique identifier (e.g. Identifier.of("my_mod", "horizontal_slash"))
     * @param durationTicks default animation duration in ticks
     * @param factory       factory to create animation instances (client-side)
     * @return the registered type
     */
    public static synchronized DeathAnimationType register(Identifier id, int durationTicks, DeathAnimationFactory factory) {
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

    /**
     * Get animation type by network ID (used for packet deserialization).
     */
    public static DeathAnimationType fromNetworkId(int networkId) {
        if (networkId < 0 || networkId >= BY_INDEX.size()) return null;
        return BY_INDEX.get(networkId);
    }

    /**
     * Get animation type by identifier.
     */
    public static DeathAnimationType get(Identifier id) {
        return BY_IDENTIFIER.get(id);
    }

    /**
     * Get all registered types.
     */
    public static List<DeathAnimationType> getAll() {
        return List.copyOf(BY_INDEX);
    }
}
