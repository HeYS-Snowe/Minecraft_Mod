package com.kill_line.animation.api;

import com.kill_line.animation.platform.Platform;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * Server-side API to trigger death animations.
 * Consumer mods call {@link #triggerDeathAnimation} when an entity is killed.
 */
public class AnimationDispatcher {

    /**
     * Trigger a death animation for an entity.
     * Sends a packet to all tracking players (and the entity itself if it's a player).
     *
     * @param world       the server world
     * @param entity      the dying entity
     * @param animationType the animation type to play
     * @param direction   the direction of the killing blow (from attacker to victim), or Vec3d.ZERO
     */
    public static void triggerDeathAnimation(ServerWorld world, LivingEntity entity,
                                              DeathAnimationType animationType, Vec3d direction) {
        if (world.isClient()) return;

        float dirX = (float) direction.x;
        float dirZ = (float) direction.z;
        // Normalize direction
        float len = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (len > 0.001f) {
            dirX /= len;
            dirZ /= len;
        }

        Platform.getPacketSender().sendDeathAnimation(world, entity, animationType.getNetworkId(), dirX, dirZ);
    }

    /**
     * Trigger a death animation with no specific direction.
     */
    public static void triggerDeathAnimation(ServerWorld world, LivingEntity entity, DeathAnimationType animationType) {
        triggerDeathAnimation(world, entity, animationType, Vec3d.ZERO);
    }
}
