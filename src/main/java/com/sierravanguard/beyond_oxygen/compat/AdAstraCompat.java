package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

public class AdAstraCompat {
    public static void init() {
        AdAstraEvents.EntityOxygenEvent.register(AdAstraCompat::onEntityOxygenCheck);
        BeyondOxygen.LOGGER.info("Beyond Oxygen Ad Astra compatibility loaded");
    }

    private static Boolean onEntityOxygenCheck(Entity entity, Boolean hasOxygen) {
        return hasOxygen || (entity instanceof LivingEntity living && living.hasEffect(BOEffects.OXYGEN_SATURATION.get()));
    }
}
