package com.sierravanguard.beyond_oxygen.compat.valkyrienskies;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class VSClientCompat {
    public static Ship getClientShipById(long shipId, Level level) {
        try {
            var ships = VSGameUtilsKt.getShipObjectWorld(level).getAllShips();
            if (ships == null) return null;
            return ships.getById(shipId);
        } catch (Exception e) {
            System.out.printf("[HermeticAreaClientManager] Exception for shipId %d: %s%n", shipId, e);
            return null;
        }
    }

    public static void applyPoseTransforms(PoseStack poseStack, Level level, BlockPos blockPos) {
        if (!level.isClientSide) return;
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
        if (ship != null) {
            Matrix4dc shipToWorld = ship.getTransform().getShipToWorld();
            Matrix4f shipToWorldMojang = new Matrix4f();
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    shipToWorldMojang.set(row, col, (float) shipToWorld.get(row, col));
                }
            }
            poseStack.last().pose().mul(shipToWorldMojang);
        }
    }
}
