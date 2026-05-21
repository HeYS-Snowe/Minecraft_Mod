package com.kill_line.kill_line.integration;

import net.minecraftforge.fml.ModList;

/**
 * Integration with CriticalCore prerequisite mod.
 * When CriticalCore is installed, Kill Line enchantment can only be obtained
 * through the Edict Anvil crafting chain, not from enchanting tables.
 */
public class CriticalCoreIntegration {

    private static final boolean CRITICAL_CORE_LOADED =
            ModList.get().isLoaded("critical_core");

    /**
     * If CriticalCore is installed, block enchanting table from giving Kill Line.
     */
    public static boolean shouldBlockEnchantingTable() {
        return CRITICAL_CORE_LOADED;
    }

    /**
     * If CriticalCore is installed, Kill Line requires the Edict Anvil.
     */
    public static boolean requiresSpecialAnvil() {
        return CRITICAL_CORE_LOADED;
    }

    /**
     * Check if CriticalCore is loaded.
     */
    public static boolean isCriticalCoreLoaded() {
        return CRITICAL_CORE_LOADED;
    }
}
