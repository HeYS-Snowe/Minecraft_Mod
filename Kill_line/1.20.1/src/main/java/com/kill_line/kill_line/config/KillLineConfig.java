package com.kill_line.kill_line.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class KillLineConfig {

    public static final ForgeConfigSpec SPEC;

    public static final Difficulty DIFFICULTY;
    public static final ScrollCrafting SCROLL_CRAFTING;
    public static final Enchanting ENCHANTING;
    public static final OreConfig ORE_CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Difficulty level: NORMAL, HARD, EXTREME").push("difficulty");
        DIFFICULTY = new Difficulty(builder);
        builder.pop();

        builder.comment("Scroll crafting settings (Permeation Altar)").push("scroll_crafting");
        SCROLL_CRAFTING = new ScrollCrafting(builder);
        builder.pop();

        builder.comment("Enchantment settings (Edict Anvil)").push("enchanting");
        ENCHANTING = new Enchanting(builder);
        builder.pop();

        builder.comment("Ore generation (from CriticalCore integration)").push("ore");
        ORE_CONFIG = new OreConfig(builder);
        builder.pop();

        SPEC = builder.build();
    }

    public static class Difficulty {
        public final ForgeConfigSpec.EnumValue<DifficultyLevel> level;

        Difficulty(ForgeConfigSpec.Builder builder) {
            level = builder
                    .comment("Difficulty level affects all other settings as presets")
                    .defineEnum("level", DifficultyLevel.NORMAL);
        }

        public DifficultyLevel getLevel() {
            return level.get();
        }
    }

    public static class ScrollCrafting {
        public final ForgeConfigSpec.DoubleValue baseSuccessRate;
        public final ForgeConfigSpec.DoubleValue materialReturnMin;
        public final ForgeConfigSpec.DoubleValue materialReturnMax;
        public final ForgeConfigSpec.BooleanValue enableStableCore;
        public final ForgeConfigSpec.IntValue autoStabilizeAfter;

        ScrollCrafting(ForgeConfigSpec.Builder builder) {
            baseSuccessRate = builder
                    .comment("Base success rate for scroll crafting (0.0-1.0)")
                    .defineInRange("baseSuccessRate", 0.90, 0.0, 1.0);
            materialReturnMin = builder
                    .comment("Minimum material return on failure (fraction)")
                    .defineInRange("materialReturnMin", 0.60, 0.0, 1.0);
            materialReturnMax = builder
                    .comment("Maximum material return on failure (fraction)")
                    .defineInRange("materialReturnMax", 0.90, 0.0, 1.0);
            enableStableCore = builder
                    .comment("Whether the Stable Core insurance slot is available")
                    .define("enableStableCore", true);
            autoStabilizeAfter = builder
                    .comment("Auto-stabilize after N consecutive failures (0 = disabled)")
                    .defineInRange("autoStabilizeAfter", 2, 0, 10);
        }
    }

    public static class Enchanting {
        public final ForgeConfigSpec.DoubleValue baseSuccessRate;
        public final ForgeConfigSpec.IntValue anvilXpCost;
        public final ForgeConfigSpec.DoubleValue weaponDurabilityRetentionOnFail;

        Enchanting(ForgeConfigSpec.Builder builder) {
            baseSuccessRate = builder
                    .comment("Base success rate for enchanting on Edict Anvil (0.0-1.0)")
                    .defineInRange("baseSuccessRate", 0.85, 0.0, 1.0);
            anvilXpCost = builder
                    .comment("Experience levels consumed when forging the Edict Anvil")
                    .defineInRange("anvilXpCost", 100, 0, 1000);
            weaponDurabilityRetentionOnFail = builder
                    .comment("Weapon durability retention fraction on enchant failure")
                    .defineInRange("weaponDurabilityRetentionOnFail", 0.90, 0.0, 1.0);
        }
    }

    public static class OreConfig {
        public final ForgeConfigSpec.IntValue orePerChunk;

        OreConfig(ForgeConfigSpec.Builder builder) {
            orePerChunk = builder
                    .comment("Threshold crystal clusters per chunk (overridden by CriticalCore if installed)")
                    .defineInRange("orePerChunk", 2, 0, 20);
        }
    }

    public enum DifficultyLevel {
        NORMAL(0.90, 0.85, 0.60, 0.90, true, 2, 100, 2),
        HARD(0.70, 0.65, 0.40, 0.70, true, 3, 500, 1),
        EXTREME(0.50, 0.40, 0.20, 0.50, false, 5, 1000, 0);

        public final double scrollSuccessRate;
        public final double enchantSuccessRate;
        public final double materialReturnMin;
        public final double materialReturnMax;
        public final boolean stableCoreEnabled;
        public final int autoStabilizeAfter;
        public final int anvilXpCost;
        public final int orePerChunk;

        DifficultyLevel(double scrollSuccessRate, double enchantSuccessRate,
                        double materialReturnMin, double materialReturnMax,
                        boolean stableCoreEnabled, int autoStabilizeAfter,
                        int anvilXpCost, int orePerChunk) {
            this.scrollSuccessRate = scrollSuccessRate;
            this.enchantSuccessRate = enchantSuccessRate;
            this.materialReturnMin = materialReturnMin;
            this.materialReturnMax = materialReturnMax;
            this.stableCoreEnabled = stableCoreEnabled;
            this.autoStabilizeAfter = autoStabilizeAfter;
            this.anvilXpCost = anvilXpCost;
            this.orePerChunk = orePerChunk;
        }
    }
}
