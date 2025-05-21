package com.kito1z.not_enough_oxygen.registry;

import com.kito1z.not_enough_oxygen.NotEnoughOxygen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class NEOCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NotEnoughOxygen.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.not_enough_oxygen.main"))
                    .icon(() -> {
                        ItemStack stack = new ItemStack(NEOItems.TAB_ICON.get());
                        stack.getOrCreateTag().putBoolean("HideFlags", true);
                        return stack;
                    })
                    .displayItems((params, output) -> {
                        NEOItems.ITEMS.getEntries().forEach(item -> {
                            if (item != NEOItems.TAB_ICON) { // Exclude the icon from the tab
                                output.accept(item.get());
                            }
                        });
                    })
                    .build());
}