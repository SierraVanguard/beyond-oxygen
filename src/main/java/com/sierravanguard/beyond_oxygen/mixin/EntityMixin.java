package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.client.ClientSealedAreaState;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private Level level;

    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Shadow public abstract void setSwimming(boolean swimming);
    @Shadow protected boolean wasEyeInWater;
    @Shadow protected boolean wasTouchingWater;
    @Shadow @Final private Set<TagKey<Fluid>> fluidOnEyes;
    @Shadow(remap = false) private FluidType forgeFluidTypeOnEyes;
    @Shadow public abstract boolean isInWater();

    @Unique
    private static EntityPolygonCollider beyond_oxygen$collider = null;

    @Unique
    private boolean neo$isInSealedArea = false;

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void onBaseTick(CallbackInfo ci) {
        if ((Object) this instanceof Player player && !level.isClientSide) {
            neo$isInSealedArea = VSCompat.playersInSealedAreas.containsKey(player);
            NetworkHandler.sendSealedAreaStatusToClient(player, neo$isInSealedArea);
        }
    }
    @Unique
    private boolean beyond_oxygen$isInSealedArea() {
        if (level.isClientSide && (Object) this instanceof Player player) {
            return ClientSealedAreaState.isInSealedArea(player.getUUID());
        }
        return neo$isInSealedArea;
    }
    @WrapMethod(method = "updateFluidHeightAndDoFluidPushing()V", remap = false)
    private void onUpdateFluidHeightAndDoFluidPushing(Operation<Void> original) {
        if (beyond_oxygen$isInSealedArea()) {
            this.wasTouchingWater = false;
            return;
        }
        original.call();
    }

    @WrapMethod(method = "updateFluidOnEyes")
    private void onUpdateFluidOnEyes(Operation<Void> original) {
        if (beyond_oxygen$isInSealedArea()) {
            this.wasEyeInWater = false;
            this.fluidOnEyes.clear();
            this.forgeFluidTypeOnEyes = ForgeMod.EMPTY_TYPE.get();
            return;
        }
        original.call();
    }

    @WrapMethod(method = "updateSwimming")
    private void onUpdateSwimming(Operation<Void> original) {
        if (beyond_oxygen$isInSealedArea()) {
            this.setSwimming(false);
            return;
        }
        original.call();
    }

    @WrapMethod(method = "isInBubbleColumn")
    private boolean onIsInBubbleColumn(Operation<Boolean> original) {
        if (beyond_oxygen$isInSealedArea()) return false;
        return original.call();
    }

    @Unique
    private static EntityPolygonCollider getCollider() {
        if (beyond_oxygen$collider == null) {
            beyond_oxygen$collider = ValkyrienSkiesMod.vsCore.getEntityPolygonCollider();
        }
        return beyond_oxygen$collider;
    }
}
