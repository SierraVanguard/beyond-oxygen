package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.compat.curios.CuriosCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

public class CompatLoader
{
    private static final String COLD_SWEAT_MODID = "cold_sweat";
    private static final String AD_ASTRA_MODID = "ad_astra";
    public static void init(ModLoadingContext context, IEventBus modEventBus)
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
        try
        {
            CuriosCompat.init(context, modEventBus);
        }
        catch (Throwable t)
        {
            BeyondOxygen.LOGGER.error("Failed to initialize Curios compat", t);
        }
    }

    public static void setComfortableTemperature(LivingEntity entity)
    {
        ColdSweatCompat.setComfortableTemp(entity);
    }
}
