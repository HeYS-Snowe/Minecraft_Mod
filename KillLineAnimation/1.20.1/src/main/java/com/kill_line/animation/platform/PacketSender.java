package com.kill_line.animation.platform;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

public interface PacketSender {
    void sendDeathAnimation(ServerLevel world, Entity entity,
                            int animationTypeId, float directionX, float directionZ);
}
