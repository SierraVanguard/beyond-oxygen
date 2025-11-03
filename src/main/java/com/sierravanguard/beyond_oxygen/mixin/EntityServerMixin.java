package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Entity.class)
public abstract class EntityServerMixin {
    @Shadow
    private Level level;
    @Unique
    private boolean neo$isInSealedArea = false;

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void onBaseTick(CallbackInfo ci) {
        if ((Object) this instanceof Player player && !level.isClientSide) {
            neo$isInSealedArea = VSCompat.playersInSealedShips.containsKey(player);
            NetworkHandler.sendSealedAreaStatusToClient(player, neo$isInSealedArea);
        }
    }
}
