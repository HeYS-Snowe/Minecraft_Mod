package com.kill_line.kill_line.init;

import com.kill_line.kill_line.Constants;
import com.kill_line.kill_line.block.CriticalWorkshopBlock;
import com.kill_line.kill_line.block.EdictAnvilBlock;
import com.kill_line.kill_line.block.PermeationAltarBlock;
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
            DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    // 临界工坊 — 特殊合成台
    public static final RegistryObject<Block> CRITICAL_WORKSHOP = registerBlock(
            "critical_workshop",
            () -> new CriticalWorkshopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5.0f, 8.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 10))
    );

    // 渗透祭坛 — 武器注入 + 卷轴合成（有失败风险）
    public static final RegistryObject<Block> PERMEATION_ALTAR = registerBlock(
            "permeation_altar",
            () -> new PermeationAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(4.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 8))
    );

    // 诫命之砧 — 最终附魔
    public static final RegistryObject<Block> EDICT_ANVIL = registerBlock(
            "edict_anvil",
            () -> new EdictAnvilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(6.0f, 10.0f)
                    .sound(SoundType.ANVIL)
                    .lightLevel(state -> 5))
    );

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockSupplier) {
        RegistryObject<T> blockRO = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.register(name, () -> new BlockItem(blockRO.get(), new Item.Properties()));
        return blockRO;
    }
}
