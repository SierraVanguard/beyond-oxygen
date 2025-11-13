package com.sierravanguard.beyond_oxygen.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;

public class BODamageSources {
    public static final DamageSource VACUUM = create("beyond_oxygen.vacuum", 0.0f, DamageEffects.DROWNING);
    public static final DamageSource FREEZE = create("beyond_oxygen.freeze", 0.5f, DamageEffects.FREEZING);
    public static final DamageSource BURN = create("beyond_oxygen.burn", 0.5f, DamageEffects.BURNING);
    public static final DamageSource HURT = create("beyond_oxygen.hurt", 0.0f, DamageEffects.HURT);

    private static DamageSource create(String msgId, float exhaustion, DamageEffects effects) {
        DamageType type = new DamageType(
                msgId,
                DamageScaling.NEVER,
                exhaustion,
                effects,
                DeathMessageType.DEFAULT
        );
        Holder<DamageType> holder = Holder.direct(type);
        return new DamageSource(holder);
    }

    public static void applyCustomDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!entity.level().isClientSide) {
            entity.hurt(source, amount);
            if (source == BURN) {
                entity.setSecondsOnFire(2);
            } else if (source == FREEZE) {
                int freezeTicks = entity.getTicksFrozen() + 5;
                entity.setTicksFrozen(freezeTicks);
            }
        }
    }
}