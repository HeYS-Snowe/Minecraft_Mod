package com.kill_line.kill_line.client.slash;

import com.kill_line.kill_line.network.ModNetworking;
import com.kill_line.kill_line.network.ModPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
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
     * Intercepts attacks on entities (Forge equivalent of Fabric's AttackEntityCallback).
     * If the player attacks an invulnerable entity with a completed slash trace,
     * cancels the vanilla attack and sends the slash kill packet to the server.
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Entity target = event.getTarget();
        if (target == null) return;

        if (SlashLineManager.getInstance().isTraceComplete(target.getId())) {
            event.setCanceled(true);
            ModNetworking.CHANNEL.sendToServer(new ModPayloads.SlashKillPayload(target.getId()));
            SlashLineManager.getInstance().removeLine(target.getId());
        }
    }
}
