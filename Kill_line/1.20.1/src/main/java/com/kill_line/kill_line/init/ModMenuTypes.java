package com.kill_line.kill_line.init;

import com.kill_line.kill_line.Constants;
import com.kill_line.kill_line.menu.CriticalWorkshopMenu;
import com.kill_line.kill_line.menu.EdictAnvilMenu;
import com.kill_line.kill_line.menu.PermeationAltarMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Constants.MOD_ID);

    public static final RegistryObject<MenuType<CriticalWorkshopMenu>> CRITICAL_WORKSHOP_MENU =
            MENU_TYPES.register("critical_workshop", () ->
                    IForgeMenuType.create(CriticalWorkshopMenu::new));

    public static final RegistryObject<MenuType<PermeationAltarMenu>> PERMEATION_ALTAR_MENU =
            MENU_TYPES.register("permeation_altar", () ->
                    IForgeMenuType.create(PermeationAltarMenu::new));

    public static final RegistryObject<MenuType<EdictAnvilMenu>> EDICT_ANVIL_MENU =
            MENU_TYPES.register("edict_anvil", () ->
                    IForgeMenuType.create(EdictAnvilMenu::new));
}
