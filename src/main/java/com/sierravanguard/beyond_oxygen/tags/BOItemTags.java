package com.sierravanguard.beyond_oxygen.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class BOItemTags {
    public static TagKey<Item> FORGE_STEEL_INGOT = ItemTags.create(new ResourceLocation("forge", "ingots/steel"));
    public static TagKey<Item> STEEL_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/steel"));
    public static TagKey<Item> REFINED_OBSIDIAN_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/refined_obsidian"));
    public static TagKey<Item> REFINED_GLOWSTONE_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/refined_glowstone"));

    public static TagKey<Item> IRON_NUGGET = ItemTags.create(new ResourceLocation("c", "nuggets/iron"));

    public static TagKey<Item> REDSTONE_DUST = ItemTags.create(new ResourceLocation("c", "dusts/redstone"));

    public static TagKey<Item> POTATO = ItemTags.create(new ResourceLocation("c", "crops/potato"));
    public static TagKey<Item> BREAD = ItemTags.create(new ResourceLocation("c", "foods/bread"));

    public static TagKey<Item> GLASS_BLOCK = ItemTags.create(new ResourceLocation("c", "glass_blocks"));
}
