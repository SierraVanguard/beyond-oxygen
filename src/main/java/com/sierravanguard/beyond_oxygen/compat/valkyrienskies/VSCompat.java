package com.sierravanguard.beyond_oxygen.compat.valkyrienskies;

import com.sierravanguard.beyond_oxygen.utils.CryoBedManager;
import com.sierravanguard.beyond_oxygen.utils.HermeticArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import java.util.Map;
import java.util.function.Consumer;

public class VSCompat {
    public static void init() {
        ValkyrienSkiesMod.getApi().registerAttachment(BuoyancyForceInducer.class);
    }

    public static Vec3 getCenter(Level level, BlockPos blockPos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
        if (ship == null) return Vec3.atCenterOf(blockPos);
        else {
            Vector3d pos = ship.getTransform().getShipToWorld().transformPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, new Vector3d());
            return new Vec3(pos.x, pos.y, pos.z);
        }
    }

    public static ServerShip getShipAtPosition(ServerLevel level, BlockPos pos) {
        return VSGameUtilsKt.getShipManagingPos(level, pos);
    }

    public static ServerShip getShipById(ServerLevel targetLevel, long shipId) {
        QueryableShipData<Ship> shipData = VSGameUtilsKt.getAllShips(targetLevel);
        Ship ship = shipData.getById(shipId);
        if (ship instanceof ServerShip serverShip) return serverShip;
        return null;
    }

    public static long getShipId(ServerLevel serverLevel, BlockPos blockPos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);
        if (ship != null) return ship.getId();
        else return -1L;
    }

    public static Vec3 getAreaPos(Vec3 pos, HermeticArea area) {
        ServerShip areaShip = getShipById(area.getLevel(), area.getShipId());
        if (areaShip != null) {
            Vector3d shipLocal = areaShip.getTransform().getWorldToShip().transformPosition(pos.x, pos.y, pos.z, new Vector3d());
            return new Vec3(shipLocal.x, shipLocal.y, shipLocal.z);
        }
        return pos;
    }

    public static void applyBouyancy(ServerLevel level, Map<Long, Double> volumePerShip) {
        QueryableShipData<LoadedShip> shipData = VSGameUtilsKt.getShipWorldNullable(level).getLoadedShips();
        for (Map.Entry<Long, Double> entry : volumePerShip.entrySet()) {
            long shipId = entry.getKey();
            double totalVolume = entry.getValue();

            Ship ship = shipData.getById(entry.getKey());
            if (ship instanceof LoadedServerShip serverShip) {
                System.out.printf("Applying buoyant force to ship %d; Volume: %f\n", shipId, totalVolume);
                BuoyancyForceInducer.tickOnShip(serverShip, totalVolume);
            }
        }
    }

    public static void updateCryoBedReference(ServerLevel serverLevel, CryoBedManager.CryoBedReference reference, Consumer<CryoBedManager.CryoBedReference> setUpdatedReference) {
        ServerShip ship = getShipAtPosition(serverLevel, reference.worldPos());
        if (ship != null) {
            var shipTransform = ship.getTransform();

            Vector3d worldCenterPos = new Vector3d(
                    reference.worldPos().getX() + 0.5,
                    reference.worldPos().getY() + 1.0,
                    reference.worldPos().getZ() + 0.5
            );

            Vector3d shipLocalPos = shipTransform.getWorldToShip().transformPosition(worldCenterPos);

            CryoBedManager.CryoBedReference updatedRef = new CryoBedManager.CryoBedReference(
                    reference.dimension(),
                    reference.worldPos(),
                    ship.getId(),
                    shipLocalPos
            );

            setUpdatedReference.accept(updatedRef);
        } else if (reference.shipId() != null) {
            CryoBedManager.CryoBedReference updatedRef = new CryoBedManager.CryoBedReference(
                    reference.dimension(),
                    reference.worldPos(),
                    null,
                    null
            );
            setUpdatedReference.accept(updatedRef);
        }
    }

    public static Pair<Long, Vector3d> getCryoBedShipAndPosition(ServerLevel level, BlockPos pos) {
        ServerShip ship = getShipAtPosition(level, pos);
        if (ship != null) {
            var transform = ship.getTransform();
            Vector3d worldPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            Vector3d shipLocal = transform.getWorldToShip().transformPosition(worldPos);
            return Pair.of(ship.getId(), shipLocal);
        } else {
            return Pair.of(null, null);
        }
    }

    public static BlockPos getCryoBedRespawnPosition(ServerLevel level, CryoBedManager.CryoBedReference cryoBed) {
        if (cryoBed.shipId() != null && cryoBed.shipLocalPos() != null) {
            ServerShip ship = getShipById(level, cryoBed.shipId());
            if (ship != null) {
                var shipTransform = ship.getTransform();
                Vector3d worldPos = shipTransform.getShipToWorld().transformPosition(cryoBed.shipLocalPos());
                return BlockPos.containing(worldPos.x, worldPos.y, worldPos.z);
            }
        }
        return cryoBed.worldPos();
    }
}
