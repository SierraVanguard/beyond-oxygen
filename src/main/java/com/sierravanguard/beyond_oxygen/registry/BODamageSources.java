package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;

public class BODamageSources {
    private static final ResourceKey<DamageType> VACUUM_KEY = key("vacuum");
    private static final ResourceKey<DamageType> FREEZE_KEY = key("freeze");
    private static final ResourceKey<DamageType> BURN_KEY = key("burn");
    private static final ResourceKey<DamageType> HURT_KEY = key("vacuum");

    private static ResourceKey<DamageType> key(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(BeyondOxygen.MODID, id));
    }

    private static DamageSource vacuum, freeze, burn, hurt;

    public static DamageSource vacuum() {
        return vacuum;
    }

    public static DamageSource freeze() {
        return freeze;
    }

    public static DamageSource burn() {
        return burn;
    }

    public static DamageSource hurt() {
        return hurt;
    }

    public static void populateSources(RegistryAccess registryAccess) {
        Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        vacuum = new DamageSource(registry.getHolderOrThrow(VACUUM_KEY));
        freeze = new DamageSource(registry.getHolderOrThrow(FREEZE_KEY));
        burn = new DamageSource(registry.getHolderOrThrow(BURN_KEY));
        hurt = new DamageSource(registry.getHolderOrThrow(HURT_KEY));
    }

    public static void releaseSources() {
        vacuum = null;
        freeze = null;
        burn = null;
        hurt = null;
    }

    public static void applyCustomDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!entity.level().isClientSide) {
            entity.hurt(source, amount);
            if (source == burn) {
                entity.setSecondsOnFire(2);
            } else if (source == freeze) {
                int freezeTicks = entity.getTicksFrozen() + 5;
                if (freezeTicks > 100) freezeTicks = 100;
                entity.setTicksFrozen(freezeTicks);
            }
        }
    }
}