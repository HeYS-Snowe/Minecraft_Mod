package com.kill_line.critical_core.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CriticalCoreConfig {

    public static final ForgeConfigSpec SPEC;

    public static final OreGeneration ORE_GENERATION;
    public static final PulseForge PULSE_FORGE;
    public static final ThresholdPerception THRESHOLD_PERCEPTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Ore generation settings").push("ore_generation");
        ORE_GENERATION = new OreGeneration(builder);
        builder.pop();

        builder.comment("Pulse Forge settings").push("pulse_forge");
        PULSE_FORGE = new PulseForge(builder);
        builder.pop();

        builder.comment("Threshold Perception settings").push("threshold_perception");
        THRESHOLD_PERCEPTION = new ThresholdPerception(builder);
        builder.pop();

        SPEC = builder.build();
    }

    public static class OreGeneration {
        public final ForgeConfigSpec.IntValue orePerChunk;
        public final ForgeConfigSpec.IntValue minY;
        public final ForgeConfigSpec.IntValue maxY;
        public final ForgeConfigSpec.IntValue veinSize;

        OreGeneration(ForgeConfigSpec.Builder builder) {
            orePerChunk = builder
                    .comment("Number of ore veins per chunk")
                    .defineInRange("orePerChunk", 2, 0, 20);
            minY = builder
                    .comment("Minimum Y level for ore generation")
                    .defineInRange("minY", -64, -64, 320);
            maxY = builder
                    .comment("Maximum Y level for ore generation")
                    .defineInRange("maxY", -30, -64, 320);
            veinSize = builder
                    .comment("Number of blocks per vein")
                    .defineInRange("veinSize", 5, 1, 20);
        }
    }

    public static class PulseForge {
        public final ForgeConfigSpec.IntValue experienceCost;
        public final ForgeConfigSpec.IntValue craftingTimeTicks;
        public final ForgeConfigSpec.IntValue shardsRequired;

        PulseForge(ForgeConfigSpec.Builder builder) {
            experienceCost = builder
                    .comment("Experience levels consumed per crafting")
                    .defineInRange("experienceCost", 5, 0, 100);
            craftingTimeTicks = builder
                    .comment("Crafting time in ticks (20 ticks = 1 second)")
                    .defineInRange("craftingTimeTicks", 100, 20, 600);
            shardsRequired = builder
                    .comment("Number of Threshold Shards required per Critical Core")
                    .defineInRange("shardsRequired", 8, 1, 64);
        }
    }

    public static class ThresholdPerception {
        public final ForgeConfigSpec.DoubleValue detectionRange;
        public final ForgeConfigSpec.DoubleValue healthThreshold;

        ThresholdPerception(ForgeConfigSpec.Builder builder) {
            detectionRange = builder
                    .comment("Detection range in blocks")
                    .defineInRange("detectionRange", 16.0, 1.0, 64.0);
            healthThreshold = builder
                    .comment("Health percentage threshold (0.0-1.0) for detection")
                    .defineInRange("healthThreshold", 0.20, 0.01, 1.0);
        }
    }
}
