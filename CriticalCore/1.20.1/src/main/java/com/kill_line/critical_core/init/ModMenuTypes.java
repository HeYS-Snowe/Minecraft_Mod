package com.kill_line.critical_core.init;

import com.kill_line.critical_core.CriticalCore;
import com.kill_line.critical_core.menu.PulseForgeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CriticalCore.MODID);

    public static final RegistryObject<MenuType<PulseForgeMenu>> PULSE_FORGE_MENU =
            MENU_TYPES.register("pulse_forge", () ->
                    IForgeMenuType.create(PulseForgeMenu::new));
}
