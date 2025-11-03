package com.sierravanguard.beyond_oxygen.mixin;

import com.sierravanguard.beyond_oxygen.client.ClientSealedAreaState;
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
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityClientMixin {
    @Shadow private Level level;
    @Shadow public abstract void setSwimming(boolean swimming);
    @Shadow protected boolean wasEyeInWater;
    @Shadow protected boolean wasTouchingWater;
    @Shadow @Final private Set<TagKey<Fluid>> fluidOnEyes;
    @Shadow(remap = false) private FluidType forgeFluidTypeOnEyes;

    @Unique
    private boolean beyond_oxygen$isInSealedArea() {
        if ((Object) this instanceof Player player) {
            return ClientSealedAreaState.isInSealedArea(player.getUUID());
        }
        return false;
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
}
