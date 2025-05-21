package com.kito1z.not_enough_oxygen.utils;

import com.kito1z.not_enough_oxygen.blocks.entity.VentBlockEntity;
import com.kito1z.not_enough_oxygen.utils.VentTracker;
import com.kito1z.not_enough_oxygen.NEOConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OxygenHelper {

    public static boolean isInBreathableEnvironment(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return true;

        // Still need oxygen if underwater
        if (player.isUnderWater()) {
            return false;
        }

        // Dimension is marked as breathable → oxygen not required
        if (!NEOConfig.unbreathableDimensions.contains(serverLevel.dimension().location())) {
            return true;
        }

        // Inside a sealed, vented area
        return isPlayerInSealedVentArea(serverLevel, player.blockPosition());
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
}
