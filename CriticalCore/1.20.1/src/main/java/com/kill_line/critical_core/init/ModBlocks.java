package com.kill_line.critical_core.init;

import com.kill_line.critical_core.CriticalCore;
import com.kill_line.critical_core.block.PulseForgeBlock;
import com.kill_line.critical_core.block.ThresholdCrystalClusterBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CriticalCore.MODID);

    // 阈界晶簇 — 稀有矿石，生成于基岩层
    public static final RegistryObject<Block> THRESHOLD_CRYSTAL_CLUSTER = registerBlock(
            "threshold_crystal_cluster",
            () -> new ThresholdCrystalClusterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 3.0f)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 5)
                    .requiresCorrectToolForDrops())
    );

    // 脉冲锻炉 — 特殊工作台
    public static final RegistryObject<Block> PULSE_FORGE = registerBlock(
            "pulse_forge",
            () -> new PulseForgeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(4.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7))
    );

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockSupplier) {
        RegistryObject<T> blockRegistryObject = BLOCKS.register(name, blockSupplier);
        registerBlockItem(name, blockRegistryObject);
        return blockRegistryObject;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> blockRegistryObject) {
        ModItems.ITEMS.register(name, () -> new BlockItem(blockRegistryObject.get(), new Item.Properties()));
    }
}
