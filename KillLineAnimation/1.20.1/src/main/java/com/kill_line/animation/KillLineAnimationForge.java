package com.kill_line.animation;

import com.kill_line.animation.client.anim.BuiltinAnimations;
import com.kill_line.animation.network.AnimationNetworking;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Constants.MOD_ID)
public class KillLineAnimationForge {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    public KillLineAnimationForge() {
        // Register common setup
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        // Register client setup via separate class
        FMLJavaModLoadingContext.get().getModEventBus().addListener(com.kill_line.animation.client.ClientSetup::onClientSetup);

        LOGGER.info("KillLine Animation library initialized");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AnimationNetworking.register();
            // Register builtin animations on both sides so the server
            // knows about animation types when selecting random ones
            com.kill_line.animation.client.anim.BuiltinAnimations.register();
        });
    }
}
