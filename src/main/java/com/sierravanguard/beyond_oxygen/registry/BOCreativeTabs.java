package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BOCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BeyondOxygen.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.beyond_oxygen.main"))
                    .icon(() -> {
                        ItemStack stack = new ItemStack(BOItems.TAB_ICON.get());
                        stack.getOrCreateTag().putBoolean("HideFlags", true);
                        return stack;
                    })
                    .displayItems((params, output) -> {
                        BOItems.ITEMS.getEntries().forEach(item -> {
                            if (item != BOItems.TAB_ICON) {
                                output.accept(item.get());
                            }
                        });
                    })
                    .build());
}