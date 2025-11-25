package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

public class CompatLoader
{
    private static final String COLD_SWEAT_MODID = "cold_sweat";
    private static final String AD_ASTRA_MODID = "ad_astra";
    public static void init()
    {
        try
        {
            ColdSweatCompat.init();
        }
        catch (Throwable t)
        {
            BeyondOxygen.LOGGER.error("Failed to initialize Cold Sweat compat", t);
        }
        try
        {
            AdAstraCompat.init();
        }
        catch (Throwable t)
        {
            BeyondOxygen.LOGGER.error("Failed to initialize Ad Astra compat", t);
        }
    }

    public static void setComfortableTemperature(LivingEntity entity)
    {
        ColdSweatCompat.setComfortableTemp(entity);
    }
}
