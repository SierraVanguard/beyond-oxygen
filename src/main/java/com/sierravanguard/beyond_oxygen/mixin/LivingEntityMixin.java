package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.extensions.ILivingEntityExtension;
import com.sierravanguard.beyond_oxygen.registry.BODamageSources;
import com.sierravanguard.beyond_oxygen.registry.BODimensions;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.tags.BOEntityTypeTags;
import com.sierravanguard.beyond_oxygen.utils.HermeticAreaManager;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.OxygenManager;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingEntityExtension {
    private static final String COLD_SWEAT_MODID = "cold_sweat";
    @Unique
    private int beyond_oxygen$vacuumDamageCooldown = 0;

    private LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void beyond_oxygen$tick() {
        if (level().isClientSide()) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        OxygenManager.consumeOxygen(entity);
        if (beyond_oxygen$vacuumDamageCooldown > 0) {
            beyond_oxygen$vacuumDamageCooldown--;
        }
        var hermeticArea = HermeticAreaManager.getHermeticAreaContaining(entity);
        if (!entity.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
            if (BODimensions.isUnbreathable(level())
                    && !entity.getType().is(BOEntityTypeTags.SURVIVES_VACUUM)
                    && !OxygenHelper.isInBreathableEnvironment(entity)
                    && !entity.isUnderWater()) {
                if (beyond_oxygen$vacuumDamageCooldown <= 0) {
                    applyDamageWithMessage(entity, BODamageSources.vacuum(), 5f);
                    beyond_oxygen$vacuumDamageCooldown = 20;
                }
                beyond_oxygen$vacuumDamageCooldown--;
            }
        }

        if (!ModList.get().isLoaded(COLD_SWEAT_MODID)) {
            var area = HermeticAreaManager.getHermeticAreaContaining(entity);
            boolean blockedByThermalController = area != null && area.hasActiveTemperatureRegulator();
            if (BODimensions.isHot(level())
                    && !entity.getType().is(BOEntityTypeTags.SURVIVES_HOT)
                    && !SpaceSuitHandler.isWearingFullThermalSuit(entity)
                    && !blockedByThermalController) {
                applyDamageWithMessage(entity, BODamageSources.burn(), 5f);
            }
            if (BODimensions.isCold(level())
                    && !entity.getType().is(BOEntityTypeTags.SURVIVES_COLD)
                    && !SpaceSuitHandler.isWearingFullCryoSuit(entity)
                    && !blockedByThermalController) {
                applyDamageWithMessage(entity, BODamageSources.freeze(), 5f);
            }
        }

        if (entity.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
    }


    private static void applyDamageWithMessage(LivingEntity player, DamageSource source, float amount) {
        if (player.level().isClientSide()) return;
        BODamageSources.applyCustomDamage(player, source, amount);
        DamageType type = source.getMsgId() != null ? source.type() : null;
        if (type != null) {
            player.getCombatTracker().recordDamage(source, amount);
        }
    }

    @Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void beyondoxygen$preventAirRefillInVacuum(int airIncrement, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        var area = HermeticAreaManager.getHermeticAreaContaining(self);
        if (area == null || area.hasAir() || self.hasEffect(BOEffects.OXYGEN_SATURATION.get())) return;
        BlockPos headPos = BlockPos.containing(self.getX(), self.getEyeY(), self.getZ());
        boolean headInWater = self.level().getFluidState(headPos).is(FluidTags.WATER);
        if (headInWater) {
            cir.setReturnValue(self.getAirSupply());
        }
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void beyondoxygen$decrementAirInVacuum(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        var area = HermeticAreaManager.getHermeticAreaContaining(self);
        if (area == null || area.hasAir() || self.hasEffect(BOEffects.OXYGEN_SATURATION.get())) return;
        int air = self.getAirSupply() - 1;
        self.setAirSupply(air);
        if (air == -20) {
            self.setAirSupply(0);
            self.hurt(self.damageSources().drown(), 2.0F);
        }
    }
}


