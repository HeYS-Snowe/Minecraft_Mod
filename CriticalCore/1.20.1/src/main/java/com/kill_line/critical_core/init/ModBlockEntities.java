package com.kill_line.critical_core.init;

import com.kill_line.critical_core.CriticalCore;
import com.kill_line.critical_core.blockentity.PulseForgeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CriticalCore.MODID);

    public static final RegistryObject<BlockEntityType<PulseForgeBlockEntity>> PULSE_FORGE_BE =
            BLOCK_ENTITIES.register("pulse_forge", () ->
                    BlockEntityType.Builder.of(PulseForgeBlockEntity::new, ModBlocks.PULSE_FORGE.get())
                            .build(null));
}
