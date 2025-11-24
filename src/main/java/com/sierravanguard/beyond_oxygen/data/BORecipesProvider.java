package com.sierravanguard.beyond_oxygen.data;

import com.sierravanguard.beyond_oxygen.registry.BOItems;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class BORecipesProvider extends RecipeProvider {
    public BORecipesProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.SPACESUIT_HELMET.get())
                .pattern("SSS")
                .pattern("SGS")
                .define('S', BOItemTags.STEEL_INGOT)
                .define('G', BOItemTags.GLASS_BLOCK)
                .unlockedBy("has_steel", has(BOItemTags.STEEL_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.SPACESUIT_CHESTPLATE.get())
                .pattern("S S")
                .pattern("SCS")
                .pattern("SSS")
                .define('S', BOItemTags.STEEL_INGOT)
                .define('C', MekanismItems.BASIC_CONTROL_CIRCUIT)
                .unlockedBy("has_steel", has(BOItemTags.STEEL_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.SPACESUIT_LEGGINGS.get())
                .pattern("CSC")
                .pattern("S S")
                .pattern("S S")
                .define('S', BOItemTags.STEEL_INGOT)
                .define('C', MekanismItems.BASIC_CONTROL_CIRCUIT)
                .unlockedBy("has_steel", has(BOItemTags.STEEL_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.SPACESUIT_BOOTS.get())
                .pattern("S S")
                .pattern("S S")
                .define('S', BOItemTags.STEEL_INGOT)
                .unlockedBy("has_steel", has(BOItemTags.STEEL_INGOT))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.CRYO_SUIT_HELMET.get())
                .pattern("OOO")
                .pattern("OCO")
                .pattern(" H ")
                .define('O', BOItemTags.REFINED_OBSIDIAN_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('H', BOItems.SPACESUIT_HELMET.get())
                .unlockedBy("has_spacesuit_helmet", has(BOItems.SPACESUIT_HELMET.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.CRYO_SUIT_CHESTPLATE.get())
                .pattern("OCO")
                .pattern("OCO")
                .pattern(" S ")
                .define('O', BOItemTags.REFINED_OBSIDIAN_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('S', BOItems.SPACESUIT_CHESTPLATE.get())
                .unlockedBy("has_spacesuit_chestplate", has(BOItems.SPACESUIT_CHESTPLATE.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.CRYO_SUIT_LEGGINGS.get())
                .pattern("COC")
                .pattern(" L ")
                .define('O', BOItemTags.REFINED_OBSIDIAN_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('L', BOItems.SPACESUIT_LEGGINGS.get())
                .unlockedBy("has_spacesuit_leggings", has(BOItems.SPACESUIT_LEGGINGS.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.CRYO_SUIT_BOOTS.get())
                .pattern("OCO")
                .pattern(" B ")
                .define('O', BOItemTags.REFINED_OBSIDIAN_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('B', BOItems.SPACESUIT_BOOTS.get())
                .unlockedBy("has_spacesuit_boots", has(BOItems.SPACESUIT_BOOTS.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.THERMAL_HELMET.get())
                .pattern("OOO")
                .pattern("OCO")
                .pattern(" H ")
                .define('O', BOItemTags.REFINED_GLOWSTONE_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('H', BOItems.SPACESUIT_HELMET.get())
                .unlockedBy("has_spacesuit_helmet", has(BOItems.SPACESUIT_HELMET.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.THERMAL_CHESTPLATE.get())
                .pattern("OCO")
                .pattern("OCO")
                .pattern(" S ")
                .define('O', BOItemTags.REFINED_GLOWSTONE_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('S', BOItems.SPACESUIT_CHESTPLATE.get())
                .unlockedBy("has_spacesuit_chestplate", has(BOItems.SPACESUIT_CHESTPLATE.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.THERMAL_LEGGINGS.get())
                .pattern("COC")
                .pattern(" L ")
                .define('O', BOItemTags.REFINED_GLOWSTONE_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('L', BOItems.SPACESUIT_LEGGINGS.get())
                .unlockedBy("has_spacesuit_leggings", has(BOItems.SPACESUIT_LEGGINGS.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, BOItems.THERMAL_BOOTS.get())
                .pattern("OCO")
                .pattern(" B ")
                .define('O', BOItemTags.REFINED_GLOWSTONE_INGOT)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('B', BOItems.SPACESUIT_BOOTS.get())
                .unlockedBy("has_spacesuit_boots", has(BOItems.SPACESUIT_BOOTS.get()))
                .save(writer);

        //TODO OXYGEN_TANK
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BOItems.THERMAL_REGULATOR.get())
                .pattern("IRI")
                .pattern("RCR")
                .pattern("IRI")
                .define('I', BOItemTags.REFINED_OBSIDIAN_INGOT)
                .define('R', BOItemTags.REDSTONE_DUST)
                .define('C', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .unlockedBy("has_refined_obsidian", has(BOItemTags.REFINED_OBSIDIAN_INGOT))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BOItems.EMPTY_CAN.get(), 4)
                .pattern("N N")
                .pattern(" N ")
                .define('N', BOItemTags.IRON_NUGGET)
                .unlockedBy("has_iron_nuggets", has(BOItemTags.IRON_NUGGET))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BOItems.CANNED_POTATOES.get())
                .requires(BOItemTags.POTATO)
                .requires(BOItems.EMPTY_CAN.get())
                .unlockedBy("has_empty_can", has(BOItems.EMPTY_CAN.get()))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BOItems.CANNED_APPLE.get())
                .requires(Items.APPLE)
                .requires(BOItems.EMPTY_CAN.get())
                .unlockedBy("has_empty_can", has(BOItems.EMPTY_CAN.get()))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BOItems.CANNED_BAGUETTE.get())
                .requires(BOItemTags.BREAD)
                .requires(BOItems.EMPTY_CAN.get())
                .unlockedBy("has_empty_can", has(BOItems.EMPTY_CAN.get()))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BOItems.CANNED_BEEF.get())
                .requires(Items.BEEF)
                .requires(BOItems.EMPTY_CAN.get())
                .unlockedBy("has_empty_can", has(BOItems.EMPTY_CAN.get()))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BOItems.CANNED_PORK.get())
                .requires(Items.PORKCHOP)
                .requires(BOItems.EMPTY_CAN.get())
                .unlockedBy("has_empty_can", has(BOItems.EMPTY_CAN.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BOItems.VENT.get())
                .pattern("#B#")
                .pattern("RSR")
                .pattern("#I#")
                .define('#', MekanismItems.INFUSED_ALLOY)
                .define('B', MekanismBlocks.BASIC_FLUID_TANK)
                .define('R', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('S', MekanismBlocks.STEEL_CASING)
                .define('I', Items.IRON_BARS)
                .unlockedBy("has_steel_casing", has(MekanismBlocks.STEEL_CASING))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BOItems.OXYGEN_HARVESTER.get())
                .pattern(" V ")
                .pattern("ACA")
                .pattern(" V ")
                .define('V', BOItems.VENT.get())
                .define('A', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('C', MekanismBlocks.ROTARY_CONDENSENTRATOR)
                .unlockedBy("has_vent", has(BOItems.VENT.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BOItems.BUBBLE_GENERATOR.get())
                .pattern(" V ")
                .pattern("ACA")
                .pattern(" V ")
                .define('V', BOItems.VENT.get())
                .define('A', MekanismItems.ADVANCED_CONTROL_CIRCUIT)
                .define('C', MekanismBlocks.BASIC_FLUID_TANK)
                .unlockedBy("has_vent", has(BOItems.VENT.get()))
                .save(writer);
        //TODO CRYO_BED

    }
}
