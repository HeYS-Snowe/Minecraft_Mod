package com.kill_line.kill_line.client;

import com.kill_line.animation.client.DeathAnimationManager;
import com.kill_line.kill_line.client.anim.KillLineAnimations;
import com.kill_line.kill_line.client.render.CrackedHealthOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line");

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register the entity ID bridge so KillLineAnimation can get entity IDs
            DeathAnimationManager.getInstance().setEntityIdProvider(entity -> entity.getId());

            LOGGER.info("Kill Line client initialized");
        });
    }

    public static void registerOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "cracked_health", CrackedHealthOverlay.INSTANCE);
    }
}
