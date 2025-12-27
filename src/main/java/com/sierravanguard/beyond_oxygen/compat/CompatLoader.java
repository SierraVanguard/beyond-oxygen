package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.compat.curios.CuriosCompat;
import com.sierravanguard.beyond_oxygen.compat.valkyrienskies.VSCompat;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.function.BiConsumer;

public enum CompatLoader {
    VALKYRIEN_SKIES("valkyrienskies", "Valkyrien Skies", VSCompat::init),
    COLD_SWEAT("cold_sweat", "Cold Sweat", ColdSweatCompat::init),
    AD_ASTRA("ad_astra", "Ad Astra", AdAstraCompat::init),
    CURIOS("curios", "Curios", CuriosCompat::init);

    public static void init(ModLoadingContext context, IEventBus modEventBus) {
        for (CompatLoader module : values()) module.initModule(context, modEventBus);
    }

    public final String modId, name;
    private final BiConsumer<ModLoadingContext, IEventBus> init;
    private boolean loaded = false;

    CompatLoader(String modId, String name, BiConsumer<ModLoadingContext, IEventBus> init) {
        this.modId = modId;
        this.name = name;
        this.init = init;
    }

    CompatLoader(String modId, String name, Runnable init) {
        this(modId, name, (context, modEventBus) -> init.run());
    }

    CompatLoader(String modId, String name) {
        this(modId, name, (context, modEventBus) -> {});
    }

    private void initModule(ModLoadingContext context, IEventBus modEventBus) {
        if (ModList.get().isLoaded(modId)) {
            BeyondOxygen.LOGGER.info("{} detected, attempting to load compat", name);
            try {
                init.accept(context, modEventBus);
                loaded = true;
                BeyondOxygen.LOGGER.info("{} compat loaded!", name);
            } catch (Throwable t) {
                BeyondOxygen.LOGGER.error("Failed to load {} compat", name, t);
            }
        }
    }

    public boolean isLoaded() {
        return loaded;
    }
}
