package com.kill_line.critical_core.init;

import com.kill_line.critical_core.CriticalCore;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CriticalCore.MODID);

    // 阈界碎片 — 矿石掉落物，脉冲锻炉的原料
    public static final RegistryObject<Item> THRESHOLD_SHARD = ITEMS.register("threshold_shard",
            () -> new Item(new Item.Properties()));

    // 临界核心 — 脉冲锻炉产物，持有时激活阈值感知
    public static final RegistryObject<Item> CRITICAL_CORE = ITEMS.register("critical_core",
            () -> new Item(new Item.Properties()));

    // 临界手札 — 引导道具，右键阅读
    public static final RegistryObject<Item> CRITICAL_HANDBOOK = ITEMS.register("critical_handbook",
            () -> new Item(new Item.Properties()));

    // 虚空瓶 — 用于收集龙息残渣
    public static final RegistryObject<Item> VOID_BOTTLE = ITEMS.register("void_bottle",
            () -> new Item(new Item.Properties()));

    // 稳定核心 — 用于保险槽防止合成失败
    public static final RegistryObject<Item> STABLE_CORE = ITEMS.register("stable_core",
            () -> new Item(new Item.Properties()));
}
