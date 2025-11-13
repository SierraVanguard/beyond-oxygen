package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public class AdAstraCompat {

    private static boolean initialized = false;

    public static void init() {
        if (!ModList.get().isLoaded("adastra")) return;
        try {
            Class<?> entityOxygenEventClass = Class.forName("earth.terrarium.adastra.api.events.AdAstraEvents$EntityOxygenEvent");
            Method registerMethod = entityOxygenEventClass.getMethod("register", java.util.function.BiFunction.class);
            registerMethod.invoke(null, (java.util.function.BiFunction<Entity, Boolean, Boolean>) AdAstraCompat::onEntityOxygenCheck);

            initialized = true;
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Boolean onEntityOxygenCheck(Entity entity, Boolean hasOxygen) {
        if (!(entity instanceof LivingEntity living)) {
            return hasOxygen;
        }
        if (living.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
            return true;
        }
        return hasOxygen;
    }
}
