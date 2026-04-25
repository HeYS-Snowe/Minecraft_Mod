package com.kill_line.animation.api;

import com.kill_line.animation.platform.Platform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class AnimationDispatcher {

    public static void triggerDeathAnimation(ServerLevel world, LivingEntity entity,
                                              DeathAnimationType animationType, Vec3 direction) {
        if (world.isClientSide) return;

        float dirX = (float) direction.x;
        float dirZ = (float) direction.z;
        float len = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (len > 0.001f) {
            dirX /= len;
            dirZ /= len;
        }

        Platform.getPacketSender().sendDeathAnimation(world, entity, animationType.getNetworkId(), dirX, dirZ);
    }

    public static void triggerDeathAnimation(ServerLevel world, LivingEntity entity, DeathAnimationType animationType) {
        triggerDeathAnimation(world, entity, animationType, Vec3.ZERO);
    }
}
