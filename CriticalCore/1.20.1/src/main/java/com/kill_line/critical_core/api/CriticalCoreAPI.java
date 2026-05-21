package com.kill_line.critical_core.api;

import com.kill_line.critical_core.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;

public class CriticalCoreAPI {

    private static final boolean LOADED = ModList.get().isLoaded("critical_core");

    /**
     * Check if CriticalCore mod is loaded.
     */
    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * Get the Critical Core item for use in Kill_line recipes.
     */
    public static Item getCriticalCoreItem() {
        return ModItems.CRITICAL_CORE.get();
    }

    /**
     * Get the Threshold Shard item.
     */
    public static Item getThresholdShardItem() {
        return ModItems.THRESHOLD_SHARD.get();
    }

    /**
     * Get the Stable Core item for insurance slot in Kill_line V2.
     */
    public static Item getStableCoreItem() {
        return ModItems.STABLE_CORE.get();
    }
}
