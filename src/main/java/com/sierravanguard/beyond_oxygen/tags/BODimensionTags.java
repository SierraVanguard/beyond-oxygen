package com.sierravanguard.beyond_oxygen.tags;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;

public class BODimensionTags {
    public static final TagKey<Level> UNBREATHABLE = create("unbreathable");
    public static final TagKey<Level> COLD = create("cold");
    public static final TagKey<Level> HOT = create("hot");

    private static TagKey<Level> create(String path) {
        return create(new ResourceLocation(BeyondOxygen.MODID, path));
    }

    private static TagKey<Level> create(ResourceLocation location) {
        return TagKey.create(Registries.DIMENSION, location);
    }
}
