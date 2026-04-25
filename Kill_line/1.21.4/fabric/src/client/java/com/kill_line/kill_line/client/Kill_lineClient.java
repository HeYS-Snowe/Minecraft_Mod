package com.kill_line.kill_line.client;

import com.kill_line.kill_line.client.anim.KillLineAnimations;
import com.kill_line.kill_line.client.mark.ClientMarkManager;
import com.kill_line.kill_line.client.render.CrackedHealthOverlay;
import com.kill_line.kill_line.client.render.KillEffectRenderer;
import com.kill_line.kill_line.client.render.KillLineRenderer;
import com.kill_line.kill_line.client.render.SlashLineRenderer;
import com.kill_line.kill_line.client.slash.SlashKillHandler;
import com.kill_line.kill_line.network.ModPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kill_lineClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line");

    @Override
    public void onInitializeClient() {
        // Register Kill_line death animations with the animation library
        KillLineAnimations.register();

        // Register client-side network receivers
        registerNetworkReceivers();

        // Register renderers
        KillLineRenderer.register();
        CrackedHealthOverlay.register();
        SlashLineRenderer.register();

        // Register slash kill handler (tick + attack intercept)
        SlashKillHandler.register();

        LOGGER.info("Kill Line client initialized");
    }

    private void registerNetworkReceivers() {
        ClientMarkManager markManager = ClientMarkManager.getInstance();

        // Mark apply
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.MarkApplyPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                LOGGER.info("Received mark_apply: target={}, attacker={}, level={}",
                        payload.targetEntityId(), payload.attackerEntityId(), payload.level());
                markManager.applyMark(payload.targetEntityId(), payload.attackerEntityId(), payload.level());
            });
        });

        // Threshold reached
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ThresholdReachedPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                LOGGER.info("Received threshold_reached: entity={}, attacker={}",
                        payload.entityId(), payload.attackerEntityId());
                markManager.setThresholdReached(payload.entityId(), payload.attackerEntityId());
            });
        });

        // Kill effect
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.KillEffectPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                LOGGER.info("Received kill_effect: pos=({:.1f}, {:.1f}, {:.1f})",
                        payload.x(), payload.y(), payload.z());
                KillEffectRenderer.spawnKillEffect(payload.x(), payload.y(), payload.z());
            });
        });

        // Mark remove
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.MarkRemovePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                LOGGER.info("Received mark_remove: entity={}", payload.entityId());
                markManager.removeMark(payload.entityId());
            });
        });
    }
}