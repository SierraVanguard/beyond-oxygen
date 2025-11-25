package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.tags.BODimensionTags;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class BODimensions {
    private static Set<ResourceKey<Level>> unbreathable = Set.of();
    private static Set<ResourceKey<Level>> cold = Set.of();
    private static Set<ResourceKey<Level>> hot = Set.of();

    public static void populateDimensions(RegistryAccess registryAccess) {
        HolderLookup.RegistryLookup<Level> lookup = registryAccess.lookupOrThrow(Registries.DIMENSION);
        unbreathable = lookup.get(BODimensionTags.UNBREATHABLE).map(BODimensions::toSet).orElseGet(Set::of);
        StringJoiner joiner = new StringJoiner(", ", "Unbreathable dimensions: ", "");
        for (ResourceKey<Level> key : unbreathable) joiner.add(key.location().toString());
        BeyondOxygen.LOGGER.info(joiner.toString());
        cold = lookup.get(BODimensionTags.COLD).map(BODimensions::toSet).orElseGet(Set::of);
        joiner = new StringJoiner(", ", "Cold dimensions: ", "");
        for (ResourceKey<Level> key : cold) joiner.add(key.location().toString());
        BeyondOxygen.LOGGER.info(joiner.toString());
        hot = lookup.get(BODimensionTags.HOT).map(BODimensions::toSet).orElseGet(Set::of);
        joiner = new StringJoiner(", ", "Hot dimensions: ", "");
        for (ResourceKey<Level> key : hot) joiner.add(key.location().toString());
        BeyondOxygen.LOGGER.info(joiner.toString());
    }

    private static Set<ResourceKey<Level>> toSet(HolderSet<Level> holderSet) {
        return holderSet.stream().map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    public static void releaseDimensions() {
        unbreathable = Set.of();
        cold = Set.of();
        hot = Set.of();
    }

    public static boolean isUnbreathable(Level level) {
        return unbreathable.contains(level.dimension());
    }

    public static boolean isCold(Level level) {
        return cold.contains(level.dimension());
    }

    public static boolean isHot(Level level) {
        return hot.contains(level.dimension());
    }
}