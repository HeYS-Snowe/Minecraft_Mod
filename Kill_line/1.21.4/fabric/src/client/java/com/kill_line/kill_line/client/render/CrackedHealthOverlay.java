package com.kill_line.kill_line.client.render;

import com.kill_line.kill_line.client.mark.ClientMarkManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class CrackedHealthOverlay {

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            // Check if the player is looking at a marked entity below threshold
            Entity target = client.targetedEntity;
            if (!(target instanceof LivingEntity livingTarget)) return;

            ClientMarkManager.MarkInfo mark = ClientMarkManager.getInstance().getMark(target.getId());
            if (mark == null || !mark.thresholdReached()) return;

            // Check visibility
            if (!shouldShowOverlay(client.player, livingTarget, mark)) return;

            // Render cracked health bar overlay
            renderCrackedHealthBar(drawContext, livingTarget, client);
        });
    }

    private static boolean shouldShowOverlay(PlayerEntity localPlayer, LivingEntity target, ClientMarkManager.MarkInfo mark) {
        if (target instanceof MobEntity) return true;
        if (target instanceof PlayerEntity) {
            return localPlayer.getId() == mark.attackerEntityId();
        }
        return true;
    }

    private static void renderCrackedHealthBar(DrawContext drawContext, LivingEntity target, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Render above the crosshair area
        float healthPercent = target.getHealth() / target.getMaxHealth();
        int barWidth = 100;
        int barHeight = 6;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight / 2 + 30;

        // Background
        drawContext.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF000000);

        // Health portion (red, cracked effect)
        int healthWidth = (int) (barWidth * healthPercent);
        int healthColor = 0xFFCC0000;
        drawContext.fill(x, y, x + healthWidth, y + barHeight, healthColor);

        // Crack lines
        int crackColor = 0xFF000000;
        for (int i = 0; i < 4; i++) {
            int crackX = x + (int) (barWidth * (0.2 + i * 0.2));
            if (crackX < x + healthWidth) {
                drawContext.fill(crackX, y, crackX + 1, y + barHeight, crackColor);
                // Diagonal crack
                drawContext.fill(crackX - 1, y + barHeight / 2, crackX + 2, y + barHeight / 2 + 1, crackColor);
            }
        }

        // Skull/kill indicator icon text
        drawContext.drawText(client.textRenderer, "X", x + barWidth + 5, y - 2, 0xFFFF3333, true);
    }
}