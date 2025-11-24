package com.sierravanguard.beyond_oxygen.data;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.registry.BOItems;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BOItemTagsProvider extends TagsProvider<Item> {
    public BOItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, Registries.ITEM, lookupProvider, BeyondOxygen.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        this.tag(BOItemTags.STEEL_INGOT)
                .addOptionalTag(BOItemTags.FORGE_STEEL_INGOT);
        this.tag(BOItemTags.REFINED_OBSIDIAN_INGOT)
                .addOptionalTag(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN);
        this.tag(BOItemTags.REFINED_GLOWSTONE_INGOT)
                .addOptionalTag(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE);

        this.tag(BOItemTags.IRON_NUGGET)
                .addOptionalTag(Tags.Items.NUGGETS_IRON);

        this.tag(BOItemTags.REDSTONE_DUST)
                .addTag(Tags.Items.DUSTS_REDSTONE);

        this.tag(BOItemTags.POTATO)
                .addTag(Tags.Items.CROPS_POTATO);
        this.tag(BOItemTags.BREAD)
                .add(Items.BREAD.builtInRegistryHolder().key());

        this.tag(BOItemTags.GLASS_BLOCK)
                .addTag(Tags.Items.GLASS);

        this.tag(Tags.Items.ARMORS_HELMETS)
                .add(BOItems.SPACESUIT_HELMET.getKey())
                .add(BOItems.CRYO_SUIT_HELMET.getKey())
                .add(BOItems.THERMAL_HELMET.getKey());
        this.tag(Tags.Items.ARMORS_CHESTPLATES)
                .add(BOItems.SPACESUIT_CHESTPLATE.getKey())
                .add(BOItems.CRYO_SUIT_CHESTPLATE.getKey())
                .add(BOItems.THERMAL_CHESTPLATE.getKey());
        this.tag(Tags.Items.ARMORS_LEGGINGS)
                .add(BOItems.SPACESUIT_LEGGINGS.getKey())
                .add(BOItems.CRYO_SUIT_LEGGINGS.getKey())
                .add(BOItems.THERMAL_LEGGINGS.getKey());
        this.tag(Tags.Items.ARMORS_BOOTS)
                .add(BOItems.SPACESUIT_BOOTS.getKey())
                .add(BOItems.CRYO_SUIT_BOOTS.getKey())
                .add(BOItems.THERMAL_BOOTS.getKey());
    }
}
