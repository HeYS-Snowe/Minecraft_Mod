package com.kill_line.kill_line.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class KillEffectRenderer {

    public static void spawnKillEffect(double x, double y, double z) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // Red damage indicator particles
        for (int i = 0; i < 30; i++) {
            double offsetX = (client.world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = client.world.random.nextDouble() * 2.0;
            double offsetZ = (client.world.random.nextDouble() - 0.5) * 2.0;
            client.world.addParticle(
                    ParticleTypes.DAMAGE_INDICATOR,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0.8, 0.0, 0.0
            );
        }

        // Explosion effect
        for (int i = 0; i < 20; i++) {
            double offsetX = (client.world.random.nextDouble() - 0.5) * 3.0;
            double offsetY = client.world.random.nextDouble() * 3.0;
            double offsetZ = (client.world.random.nextDouble() - 0.5) * 3.0;
            client.world.addParticle(
                    ParticleTypes.EXPLOSION,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0
            );
        }

        // Red sweeping particles
        for (int i = 0; i < 15; i++) {
            double offsetX = (client.world.random.nextDouble() - 0.5) * 1.5;
            double offsetY = client.world.random.nextDouble() * 1.5;
            double offsetZ = (client.world.random.nextDouble() - 0.5) * 1.5;
            client.world.addParticle(
                    ParticleTypes.SWEEP_ATTACK,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0
            );
        }

        // Play kill sound
        client.world.playSound(x, y, z,
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                SoundCategory.PLAYERS, 2.0f, 0.5f, false);
    }
}