package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.network.SyncHermeticBlocksS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.Ship;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HermeticArea {

    public static final int limit = BOConfig.ventRange;

    private final Set<BlockPos> area = new HashSet<>();
    private boolean hermetic = false;

    public HermeticArea() {}

    public boolean bakeArea(ServerLevel level, BlockPos start, Direction startDir) {
        area.clear();

        if (HermeticUtils.isHermetic(level, start, startDir)) {
            hermetic = false;
            return false;
        }

        area.add(start);
        List<AirBlockData> oldLayer = new ArrayList<>();
        oldLayer.add(new AirBlockData(start).setSource(startDir));

        while (area.size() < limit && !oldLayer.isEmpty()) {
            List<AirBlockData> temp = new ArrayList<>();
            for (AirBlockData blockData : oldLayer) {
                if (area.size() >= limit) break;
                bakeNeighbors(level, blockData, temp);
            }
            oldLayer = temp;
        }

        hermetic = oldLayer.isEmpty();
        if (hermetic && !area.isEmpty()) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, start);
            long shipId = (ship != null) ? ship.getId() : -1L;

            Set<Vec3> blocks = new HashSet<>();
            for (BlockPos p : area) {
                if (ship != null) {
                    blocks.add(new Vec3(p.getX(), p.getY(), p.getZ()));
                } else {
                    blocks.add(new Vec3(p.getX(), p.getY(), p.getZ()));
                }

            }
            //for future optimizations, shipID stays. For now, the method is as precise as an axe, forcing recalculation of ALL hermetic areas.
            NetworkHandler.sendInvalidateHermeticAreas(shipId, true);
            NetworkHandler.sendToAllPlayers(new SyncHermeticBlocksS2CPacket(shipId, blocks));
        }


        return hermetic;
    }

    public void bakeNeighbors(ServerLevel level, AirBlockData pos, @Nullable List<AirBlockData> temp) {
        for (Direction dir : Direction.values()) {
            if (pos.isSource(dir)) continue;
            if (area.size() >= limit) return;

            BlockPos neighbor = pos.relative(dir);
            if (!area.contains(neighbor)
                    && !HermeticUtils.isHermetic(level, neighbor, dir.getOpposite())
                    && HermeticUtils.canFlowTrough(level, pos, pos.getSource(), dir)) {

                area.add(neighbor);
                if (temp != null) temp.add(new AirBlockData(neighbor).setSource(dir.getOpposite()));
            }
        }
    }

    public Set<BlockPos> getArea() {
        return area;
    }

    public boolean isHermetic() {
        return hermetic;
    }
}
