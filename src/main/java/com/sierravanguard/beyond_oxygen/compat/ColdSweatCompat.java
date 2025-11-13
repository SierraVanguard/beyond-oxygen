package com.sierravanguard.beyond_oxygen.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class ColdSweatCompat {
    private static final String MODID = "coldsweat";

    public static void init() {
        if (ModList.get().isLoaded(MODID)) {
            try {
                Class<?> tempModifierClass = Class.forName("com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier");
                Class<?> registerEventClass = Class.forName("com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent");
                MinecraftForge.EVENT_BUS.register(ColdSweatCompat.class);
            } catch (ClassNotFoundException e) {
                System.out.println("[BO-DEBUG] Cold Sweat API not found, skipping compatibility.");
            }
        }
    }

    public static void setComfortableTemp(LivingEntity entity) {
        if (!ModList.get().isLoaded(MODID)) return;
        try {
            Class<?> temperatureClass = Class.forName("com.momosoftworks.coldsweat.api.util.Temperature");
            Class<?> traitClass = Class.forName("com.momosoftworks.coldsweat.api.util.Temperature$Trait");
        } catch (ClassNotFoundException e) {
        }
    }
}
