package com.kill_line.kill_line.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, com.kill_line.kill_line.Constants.MOD_ID);

    public static final RegistryObject<Enchantment> KILL_LINE = ENCHANTMENTS.register(
            "kill_line", KillLineEnchantment::new
    );

    public static final double SLASH_LINE_RANGE = 5.0;
    public static final double SLASH_LINE_TRACE_SPEED = 0.035;

    /**
     * Get Kill Line level from an ItemStack using the enchantment map.
     */
    public static int getKillLineLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Enchantment ench = KILL_LINE.get();
        if (ench == null) return 0;
        // 1.20.1: use getEnchantments() map to find level
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        return enchantments.getOrDefault(ench, 0);
    }

    public static float getThreshold(int level) {
        return 0.20f + (level - 1) * 0.05f;
    }

    public static float getKillChance(int level) {
        return level * 0.20f;
    }
}
