package com.kill_line.kill_line.init;

import com.kill_line.kill_line.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final RegistryObject<CreativeModeTab> KILL_LINE_TAB =
            CREATIVE_MODE_TABS.register("kill_line_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kill_line"))
                    .icon(() -> new ItemStack(Items.NETHERITE_SWORD))
                    .displayItems((parameters, output) -> {
                        // V2.0 Items
                        output.accept(ModItems.ANCIENT_CORNERSTONE.get());
                        output.accept(ModItems.VOID_FRAME.get());
                        output.accept(ModItems.BLOOD_MOON_CORE.get());
                        output.accept(ModItems.SILENCE_DUST.get());
                        output.accept(ModItems.FATE_THREAD.get());
                        output.accept(ModItems.ANCIENT_PAGE.get());
                        output.accept(ModItems.CRITICAL_ESSENCE.get());
                        output.accept(ModItems.CRITICAL_SCROLL.get());
                        output.accept(ModItems.ANCIENT_IRON_BLOCK.get());
                        output.accept(ModItems.SOUL_SPARK.get());
                        output.accept(ModItems.BLEEDING_ROSE.get());
                        output.accept(ModItems.MOON_SHADOW_ALTAR.get());
                        // V2.0 Block items
                        output.accept(ModBlocks.CRITICAL_WORKSHOP.get());
                        output.accept(ModBlocks.PERMEATION_ALTAR.get());
                        output.accept(ModBlocks.EDICT_ANVIL.get());
                    })
                    .build());
}
