package com.kill_line.kill_line;

import com.kill_line.kill_line.config.KillLineConfig;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.init.ModBlockEntities;
import com.kill_line.kill_line.init.ModBlocks;
import com.kill_line.kill_line.init.ModCreativeTabs;
import com.kill_line.kill_line.init.ModItems;
import com.kill_line.kill_line.init.ModMenuTypes;
import com.kill_line.kill_line.network.ModNetworking;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("removal")
@Mod(Constants.MOD_ID)
public class Kill_line {

    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MOD_ID);

    public Kill_line() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KillLineConfig.SPEC);

        // Register V1.x content
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        // Register V2.0 content
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Register common setup for networking + animation registration
        modEventBus.addListener(this::commonSetup);

        // Register client setup via separate class
        modEventBus.addListener(com.kill_line.kill_line.client.ClientSetup::onClientSetup);
        modEventBus.addListener(com.kill_line.kill_line.client.ClientSetup::registerOverlays);

        LOGGER.info("Kill Line mod initialized (V2.0)");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetworking.register();
            com.kill_line.kill_line.client.anim.KillLineAnimations.register();
        });
    }
}
