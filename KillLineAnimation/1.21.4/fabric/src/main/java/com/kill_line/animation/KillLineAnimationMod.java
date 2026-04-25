package com.kill_line.animation;

import com.kill_line.animation.network.AnimationNetworking;
import com.kill_line.animation.platform.PacketSender;
import com.kill_line.animation.platform.Platform;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillLineAnimationMod implements ModInitializer {

    public static final String MOD_ID = Constants.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register platform-specific packet sender
        Platform.setPacketSender(new PacketSender() {
            @Override
            public void sendDeathAnimation(net.minecraft.server.world.ServerWorld world,
                                           net.minecraft.entity.Entity entity,
                                           int animationTypeId, float directionX, float directionZ) {
                AnimationNetworking.sendDeathAnimation(world, entity, animationTypeId, directionX, directionZ);
            }
        });

        AnimationNetworking.registerS2C();
        LOGGER.info("KillLine Animation library initialized");
    }
}
