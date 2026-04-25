package com.kill_line.kill_line.enchantment;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ModEnchantments {

    public static final RegistryKey<Enchantment> KILL_LINE = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of("kill_line", "kill_line")
    );

    // Slash line constants
    public static final double SLASH_LINE_RANGE = 5.0;
    public static final double SLASH_LINE_TRACE_SPEED = 0.035;

    public static int getKillLineLevel(ServerWorld world, ItemStack stack) {
        return getKillLineLevel(world.getRegistryManager(), stack);
    }

    public static int getKillLineLevel(DynamicRegistryManager drm, ItemStack stack) {
        if (stack.isEmpty()) return 0;

        Optional<? extends Registry<Enchantment>> optRegistry = drm.getOptional(RegistryKeys.ENCHANTMENT);
        if (optRegistry.isEmpty()) return 0;

        Registry<Enchantment> registry = optRegistry.get();
        Optional<RegistryEntry.Reference<Enchantment>> optEntry = registry.getEntry(KILL_LINE.getValue());
        if (optEntry.isEmpty()) return 0;

        return net.minecraft.enchantment.EnchantmentHelper.getLevel(optEntry.get(), stack);
    }

    public static float getThreshold(int level) {
        return 0.20f + (level - 1) * 0.05f;
    }

    public static float getKillChance(int level) {
        return level * 0.20f;
    }
}
