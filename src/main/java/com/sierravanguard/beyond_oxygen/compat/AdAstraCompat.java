package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AdAstraCompat {

    public static void init() {
        AdAstraEvents.EntityOxygenEvent.register(AdAstraCompat::onEntityOxygenCheck);
    }

    private static boolean onEntityOxygenCheck(Entity entity, boolean hasOxygen) {
        if (!(entity instanceof LivingEntity living)) {
            return hasOxygen;
        }
        if (living.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
            return true;
        }

        return hasOxygen;
    }
}
