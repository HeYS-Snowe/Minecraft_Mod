package com.kill_line.kill_line.client.render;

import com.kill_line.kill_line.client.mark.ClientMarkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kill_line", value = Dist.CLIENT)
public class KillLineRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        Player localPlayer = client.player;
        ClientMarkManager markManager = ClientMarkManager.getInstance();

        for (Entity entity : client.level.entitiesForRendering()) {
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
    }

    private static boolean shouldRender(Player localPlayer, LivingEntity target, ClientMarkManager.MarkInfo mark) {
        if (target instanceof Mob) return true;
        if (target instanceof Player) {
            return localPlayer.getId() == mark.attackerEntityId();
        }
        return true;
    }

    private static void renderThresholdParticles(LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() + 0.3;
        double z = entity.getZ();

        float time = (float) (entity.level().getGameTime() % 360) * 0.1f;
        for (int i = 0; i < 3; i++) {
            double offsetX = Mth.sin(time + i * 2.094f) * 0.3;
            double offsetZ = Mth.cos(time + i * 2.094f) * 0.3;

            entity.level().addParticle(
                    ParticleTypes.ENCHANTED_HIT,
                    x + offsetX, y, z + offsetZ,
                    0.0, 0.1, 0.0
            );
        }
    }

    private static void renderMarkParticles(LivingEntity entity) {
        if (entity.tickCount % 4 != 0) return;

        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() + 0.2;
        double z = entity.getZ();

        entity.level().addParticle(
                ParticleTypes.ENCHANTED_HIT,
                x, y, z,
                0.0, 0.05, 0.0
        );
    }
}
