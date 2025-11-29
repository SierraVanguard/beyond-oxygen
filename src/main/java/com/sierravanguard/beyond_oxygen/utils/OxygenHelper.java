package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.registry.BODimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class OxygenHelper {

    public static boolean isInBreathableEnvironment(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return true;
        if (entity.isUnderWater()) return false;
        if (!BODimensions.isUnbreathable(serverLevel)) return true;
        BlockPos pos = entity.blockPosition();
        return isBlockPosInsideBreathableArea(serverLevel, pos);
    }
    public static boolean isBlockPosInsideBreathableArea(ServerLevel level, BlockPos pos) {
        for (HermeticArea area : HermeticAreaData.get(level).getAreasAffecting(pos)) {
            if (area.isHermetic() && area.contains(pos)) {
                if (area.hasAir()) return true;
            }
        }
        return false;
    }
}
