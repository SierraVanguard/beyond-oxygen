package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAirMixin {

    @Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void beyondoxygen$preventAirRefillInVacuum(int airIncrement, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) return;
        var area = VSCompat.getHermeticAreaContaining(player);
        if (area == null || area.hasAir() || player.hasEffect(BOEffects.OXYGEN_SATURATION.get())) return;
        BlockPos headPos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        boolean headInWater = player.level().getFluidState(headPos).is(FluidTags.WATER);
        if (headInWater) {
            cir.setReturnValue(player.getAirSupply());
        }
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void beyondoxygen$decrementAirInVacuum(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer player)) return;
        var area = VSCompat.getHermeticAreaContaining(player);
        if (area == null || area.hasAir() || player.hasEffect(BOEffects.OXYGEN_SATURATION.get())) return;
        int air = player.getAirSupply() - 1;
        player.setAirSupply(air);
        if (air == -20) {
            player.setAirSupply(0);
            player.hurt(player.damageSources().drown(), 2.0F);
        }
    }
}


