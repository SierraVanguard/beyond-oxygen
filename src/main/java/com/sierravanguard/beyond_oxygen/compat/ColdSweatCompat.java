package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class ColdSweatCompat {
    private static final String COLD_SWEAT_MODID = "cold_sweat";

    private static boolean initialized = false;

    public static void init() {
        if (!ModList.get().isLoaded(COLD_SWEAT_MODID)) return;
        try {
            Class<?> tempModifierClass = Class.forName("com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier");
            Class<?> registerEventClass = Class.forName("com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent");
            MinecraftForge.EVENT_BUS.register(ColdSweatCompat.class);
            initialized = true;
            BeyondOxygen.LOGGER.info("Beyond Oxygen Cold Sweat compatibility loaded");
        } catch (ClassNotFoundException e) {
            BeyondOxygen.LOGGER.warn("Cold Sweat API not found, skipping compatibility.");
        }
    }

    public static void setComfortableTemp(LivingEntity entity) {
        if (initialized) {
            try {
                Class<?> temperatureClass = Class.forName("com.momosoftworks.coldsweat.api.util.Temperature");
                Class<?> traitClass = Class.forName("com.momosoftworks.coldsweat.api.util.Temperature$Trait");
            } catch (ClassNotFoundException e) {
            }
        }
    }
}
