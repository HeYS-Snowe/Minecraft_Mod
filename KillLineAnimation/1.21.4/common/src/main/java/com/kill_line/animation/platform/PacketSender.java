package com.kill_line.animation.platform;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Platform abstraction for sending network packets.
 * Each loader provides its own implementation.
 */
public interface PacketSender {

    /**
     * Send a death animation trigger packet to all tracking players
     * and the entity itself (if it's a player).
     */
    void sendDeathAnimation(ServerWorld world, Entity entity,
                            int animationTypeId, float directionX, float directionZ);
}
