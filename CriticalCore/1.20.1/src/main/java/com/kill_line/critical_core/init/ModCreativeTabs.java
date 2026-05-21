package com.kill_line.critical_core.init;

import com.kill_line.critical_core.CriticalCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CriticalCore.MODID);

    public static final RegistryObject<CreativeModeTab> CRITICAL_CORE_TAB =
            CREATIVE_MODE_TABS.register("critical_core_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.critical_core"))
                    .icon(() -> new ItemStack(ModItems.CRITICAL_CORE.get()))
                    .displayItems((parameters, output) -> {
                        // Items
                        output.accept(ModItems.THRESHOLD_SHARD.get());
                        output.accept(ModItems.CRITICAL_CORE.get());
                        output.accept(ModItems.CRITICAL_HANDBOOK.get());
                        output.accept(ModItems.VOID_BOTTLE.get());
                        output.accept(ModItems.STABLE_CORE.get());
                        // Block items
                        output.accept(ModBlocks.THRESHOLD_CRYSTAL_CLUSTER.get());
                        output.accept(ModBlocks.PULSE_FORGE.get());
                    })
                    .build());
}
