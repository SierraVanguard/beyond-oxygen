package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.items.*;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.items.armor.SpaceSuitArmorMaterial;
import com.sierravanguard.beyond_oxygen.items.armor.SpacesuitArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BOItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BeyondOxygen.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BeyondOxygen.MODID);
    public static final RegistryObject<Item> OXYGEN_TANK = ITEMS.register("oxygen_tank",()->new OxygenTank(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EMPTY_CAN = ITEMS.register("empty_can",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> VENT = ITEMS.register("vent", ()-> new BlockItem(BOBlocks.VENT.get(), new Item.Properties()));
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
                @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Not Enough Oxygen").withStyle(ChatFormatting.BLUE));
                }});
    public static final RegistryObject<Item> BUBBLE_GENERATOR = ITEMS.register("bubble_generator", () -> new BlockItem(BOBlocks.BUBBLE_GENERATOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> SPACESUIT_HELMET = ITEMS.register("spacesuit_helmet",
            () -> new OpenableSpacesuitHelmetItem(SpaceSuitArmorMaterial.SPACESUIT, ArmorItem.Type.HELMET, new Item.Properties(), "spacesuit_helmet"));
    public static final RegistryObject<Item> SPACESUIT_CHESTPLATE = ITEMS.register("spacesuit_chestplate",
            () -> new SpacesuitArmorItem(SpaceSuitArmorMaterial.SPACESUIT, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SPACESUIT_LEGGINGS = ITEMS.register("spacesuit_leggings",
            () -> new SpacesuitArmorItem(SpaceSuitArmorMaterial.SPACESUIT, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SPACESUIT_BOOTS = ITEMS.register("spacesuit_boots",
            () -> new SpacesuitArmorItem(SpaceSuitArmorMaterial.SPACESUIT, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> CANNED_POTATOES = ITEMS.register("canned_potatoes",
            () -> new CannedFoodItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.8F).build())
                    .stacksTo(16), EMPTY_CAN.get()));

    public static final RegistryObject<Item> CANNED_APPLE = ITEMS.register("canned_apple",
            () -> new CannedFoodItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(5).saturationMod(1F).build())
                    .stacksTo(16), EMPTY_CAN.get()));
    public static final RegistryObject<Item> CANNED_BAGUETTE = ITEMS.register("canned_baguette",
            () -> new CannedFoodItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.7F).build())
                    .stacksTo(16), EMPTY_CAN.get()) {
                @Override
                public Component getName(ItemStack stack) {
                    return Component.translatable("special.item.cannedbaguette").withStyle(ChatFormatting.DARK_PURPLE);
                }
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("special.message.matt").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
                }
            });
    public static final RegistryObject<Item> CANNED_BEEF = ITEMS.register("canned_beef",
            () -> new CannedFoodItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(7).saturationMod(2.0F).build())
                    .stacksTo(16), EMPTY_CAN.get()));
    public static final RegistryObject<Item> CANNED_PORK = ITEMS.register("canned_pork",
            () -> new CannedFoodItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(5).saturationMod(1.5F).build())
                    .stacksTo(16), EMPTY_CAN.get()));
    public static final RegistryObject<Item> CRYO_BED = ITEMS.register("cryo_bed",
            () -> new BlockItem(BOBlocks.CRYO_BED.get(), new Item.Properties()));
}