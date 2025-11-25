package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.tags.BOFluidTags;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class BOFluids {
    private static Set<Fluid> oxygenFluids = Set.of();

    public static void populateFluids(RegistryAccess registryAccess) {
        oxygenFluids = registryAccess.lookupOrThrow(Registries.FLUID).get(BOFluidTags.OXYGEN).map(named -> named.stream().map(Holder::get).collect(Collectors.toSet())).orElse(Set.of());
        StringJoiner joiner = new StringJoiner(", ", "Accepted oxygen fluids: ", "");
        oxygenFluids.forEach(fluid -> joiner.add(fluid.builtInRegistryHolder().key().location().toString()));
        BeyondOxygen.LOGGER.info(joiner.toString());
    }

    public static void releaseFluids() {
        oxygenFluids = Set.of();
    }

    public static boolean isOxygen(FluidStack fluidStack) {
        return oxygenFluids.contains(fluidStack.getFluid());
    }

    public static Optional<Fluid> getOxygenFluid() {
        return oxygenFluids.stream().findFirst();
    }
}
