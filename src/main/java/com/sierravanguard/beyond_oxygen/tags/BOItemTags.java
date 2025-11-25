package com.sierravanguard.beyond_oxygen.tags;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class BOItemTags {
    public static final TagKey<Item> SPACE_SUIT_HELMETS = create("armor/space_suit/helmets");
    public static final TagKey<Item> SPACE_SUIT_CHESTPLATES = create("armor/space_suit/chestplates");
    public static final TagKey<Item> SPACE_SUIT_LEGGINGS = create("armor/space_suit/leggings");
    public static final TagKey<Item> SPACE_SUIT_BOOTS = create("armor/space_suit/boots");

    public static final TagKey<Item> CRYO_HELMETS = create("armor/cryo/helmets");
    public static final TagKey<Item> CRYO_CHESTPLATES = create("armor/cryo/chestplates");
    public static final TagKey<Item> CRYO_LEGGINGS = create("armor/cryo/leggings");
    public static final TagKey<Item> CRYO_BOOTS = create("armor/cryo/boots");

    public static final TagKey<Item> THERMAL_HELMETS = create("armor/thermal/helmets");
    public static final TagKey<Item> THERMAL_CHESTPLATES = create("armor/thermal/chestplates");
    public static final TagKey<Item> THERMAL_LEGGINGS = create("armor/thermal/leggings");
    public static final TagKey<Item> THERMAL_BOOTS = create("armor/thermal/boots");

    public static final TagKey<Item> SPACE_SUIT_REPAIR_MATERIAL = create("repair_item/space_suit");
    public static final TagKey<Item> CRYO_REPAIR_MATERIAL = create("repair_item/cryo");
    public static final TagKey<Item> THERMAL_REPAIR_MATERIAL = create("repair_item/thermal");

    public static final TagKey<Item> SPACE_SUIT_EATABLE = create("space_suit_eatable");
    public static final TagKey<Item> BREATHABLES = create("breathables");


    public static final TagKey<Item> FORGE_STEEL_INGOT = ItemTags.create(new ResourceLocation("forge", "ingots/steel"));
    public static final TagKey<Item> STEEL_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/steel"));
    public static final TagKey<Item> REFINED_OBSIDIAN_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/refined_obsidian"));
    public static final TagKey<Item> REFINED_GLOWSTONE_INGOT = ItemTags.create(new ResourceLocation("c", "ingots/refined_glowstone"));

    public static final TagKey<Item> IRON_NUGGET = ItemTags.create(new ResourceLocation("c", "nuggets/iron"));

    public static final TagKey<Item> REDSTONE_DUST = ItemTags.create(new ResourceLocation("c", "dusts/redstone"));

    public static final TagKey<Item> POTATO = ItemTags.create(new ResourceLocation("c", "crops/potato"));
    public static final TagKey<Item> BREAD = ItemTags.create(new ResourceLocation("c", "foods/bread"));

    public static final TagKey<Item> GLASS_BLOCK = ItemTags.create(new ResourceLocation("c", "glass_blocks"));

    private static TagKey<Item> create(String path) {
        return ItemTags.create(new ResourceLocation(BeyondOxygen.MODID, path));
    }
}
