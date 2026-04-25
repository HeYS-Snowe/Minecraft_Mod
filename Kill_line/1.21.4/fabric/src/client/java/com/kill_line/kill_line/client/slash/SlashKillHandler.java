package com.kill_line.kill_line.client.slash;

import com.kill_line.kill_line.network.ModPayloads;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side handler for the slash kill mechanic.
 * - Ticks SlashLineManager each client tick for trace progress updates
 * - Intercepts attacks on invulnerable entities when trace is complete
 */
public class SlashKillHandler {

    public static void register() {
        // Tick the slash line manager every client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SlashLineManager.getInstance().tick(client);

            // Clear on world change
            if (client.world == null) {
                SlashLineManager.getInstance().clear();
            }
        });

        // Intercept attacks on slash-ready entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && entity != null) {
                if (SlashLineManager.getInstance().isTraceComplete(entity.getId())) {
                    // Send slash kill packet to server
                    ClientPlayNetworking.send(new ModPayloads.SlashKillPayload(entity.getId()));
                    SlashLineManager.getInstance().removeLine(entity.getId());
                    return ActionResult.SUCCESS; // Consume the attack
                }
            }
            return ActionResult.PASS; // Let normal attack proceed
        });
    }
}
