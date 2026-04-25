package com.kill_line.kill_line.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class KillEffectRenderer {

    public static void spawnKillEffect(double x, double y, double z) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        // Red damage indicator particles
        for (int i = 0; i < 30; i++) {
            double offsetX = (client.level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = client.level.random.nextDouble() * 2.0;
            double offsetZ = (client.level.random.nextDouble() - 0.5) * 2.0;
            client.level.addParticle(
                    ParticleTypes.DAMAGE_INDICATOR,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0.8, 0.0, 0.0
            );
        }

        // Explosion effect
        for (int i = 0; i < 20; i++) {
            double offsetX = (client.level.random.nextDouble() - 0.5) * 3.0;
            double offsetY = client.level.random.nextDouble() * 3.0;
            double offsetZ = (client.level.random.nextDouble() - 0.5) * 3.0;
            client.level.addParticle(
                    ParticleTypes.EXPLOSION,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0
            );
        }

        // Red sweeping particles
        for (int i = 0; i < 15; i++) {
            double offsetX = (client.level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = client.level.random.nextDouble() * 1.5;
            double offsetZ = (client.level.random.nextDouble() - 0.5) * 1.5;
            client.level.addParticle(
                    ParticleTypes.SWEEP_ATTACK,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0
            );
        }

        // Play kill sound
        client.level.playLocalSound(x, y, z,
                SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS, 2.0f, 0.5f, false);
    }
}
