package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.compat.curios.CuriosCompat;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class CompatLoader
{
    private static final String COLD_SWEAT_MODID = "cold_sweat";
    private static final String AD_ASTRA_MODID = "ad_astra";
    private static final String CURIOS_MOD_ID = "curios";
    public static void init(ModLoadingContext context, IEventBus modEventBus)
    {
        if (ModList.get().isLoaded(COLD_SWEAT_MODID)) try
        {
            ColdSweatCompat.init();
        }
        catch (Throwable t)
        {
            BeyondOxygen.LOGGER.error("Failed to initialize Cold Sweat compat", t);
        }
        if (ModList.get().isLoaded(AD_ASTRA_MODID)) try
        {
            AdAstraCompat.init();
        }
        catch (Throwable t)
        {
            BeyondOxygen.LOGGER.error("Failed to initialize Ad Astra compat", t);
        }
        if (ModList.get().isLoaded(CURIOS_MOD_ID)) try
        {
            CuriosCompat.init(context, modEventBus);
        }
        catch (Throwable t)
        {
            BeyondOxygen.LOGGER.error("Failed to initialize Curios compat", t);
        }
    }
}
