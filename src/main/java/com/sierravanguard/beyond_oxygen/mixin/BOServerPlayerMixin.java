package com.sierravanguard.beyond_oxygen.mixin;


import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class BOServerPlayerMixin {
    @Shadow public abstract ServerLevel serverLevel();

    @Shadow public abstract boolean hurt(DamageSource p_9037_, float p_9038_);

    @Unique
    int beyond_oxygen$vacuumDamageTicks = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = this.serverLevel();
        if (BOConfig.unbreathableDimensions.contains(level.dimension().location()) &&
                !player.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
            if (beyond_oxygen$vacuumDamageTicks <= 0) {
                hurt(player.damageSources().dryOut(), 2f);
                beyond_oxygen$vacuumDamageTicks = 20;
            }
            beyond_oxygen$vacuumDamageTicks--;
        }
        if (player.hasEffect(BOEffects.OXYGEN_SATURATION.get())){
            player.setAirSupply(player.getMaxAirSupply());
        }
    }

}
