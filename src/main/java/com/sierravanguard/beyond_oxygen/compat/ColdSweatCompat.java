package com.sierravanguard.beyond_oxygen.compat;

import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Trait;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;
@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ColdSweatCompat
{
    private static final String MODID = "cold_sweat";
    private static final ResourceLocation MODIFIER_ID = new ResourceLocation("beyond_oxygen", "comfort_adjust");

    public static void init()
    {
        if (ModList.get().isLoaded(MODID))
        {
            MinecraftForge.EVENT_BUS.register(ColdSweatCompat.class);
        }
    }

    public static void setComfortableTemp(LivingEntity entity)
    {
        if (ModList.get().isLoaded(MODID)
                && !Temperature.hasModifier(entity, Trait.WORLD, ComfortTempModifier.class)) {
            Temperature.addModifier(
                    entity,
                    new ComfortTempModifier().expires(10),
                    Trait.WORLD,
                    Placement.Duplicates.BY_CLASS
            );
        }
    }

    @SubscribeEvent
    public static void onModifiersRegister(TempModifierRegisterEvent event)
    {
        event.register(MODIFIER_ID, ComfortTempModifier::new);
        System.out.println("Cold Sweat loaded- Registered comfy modifier!");
    }

    public static class ComfortTempModifier extends TempModifier
    {
        @Override
        public Function<Double, Double> calculate(LivingEntity entity, Trait trait)
        {
            return currentTemp -> currentTemp + (1.0 - currentTemp); // move halfway to 1.0
        }
    }
}
