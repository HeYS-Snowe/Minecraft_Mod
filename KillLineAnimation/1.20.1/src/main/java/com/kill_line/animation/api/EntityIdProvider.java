package com.kill_line.animation.api;

import net.minecraft.world.entity.Entity;

/**
 * Bridge interface for extracting entity IDs without calling mapped methods directly.
 * The consumer mod (Kill_line) provides the implementation, since its code
 * runs in the dev environment with official mappings (not reobfuscated).
 */
@FunctionalInterface
public interface EntityIdProvider {
    int getEntityId(Entity entity);
}
