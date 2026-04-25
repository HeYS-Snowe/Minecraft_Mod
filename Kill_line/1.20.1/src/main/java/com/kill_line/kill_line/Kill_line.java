package com.kill_line.kill_line;

import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.network.ModNetworking;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Constants.MOD_ID)
public class Kill_line {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    public Kill_line() {
        // Register enchantment via DeferredRegister
        ModEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register common setup for networking + animation registration
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        // Register client setup via separate class
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
                com.kill_line.kill_line.client.ClientSetup::onClientSetup);

        // Register GUI overlays
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
                com.kill_line.kill_line.client.ClientSetup::registerOverlays);

        LOGGER.info("Kill Line mod initialized");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetworking.register();
            // Register animations on both server and client so the server
            // can pick random animation types to send to clients
            com.kill_line.kill_line.client.anim.KillLineAnimations.register();
        });
    }
}
