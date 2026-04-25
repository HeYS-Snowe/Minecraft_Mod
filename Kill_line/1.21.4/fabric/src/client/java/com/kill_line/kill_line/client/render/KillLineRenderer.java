package com.kill_line.kill_line.client.render;

import com.kill_line.kill_line.client.mark.ClientMarkManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;

public class KillLineRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            PlayerEntity localPlayer = client.player;
            ClientMarkManager markManager = ClientMarkManager.getInstance();

            for (Entity entity : context.world().getEntities()) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (!living.isAlive()) continue;

                ClientMarkManager.MarkInfo mark = markManager.getMark(entity.getId());
                if (mark == null) continue;

                if (!shouldRender(localPlayer, living, mark)) continue;

                if (mark.thresholdReached()) {
                    renderThresholdParticles(living);
                } else {
                    renderMarkParticles(living);
                }
            }
        });
    }

    private static boolean shouldRender(PlayerEntity localPlayer, LivingEntity target, ClientMarkManager.MarkInfo mark) {
        if (target instanceof MobEntity) return true;
        if (target instanceof PlayerEntity) {
            return localPlayer.getId() == mark.attackerEntityId();
        }
        return true;
    }

    private static void renderThresholdParticles(LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getHeight() + 0.3;
        double z = entity.getZ();

        float time = (float) (entity.getWorld().getTime() % 360) * 0.1f;
        for (int i = 0; i < 3; i++) {
            double offsetX = MathHelper.sin(time + i * 2.094f) * 0.3;
            double offsetZ = MathHelper.cos(time + i * 2.094f) * 0.3;

            entity.getWorld().addParticle(
                    ParticleTypes.ENCHANTED_HIT,
                    x + offsetX, y, z + offsetZ,
                    0.0, 0.1, 0.0
            );
        }
    }

    /**
     * Renders a subtle mark indicator above a marked entity (not yet at threshold).
     * Uses a slow-pulsing red particle to show the entity is marked.
     */
    private static void renderMarkParticles(LivingEntity entity) {
        // Only render every 4 ticks to keep it subtle
        if (entity.age % 4 != 0) return;

        double x = entity.getX();
        double y = entity.getY() + entity.getHeight() + 0.2;
        double z = entity.getZ();

        entity.getWorld().addParticle(
                ParticleTypes.ENCHANTED_HIT,
                x, y, z,
                0.0, 0.05, 0.0
        );
    }
}