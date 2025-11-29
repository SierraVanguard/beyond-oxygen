package com.sierravanguard.beyond_oxygen.data;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.tags.BOEntityTypeTags;
import earth.terrarium.adastra.common.tags.ModEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BOEntityTypeTagsProvider extends TagsProvider<EntityType<?>> {
    public BOEntityTypeTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, Registries.ENTITY_TYPE, lookupProvider, BeyondOxygen.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        this.tag(BOEntityTypeTags.SURVIVES_VACUUM)
                .addTag(EntityTypeTags.SKELETONS)
                .add(EntityType.SKELETON_HORSE.builtInRegistryHolder().key())
                .add(EntityType.ENDERMAN.builtInRegistryHolder().key())
                .add(EntityType.ENDERMITE.builtInRegistryHolder().key())
                .add(EntityType.ENDER_DRAGON.builtInRegistryHolder().key())
                .add(EntityType.SHULKER.builtInRegistryHolder().key())
                .add(EntityType.IRON_GOLEM.builtInRegistryHolder().key())
                .add(EntityType.SNOW_GOLEM.builtInRegistryHolder().key())
                .add(EntityType.WITHER.builtInRegistryHolder().key())
                .addOptionalTag(ModEntityTypeTags.LIVES_WITHOUT_OXYGEN);
        this.tag(BOEntityTypeTags.SURVIVES_COLD)
                .addTag(EntityTypeTags.SKELETONS)
                .add(EntityType.SKELETON_HORSE.builtInRegistryHolder().key())
                .add(EntityType.ENDERMAN.builtInRegistryHolder().key())
                .add(EntityType.ENDERMITE.builtInRegistryHolder().key())
                .add(EntityType.ENDER_DRAGON.builtInRegistryHolder().key())
                .add(EntityType.SHULKER.builtInRegistryHolder().key())
                .add(EntityType.IRON_GOLEM.builtInRegistryHolder().key())
                .addTag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)
                .addOptionalTag(ModEntityTypeTags.CAN_SURVIVE_EXTREME_COLD);
        this.tag(BOEntityTypeTags.SURVIVES_HOT)
                .addTag(EntityTypeTags.SKELETONS)
                .add(EntityType.SKELETON_HORSE.builtInRegistryHolder().key())
                .add(EntityType.BLAZE.builtInRegistryHolder().key())
                .add(EntityType.ENDER_DRAGON.builtInRegistryHolder().key())
                .add(EntityType.GHAST.builtInRegistryHolder().key())
                .add(EntityType.HOGLIN.builtInRegistryHolder().key())
                .add(EntityType.HUSK.builtInRegistryHolder().key())
                .add(EntityType.MAGMA_CUBE.builtInRegistryHolder().key())
                .add(EntityType.PIGLIN.builtInRegistryHolder().key())
                .add(EntityType.PIGLIN_BRUTE.builtInRegistryHolder().key())
                .add(EntityType.ZOMBIFIED_PIGLIN.builtInRegistryHolder().key())
                .add(EntityType.ZOGLIN.builtInRegistryHolder().key())
                .addOptionalTag(ModEntityTypeTags.CAN_SURVIVE_EXTREME_HEAT);

    }
}
