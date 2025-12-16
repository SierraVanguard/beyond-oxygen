package com.sierravanguard.beyond_oxygen.compat;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.compat.valkyrienskies.VSCompat;
import com.sierravanguard.beyond_oxygen.utils.CryoBedManager;
import com.sierravanguard.beyond_oxygen.utils.HermeticArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;

import java.util.Map;
import java.util.function.Consumer;

public class CompatUtils {
    public static void setComfortableTemperature(LivingEntity entity) {
        ColdSweatCompat.setComfortableTemp(entity);
    }

    public static BlockPos getAreaBlockPos(Vec3 pos, HermeticArea area) {
        return BlockPos.containing(getAreaPos(pos, area));
    }

    public static Vec3 getAreaPos(Vec3 pos, HermeticArea area) {
        if (BeyondOxygen.ModsLoaded.VS) {
            return VSCompat.getAreaPos(pos, area);
        }
        return pos;
    }

    public static long getShipId(ServerLevel serverLevel, BlockPos blockPos) {
        if (BeyondOxygen.ModsLoaded.VS) {
            return VSCompat.getShipId(serverLevel, blockPos);
        }
        return -1L;
    }

    public static Vec3 getCenter(Level level, BlockPos blockPos) {
        if (BeyondOxygen.ModsLoaded.VS) {
            return VSCompat.getCenter(level, blockPos);
        }
        return Vec3.atCenterOf(blockPos);
    }

    public static boolean isWithinShipRadius(Level level, LivingEntity entity, BlockPos origin, double radius) {
        Vec3 localPos = getCenter(level, origin);
        return localPos.distanceToSqr(entity.getEyePosition()) <= radius * radius;
    }

    public static void applyBouyancy(ServerLevel level, Map<Long, Double> volumePerShip) {
        if (BeyondOxygen.ModsLoaded.VS) VSCompat.applyBouyancy(level, volumePerShip);
    }

    public static void updateCryoBedReference(ServerLevel serverLevel, CryoBedManager.CryoBedReference reference, Consumer<CryoBedManager.CryoBedReference> setUpdatedReference) {
        if (BeyondOxygen.ModsLoaded.VS) {
            VSCompat.updateCryoBedReference(serverLevel, reference, setUpdatedReference);
        } else {
            if (reference.shipId() != null) {
                CryoBedManager.CryoBedReference updatedRef = new CryoBedManager.CryoBedReference(
                        reference.dimension(),
                        reference.worldPos(),
                        null,
                        null
                );
                setUpdatedReference.accept(updatedRef);
            }
        }
    }

    public static Pair<Long, Vector3d> getCryoBedShipAndPosition(ServerLevel level, BlockPos pos) {
        if (BeyondOxygen.ModsLoaded.VS) return VSCompat.getCryoBedShipAndPosition(level, pos);
        else return Pair.of(null, null);
    }

    public static BlockPos getCryoBedRespawnPosition(ServerLevel level, CryoBedManager.CryoBedReference cryoBed) {
        if (BeyondOxygen.ModsLoaded.VS) return VSCompat.getCryoBedRespawnPosition(level, cryoBed);
        return cryoBed.worldPos();
    }
}
