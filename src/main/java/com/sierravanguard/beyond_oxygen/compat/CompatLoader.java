package com.sierravanguard.beyond_oxygen.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

public class CompatLoader
{
    private static final String COLD_SWEAT_MODID = "cold_sweat";

    public static void init()
    {
        if (ModList.get().isLoaded(COLD_SWEAT_MODID))
        {
            try
            {
                ColdSweatCompat.init();
            }
            catch (Throwable t)
            {
                System.err.println("[Beyond Oxygen] Failed to initialize Cold Sweat compat: " + t.getMessage());
            }
        }
    }

    public static void setComfortableTemperature(LivingEntity entity)
    {
        if (ModList.get().isLoaded(COLD_SWEAT_MODID))
        {
            try
            {
                ColdSweatCompat.setComfortableTemp(entity);
            }
            catch (Throwable t)
            {
                System.err.println("[Beyond Oxygen] Failed to set comfortable temperature: " + t.getMessage());
            }
        }
    }
}
