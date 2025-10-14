package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BOConfig;
import net.minecraftforge.fml.ModList;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class AdAstraConfigHelper {
    private static final List<String> AD_ASTRA_PLANETS = List.of(
            "ad_astra:glacio",
            "ad_astra:mars",
            "ad_astra:mercury",
            "ad_astra:moon",
            "ad_astra:venus"
    );
    private static final ResourceLocation AD_ASTRA_OXYGEN = new ResourceLocation("ad_astra", "oxygen");

    public static void injectAdAstraDimensions() {
        if (!ModList.get().isLoaded("ad_astra")) return;

        List<ResourceLocation> dims = new ArrayList<>(BOConfig.unbreathableDimensions);

        for (String dim : AD_ASTRA_PLANETS) {
            ResourceLocation planet = ResourceLocation.parse(dim);
            ResourceLocation orbit = ResourceLocation.parse(dim + "_orbit");

            if (!dims.contains(planet)) dims.add(planet);
            if (!dims.contains(orbit)) dims.add(orbit);
        }
        ResourceLocation earthOrbit = ResourceLocation.parse("ad_astra:earth_orbit");
        if (!dims.contains(earthOrbit)) dims.add(earthOrbit);

        BOConfig.unbreathableDimensions = dims;
        List<ResourceLocation> fluids = new ArrayList<>(BOConfig.oxygenFluids);

        if (!fluids.contains(AD_ASTRA_OXYGEN)) {
            fluids.add(AD_ASTRA_OXYGEN);
        }
    }
}
