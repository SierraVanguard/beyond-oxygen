package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.registry.BODamageSources;
import com.sierravanguard.beyond_oxygen.registry.BODimensions;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.logging.Level;

@Mixin(ServerPlayer.class)
public abstract class BOServerPlayerMixin {
    private static final String COLD_SWEAT_MODID = "cold_sweat";

    @Shadow public abstract ServerLevel serverLevel();
    @Shadow public abstract boolean hurt(DamageSource source, float amount);

    @Unique
    private int beyond_oxygen$vacuumDamageCooldown = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (beyond_oxygen$vacuumDamageCooldown > 0) {
            beyond_oxygen$vacuumDamageCooldown--;
        }
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = this.serverLevel();
        var hermeticArea = VSCompat.getHermeticAreaContaining(player);
        if (!player.hasEffect(BOEffects.OXYGEN_SATURATION.get())) {
                if (BODimensions.isUnbreathable(level)
                    && !OxygenHelper.isInBreathableEnvironment(player)
                    && !player.isUnderWater()) {
                if (beyond_oxygen$vacuumDamageCooldown <= 0) {
                    applyDamageWithMessage(player, BODamageSources.vacuum(), 5f);
                    beyond_oxygen$vacuumDamageCooldown = 20;
                }
                beyond_oxygen$vacuumDamageCooldown--;
            }
        }

        if (!ModList.get().isLoaded(COLD_SWEAT_MODID)) {
            var area = VSCompat.getHermeticAreaContaining(player);
            boolean blockedByThermalController = area != null && area.hasActiveTemperatureRegulator();
            if (BODimensions.isHot(level)
                    && !SpaceSuitHandler.isWearingFullThermalSuit(player)
                    && !blockedByThermalController) {
                applyDamageWithMessage(player, BODamageSources.burn(), 5f);
            }
            if (BODimensions.isCold(level)
                    && !SpaceSuitHandler.isWearingFullCryoSuit(player)
                    && !blockedByThermalController) {
                applyDamageWithMessage(player, BODamageSources.freeze(), 5f);
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
