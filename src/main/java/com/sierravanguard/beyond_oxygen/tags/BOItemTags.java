package com.sierravanguard.beyond_oxygen.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class BOItemTags {
    public static final TagKey<Item> FORGE_STEEL_INGOT = ItemTags.create(new ResourceLocation("forge", "ingots/steel"));
    public static final TagKey<Item> STEEL_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/steel"));
    public static final TagKey<Item> REFINED_OBSIDIAN_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/refined_obsidian"));
    public static final TagKey<Item> REFINED_GLOWSTONE_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/refined_glowstone"));

    public static final TagKey<Item> IRON_NUGGET = ItemTags.create(new ResourceLocation("c", "nuggets/iron"));

    public static final TagKey<Item> REDSTONE_DUST = ItemTags.create(new ResourceLocation("c", "dusts/redstone"));

    public static final TagKey<Item> POTATO = ItemTags.create(new ResourceLocation("c", "crops/potato"));
    public static final TagKey<Item> BREAD = ItemTags.create(new ResourceLocation("c", "foods/bread"));

    public static final TagKey<Item> GLASS_BLOCK = ItemTags.create(new ResourceLocation("c", "glass_blocks"));
}
