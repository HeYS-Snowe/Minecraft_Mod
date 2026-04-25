package com.kill_line.animation.client;

import com.kill_line.animation.KillLineAnimationMod;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.animation.client.anim.BuiltinAnimations;
import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.network.AnimationPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KillLineAnimationClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KillLineAnimationMod.MOD_ID + "_client");

    @Override
    public void onInitializeClient() {
        // Register built-in animations
        BuiltinAnimations.register();

        // Register renderer
        DeathAnimationRenderer.register();

        // Register network receiver
        registerNetworkReceivers();

        LOGGER.info("KillLine Animation client initialized");
    }

    private void registerNetworkReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationPayloads.DeathAnimationTriggerPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                DeathAnimationType type = DeathAnimationRegistry.fromNetworkId(payload.animationTypeId());
                if (type == null) {
                    LOGGER.warn("Unknown animation type id: {}", payload.animationTypeId());
                    return;
                }

                // Create snapshot from packet data
                EntitySnapshot snapshot = EntitySnapshot.fromPacket(payload, context.client());

                // Create and start animation via factory
                Object anim = type.getFactory().create(snapshot, type);
                if (anim instanceof AbstractDeathAnimation deathAnim) {
                    DeathAnimationManager.getInstance().startAnimation(snapshot.getEntityId(), deathAnim);
                    LOGGER.debug("Started death animation {} for entity {}", type.getId(), snapshot.getEntityId());
                }
            });
        });
    }
}
