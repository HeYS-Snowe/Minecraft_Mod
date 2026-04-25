package com.kill_line.animation.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class AnimationNetworking {

    public static void registerS2C() {
        PayloadTypeRegistry.playS2C().register(
                AnimationPayloads.DeathAnimationTriggerPacket.ID,
                AnimationPayloads.DeathAnimationTriggerPacket.CODEC
        );
    }

    /**
     * Send death animation trigger packet to all tracking players.
     */
    public static void sendDeathAnimation(ServerWorld world, Entity entity,
                                           int animationTypeId, float directionX, float directionZ) {
        AnimationPayloads.DeathAnimationTriggerPacket packet = new AnimationPayloads.DeathAnimationTriggerPacket(
                entity.getId(),
                entity.getX(), entity.getY(), entity.getZ(),
                entity instanceof net.minecraft.entity.LivingEntity living ? living.getBodyYaw() : 0f,
                animationTypeId,
                directionX, directionZ
        );

        for (ServerPlayerEntity player : PlayerLookup.tracking(world, entity.getBlockPos())) {
            ServerPlayNetworking.send(player, packet);
        }

        // Also send to the entity itself if it's a player
        if (entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, packet);
        }
    }
}
