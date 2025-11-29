package com.sierravanguard.beyond_oxygen.tags;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class BOEntityTypeTags {
    public static final TagKey<EntityType<?>> SURVIVES_VACUUM = create("survives_vacuum");
    public static final TagKey<EntityType<?>> SURVIVES_COLD = create("survives_cold");
    public static final TagKey<EntityType<?>> SURVIVES_HOT = create("survives_hot");

    private static TagKey<EntityType<?>> create(String path) {
        return create(new ResourceLocation(BeyondOxygen.MODID, path));
    }

    private static TagKey<EntityType<?>> create(ResourceLocation location) {
        return TagKey.create(Registries.ENTITY_TYPE, location);
    }
}
