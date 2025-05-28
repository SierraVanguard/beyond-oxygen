package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.BOConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class OxygenHelper {

    public static boolean isInBreathableEnvironment(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return true;

        if (player.isUnderWater()) {
            return false;
        }

        if (!BOConfig.unbreathableDimensions.contains(serverLevel.dimension().location())) {
            return true;
        }

        BlockPos playerPos = player.blockPosition();

        return isPlayerInSealedVentArea(serverLevel, playerPos)
                || isPlayerInBubbleArea(serverLevel, playerPos);
    }

    private static boolean isPlayerInSealedVentArea(ServerLevel level, BlockPos playerPos) {
        for (BlockPos ventPos : VentTracker.getVentsIn(level)) {
            BlockEntity be = level.getBlockEntity(ventPos);
            if (!(be instanceof VentBlockEntity vent)) continue;

            if (vent.hermeticArea != null && vent.hermeticArea.isHermetic()) {
                if (vent.hermeticArea.getArea().contains(playerPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPlayerInBubbleArea(ServerLevel level, BlockPos playerPos) {
        Vec3 playerVec = Vec3.atCenterOf(playerPos);
        for (BlockPos bubblePos : BubbleGeneratorTracker.getGeneratorsIn(level)) {
            BlockEntity be = level.getBlockEntity(bubblePos);
            if (!(be instanceof BubbleGeneratorBlockEntity bubble)) continue;

            float radius = bubble.getCurrentRadius();
            if (radius <= 0) continue;

            Vec3 bubbleCenter = Vec3.atCenterOf(bubblePos);
            double distance = bubbleCenter.distanceTo(playerVec);
            if (distance <= radius * 2) {
                return true;
            }
        }
        return false;
    }
    public static boolean isBlockPosInsideBreathableArea(ServerLevel level, BlockPos pos) {
        for (BlockPos ventPos : VentTracker.getVentsIn(level)) {
            BlockEntity be = level.getBlockEntity(ventPos);
            if (!(be instanceof VentBlockEntity vent)) continue;

            if (vent.hermeticArea != null && vent.hermeticArea.isHermetic()) {
                if (vent.hermeticArea.getArea().contains(pos)) {
                    return true;
                }
            }
        }
        Vec3 posVec = Vec3.atCenterOf(pos);
        for (BlockPos bubblePos : BubbleGeneratorTracker.getGeneratorsIn(level)) {
            BlockEntity be = level.getBlockEntity(bubblePos);
            if (!(be instanceof BubbleGeneratorBlockEntity bubble)) continue;

            float radius = bubble.getCurrentRadius();
            if (radius <= 0) continue;

            Vec3 bubbleCenter = Vec3.atCenterOf(bubblePos);
            double distance = bubbleCenter.distanceTo(posVec);
            if (distance <= radius * 2) {
                return true;
            }
        }

        return false;
    }

}
