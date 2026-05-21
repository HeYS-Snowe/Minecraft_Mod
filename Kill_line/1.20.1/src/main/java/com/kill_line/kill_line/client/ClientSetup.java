package com.kill_line.kill_line.client;

import com.kill_line.animation.client.DeathAnimationManager;
import com.kill_line.kill_line.client.anim.KillLineAnimations;
import com.kill_line.kill_line.client.render.CrackedHealthOverlay;
import com.kill_line.kill_line.init.ModMenuTypes;
import com.kill_line.kill_line.screen.CriticalWorkshopScreen;
import com.kill_line.kill_line.screen.EdictAnvilScreen;
import com.kill_line.kill_line.screen.PermeationAltarScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line");

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            DeathAnimationManager.getInstance().setEntityIdProvider(entity -> entity.getId());

            // Register V2.0 screens
            MenuScreens.register(ModMenuTypes.CRITICAL_WORKSHOP_MENU.get(), CriticalWorkshopScreen::new);
            MenuScreens.register(ModMenuTypes.PERMEATION_ALTAR_MENU.get(), PermeationAltarScreen::new);
            MenuScreens.register(ModMenuTypes.EDICT_ANVIL_MENU.get(), EdictAnvilScreen::new);

            LOGGER.info("Kill Line client initialized (V2.0)");
        });
    }

    public static void registerOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "cracked_health", CrackedHealthOverlay.INSTANCE);
    }
}
