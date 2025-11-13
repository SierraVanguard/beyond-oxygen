package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.registry.BODamageSources;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class BOServerPlayerMixin {
    private static final String COLD_SWEAT_MODID = "cold_sweat";

    @Shadow public abstract ServerLevel serverLevel();
    @Shadow public abstract boolean hurt(DamageSource source, float amount);

    @Unique
    private int beyond_oxygen$vacuumDamageCooldown = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = this.serverLevel();

        if (!player.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {

            // VACUUM DAMAGE
            if (BOConfig.unbreathableDimensions != null
                    && BOConfig.unbreathableDimensions.contains(level.dimension().location())
                    && !OxygenHelper.isInBreathableEnvironment(player)
                    && !player.isUnderWater()) {
                if (beyond_oxygen$vacuumDamageCooldown <= 0) {
                    applyDamageWithMessage(player, BODamageSources.VACUUM, 5f);
                    beyond_oxygen$vacuumDamageCooldown = 20;
                }
                beyond_oxygen$vacuumDamageCooldown--;
            }
        }

        if (!ModList.get().isLoaded(COLD_SWEAT_MODID)) {
            ResourceLocation dim = level.dimension().location();
            var area = VSCompat.getHermeticAreaContaining(player);
            boolean blockedByThermalController = area != null && area.hasActiveTemperatureRegulator();

            if (BOConfig.hotDimensions != null
                    && BOConfig.hotDimensions.contains(dim)
                    && !SpaceSuitHandler.isWearingFullThermalSuit(player)
                    && !blockedByThermalController) {
                applyDamageWithMessage(player, BODamageSources.BURN, 5f);
            }

            if (BOConfig.coldDimensions != null
                    && BOConfig.coldDimensions.contains(dim)
                    && !SpaceSuitHandler.isWearingFullCryoSuit(player)
                    && !blockedByThermalController) {
                applyDamageWithMessage(player, BODamageSources.FREEZE, 5f);
            }
        }

        if (player.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
            player.setAirSupply(player.getMaxAirSupply());
        }
    }


    @Unique
    private void applyDamageWithMessage(ServerPlayer player, DamageSource source, float amount) {
        if (player.level().isClientSide()) return;
        BODamageSources.applyCustomDamage(player, source, amount);
        DamageType type = source.getMsgId() != null ? source.type() : null;
        if (type != null) {
            player.getCombatTracker().recordDamage(source, amount);
        }
    }
}
