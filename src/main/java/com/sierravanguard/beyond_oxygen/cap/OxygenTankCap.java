package com.sierravanguard.beyond_oxygen.cap;

import com.sierravanguard.beyond_oxygen.registry.BOFluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.sierravanguard.beyond_oxygen.BOConfig;

public class OxygenTankCap extends FluidHandlerItemStack {
    public OxygenTankCap(@NotNull ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return BOFluids.isOxygen(fluid);
    }
}