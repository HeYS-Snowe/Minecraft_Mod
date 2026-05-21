package com.kill_line.kill_line.enchantment;

import com.kill_line.kill_line.integration.CriticalCoreIntegration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * The Kill Line enchantment.
 * When applied to a weapon, it marks targets and can trigger instant kills
 * when the target's health falls below a threshold.
 *
 * When CriticalCore is installed: treasure-only, can only be obtained via Edict Anvil.
 * When CriticalCore is NOT installed: obtainable from enchanting tables (V1.x behavior).
 */
public class KillLineEnchantment extends Enchantment {

    public KillLineEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return CriticalCoreIntegration.shouldBlockEnchantingTable();
    }

    @Override
    public boolean isCurse() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return !CriticalCoreIntegration.shouldBlockEnchantingTable();
    }

    @Override
    public boolean isDiscoverable() {
        return !CriticalCoreIntegration.shouldBlockEnchantingTable();
    }
}
