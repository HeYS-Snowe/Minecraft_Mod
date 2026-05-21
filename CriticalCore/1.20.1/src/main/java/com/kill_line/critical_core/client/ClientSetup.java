package com.kill_line.critical_core.client;

import com.kill_line.critical_core.CriticalCore;
import com.kill_line.critical_core.init.ModMenuTypes;
import com.kill_line.critical_core.screen.PulseForgeScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CriticalCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.PULSE_FORGE_MENU.get(), PulseForgeScreen::new);
        });
    }
}
