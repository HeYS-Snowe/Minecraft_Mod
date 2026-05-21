package com.kill_line.kill_line.init;

import com.kill_line.kill_line.Constants;
import com.kill_line.kill_line.blockentity.CriticalWorkshopBlockEntity;
import com.kill_line.kill_line.blockentity.EdictAnvilBlockEntity;
import com.kill_line.kill_line.blockentity.PermeationAltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public static final RegistryObject<BlockEntityType<CriticalWorkshopBlockEntity>> CRITICAL_WORKSHOP_BE =
            BLOCK_ENTITIES.register("critical_workshop", () ->
                    BlockEntityType.Builder.of(CriticalWorkshopBlockEntity::new, ModBlocks.CRITICAL_WORKSHOP.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<PermeationAltarBlockEntity>> PERMEATION_ALTAR_BE =
            BLOCK_ENTITIES.register("permeation_altar", () ->
                    BlockEntityType.Builder.of(PermeationAltarBlockEntity::new, ModBlocks.PERMEATION_ALTAR.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<EdictAnvilBlockEntity>> EDICT_ANVIL_BE =
            BLOCK_ENTITIES.register("edict_anvil", () ->
                    BlockEntityType.Builder.of(EdictAnvilBlockEntity::new, ModBlocks.EDICT_ANVIL.get())
                            .build(null));
}
