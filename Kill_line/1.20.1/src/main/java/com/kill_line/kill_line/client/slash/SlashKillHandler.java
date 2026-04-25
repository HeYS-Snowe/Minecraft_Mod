package com.kill_line.kill_line.client.slash;

import com.kill_line.kill_line.network.ModNetworking;
import com.kill_line.kill_line.network.ModPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side handler for the slash kill mechanic.
 * - Ticks SlashLineManager each client tick for trace progress updates
 * - Intercepts attacks on invulnerable entities when trace is complete
 */
@Mod.EventBusSubscriber(modid = "kill_line", value = Dist.CLIENT)
public class SlashKillHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getInstance();
            SlashLineManager.getInstance().tick(client);

            // Clear on world change
            if (client.level == null) {
                SlashLineManager.getInstance().clear();
            }
        }
    }

    /**
     * Intercepts left-click attacks on entities.
     * If the player clicks an invulnerable entity with a completed slash trace,
     * sends the slash kill packet to the server.
     */
    @SubscribeEvent
    public static void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        // This fires on client when left-clicking empty space or after a missed attack.
        // We need to check if the crosshair entity is a slash-ready target.
        handleSlashAttack();
    }

    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        // Also handle case where player clicks a block but has a slash-ready entity targeted
        handleSlashAttack();
    }

    private static void handleSlashAttack() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        Entity target = client.crosshairPickEntity;
        if (target == null) return;

        if (SlashLineManager.getInstance().isTraceComplete(target.getId())) {
            ModNetworking.CHANNEL.sendToServer(new ModPayloads.SlashKillPayload(target.getId()));
            SlashLineManager.getInstance().removeLine(target.getId());
        }
    }
}
