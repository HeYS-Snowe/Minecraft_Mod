package com.kill_line.kill_line.init;

import com.kill_line.kill_line.Constants;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    // === V2.0 物品 ===

    // 远古今石 — 远古城市共鸣石碑挖掘
    public static final RegistryObject<Item> ANCIENT_CORNERSTONE = ITEMS.register("ancient_cornerstone",
            () -> new Item(new Item.Properties()));

    // 虚空框架 — 末影之眼×2 + 黑曜石×4 工作台合成
    public static final RegistryObject<Item> VOID_FRAME = ITEMS.register("void_frame",
            () -> new Item(new Item.Properties()));

    // 血月核心 — 泣血玫瑰 + 月影祭坛合成
    public static final RegistryObject<Item> BLOOD_MOON_CORE = ITEMS.register("blood_moon_core",
            () -> new Item(new Item.Properties()));

    // 寂灭尘埃 — 虚空瓶收集龙息残渣 + 哭泣黑曜石
    public static final RegistryObject<Item> SILENCE_DUST = ITEMS.register("silence_dust",
            () -> new Item(new Item.Properties()));

    // 命运丝线 — 蜘蛛网×2 + 金锭×1 工作台合成
    public static final RegistryObject<Item> FATE_THREAD = ITEMS.register("fate_thread",
            () -> new Item(new Item.Properties()));

    // 古神残页 — 林地府邸/废弃矿井/钓鱼
    public static final RegistryObject<Item> ANCIENT_PAGE = ITEMS.register("ancient_page",
            () -> new Item(new Item.Properties()));

    // 临界精粹 — 临界工坊内用临界核心提纯
    public static final RegistryObject<Item> CRITICAL_ESSENCE = ITEMS.register("critical_essence",
            () -> new Item(new Item.Properties()));

    // 临界卷轴 — 渗透祭坛合成（有失败概率）
    public static final RegistryObject<Item> CRITICAL_SCROLL = ITEMS.register("critical_scroll",
            () -> new Item(new Item.Properties()));

    // 远古铁块 — 铁锭×4 + 远古碎片×1 烧炼
    public static final RegistryObject<Item> ANCIENT_IRON_BLOCK = ITEMS.register("ancient_iron_block",
            () -> new Item(new Item.Properties()));

    // 灵魂火花 — 凋灵骷髅掉落
    public static final RegistryObject<Item> SOUL_SPARK = ITEMS.register("soul_spark",
            () -> new Item(new Item.Properties()));

    // 泣血玫瑰 — 凋灵骷髅头 + 玫瑰合成
    public static final RegistryObject<Item> BLEEDING_ROSE = ITEMS.register("bleeding_rose",
            () -> new Item(new Item.Properties()));

    // 月影祭坛 — 雪块×4 + 青金石×1 合成
    public static final RegistryObject<Item> MOON_SHADOW_ALTAR = ITEMS.register("moon_shadow_altar",
            () -> new Item(new Item.Properties()));
}
