package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.client.menu.BubbleGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BOMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, BeyondOxygen.MODID);

    public static final RegistryObject<MenuType<BubbleGeneratorMenu>> BUBBLE_GENERATOR =
            MENUS.register("bubble_generator", () -> IForgeMenuType.create(BubbleGeneratorMenu::new));
}
