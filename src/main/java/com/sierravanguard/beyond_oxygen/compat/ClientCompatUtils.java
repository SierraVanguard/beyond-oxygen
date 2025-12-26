package com.sierravanguard.beyond_oxygen.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sierravanguard.beyond_oxygen.compat.valkyrienskies.VSClientCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ClientCompatUtils {
    public static void applyPoseTransforms(PoseStack poseStack, Level level, BlockPos blockPos) {
        if (CompatLoader.VALKYRIEN_SKIES.isLoaded()) {
            VSClientCompat.applyPoseTransforms(poseStack, level, blockPos);
        }
    }
}
