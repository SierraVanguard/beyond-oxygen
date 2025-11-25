package com.sierravanguard.beyond_oxygen.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class BOFluidTags {
    public static final TagKey<Fluid> OXYGEN = FluidTags.create(new ResourceLocation("c", "oxygen"));
    public static final TagKey<Fluid> FORGE_OXYGEN = FluidTags.create(new ResourceLocation("forge", "oxygen"));
}
