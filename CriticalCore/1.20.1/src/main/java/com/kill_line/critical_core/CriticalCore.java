package com.kill_line.critical_core;

import com.kill_line.critical_core.config.CriticalCoreConfig;
import com.kill_line.critical_core.init.ModBlockEntities;
import com.kill_line.critical_core.init.ModBlocks;
import com.kill_line.critical_core.init.ModCreativeTabs;
import com.kill_line.critical_core.init.ModItems;
import com.kill_line.critical_core.init.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CriticalCore.MODID)
@SuppressWarnings("removal")
public class CriticalCore {

    public static final String MODID = "critical_core";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CriticalCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CriticalCoreConfig.SPEC);

        // Register deferred registers
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Critical Core initialized");
    }
}
