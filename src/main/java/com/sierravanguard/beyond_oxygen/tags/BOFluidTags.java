package com.sierravanguard.beyond_oxygen.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class BOFluidTags {
    public static TagKey<Fluid> OXYGEN = FluidTags.create(new ResourceLocation("c", "oxygen"));
}
