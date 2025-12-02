package com.sierravanguard.beyond_oxygen.compat.curios;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class CuriosCompat {
    private static final String CURIOS_MOD_ID = "curios";

    private static boolean initialized = false;

    public static void init(ModLoadingContext context, IEventBus modEventBus) {
        if (!ModList.get().isLoaded(CURIOS_MOD_ID)) return;
        BOCuriosOxygenSources.register(modEventBus);
        context.registerConfig(ModConfig.Type.SERVER, BOCuriosCompatServerConfig.SPEC, BeyondOxygen.MODID + "-curios_compat-server.toml");
        initialized = true;
        BeyondOxygen.LOGGER.info("Beyond Oxygen Curios compatibility loaded");
    }
}
