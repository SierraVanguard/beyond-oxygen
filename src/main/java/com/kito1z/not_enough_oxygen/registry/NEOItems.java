package com.kito1z.not_enough_oxygen.registry;

import com.kito1z.not_enough_oxygen.NotEnoughOxygen;
import com.kito1z.not_enough_oxygen.items.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class NEOItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NotEnoughOxygen.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NotEnoughOxygen.MODID);
    public static final RegistryObject<Item> OXYGEN_TANK = ITEMS.register("oxygen_tank",()->new OxygenTank(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> VENT = ITEMS.register("vent", ()-> new BlockItem(NEOBlocks.VENT.get(), new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_OXYGEN_TANK = ITEMS.register("medium_oxygen_tank",
            () -> new MediumOxygenTank(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SMALL_OXYGEN_TANK = ITEMS.register("small_oxygen_tank",
            () -> new SmallOxygenTank(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LARGE_OXYGEN_TANK = ITEMS.register("large_oxygen_tank",
            () -> new LargeOxygenTank(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ATMOSPHERIC_OXYGEN_TANK = ITEMS.register("atmospheric_oxygen_tank",
            () -> new AtmosphericOxygenTank(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TAB_ICON = ITEMS.register("tab_icon",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Not Enough Oxygen").withStyle(ChatFormatting.BLUE));
                }
            });
}