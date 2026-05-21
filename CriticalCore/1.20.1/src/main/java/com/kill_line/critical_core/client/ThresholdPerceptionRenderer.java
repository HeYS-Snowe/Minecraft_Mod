package com.kill_line.critical_core.client;

import com.kill_line.critical_core.CriticalCore;
import com.kill_line.critical_core.config.CriticalCoreConfig;
import com.kill_line.critical_core.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = CriticalCore.MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ThresholdPerceptionRenderer {

    private static int tickCounter = 0;
    private static final List<LivingEntity> detectedEntities = new ArrayList<>();
    private static float pulseAlpha = 0.0f;
    private static boolean pulseIncreasing = true;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        tickCounter++;

        // Check every 10 ticks if player has critical core
        if (tickCounter % 10 == 0) {
            detectedEntities.clear();
            if (hasCriticalCore(player)) {
                double range = CriticalCoreConfig.THRESHOLD_PERCEPTION.detectionRange.get();
                double threshold = CriticalCoreConfig.THRESHOLD_PERCEPTION.healthThreshold.get();

                for (Entity entity : player.level().getEntities(player,
                        player.getBoundingBox().inflate(range))) {
                    if (entity instanceof LivingEntity living && living.isAlive()) {
                        float healthRatio = living.getHealth() / living.getMaxHealth();
                        if (healthRatio <= threshold && healthRatio > 0) {
                            detectedEntities.add(living);
                        }
                    }
                }
            }
        }

        // Pulse animation
        if (!detectedEntities.isEmpty()) {
            if (pulseIncreasing) {
                pulseAlpha += 0.02f;
                if (pulseAlpha >= 0.4f) pulseIncreasing = false;
            } else {
                pulseAlpha -= 0.02f;
                if (pulseAlpha <= 0.05f) pulseIncreasing = true;
            }
        } else {
            pulseAlpha = 0.0f;
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;
        if (detectedEntities.isEmpty()) return;
        if (pulseAlpha <= 0.0f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Draw pulsing purple border at screen edges
        int color = ((int)(pulseAlpha * 255) << 24) | 0x8B00FF; // Purple with pulsing alpha

        // Top edge
        guiGraphics.fill(0, 0, width, 3, color);
        // Bottom edge
        guiGraphics.fill(0, height - 3, width, height, color);
        // Left edge
        guiGraphics.fill(0, 0, 3, height, color);
        // Right edge
        guiGraphics.fill(width - 3, 0, width, height, color);
    }

    private static boolean hasCriticalCore(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.CRITICAL_CORE.get())) {
                return true;
            }
        }
        return false;
    }
}
