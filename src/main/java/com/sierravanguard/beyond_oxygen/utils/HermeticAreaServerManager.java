package com.sierravanguard.beyond_oxygen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class HermeticAreaServerManager {

    private static final Map<Long, List<HermeticArea>> areasPerShip = new HashMap<>();
    private static final List<HermeticArea> worldAreas = new ArrayList<>();

    public static void onBlockChanged(ServerLevel level, BlockPos pos) {
        long shipId = getShipId(level, pos);
        List<HermeticArea> list = (shipId == -1L) ? worldAreas : areasPerShip.get(shipId);
        if (list == null) return;

        for (HermeticArea area : list) {
            for (BlockPos boundary : area.getBoundaryBlocks()) {
                boolean isBoundary = boundary.equals(pos) || boundary.closerThan(pos, 1.5);
                boolean isInside = area.contains(pos);

                if (isBoundary || isInside) {
                    area.markDirty();
                    break;
                }
            }
        }
    }

    public static void tick(ServerLevel level) {
        for (Iterator<HermeticArea> it = worldAreas.iterator(); it.hasNext();) {
            HermeticArea area = it.next();
            if (area.isDirty() || !area.isHermetic()) {
                BlockPos start = area.getBlocks().isEmpty() ? null : area.getBlocks().iterator().next();
                if (start != null) area.bake(start);
            }
        }
        for (var entry : areasPerShip.entrySet()) {
            List<HermeticArea> list = entry.getValue();
            list.forEach(area -> {
                if (area.isDirty() || !area.isHermetic()) {
                    BlockPos start = area.getBlocks().isEmpty() ? null : area.getBlocks().iterator().next();
                    if (start != null) area.bake(start);
                }
            });
        }
    }

    public static void register(HermeticArea area) {
        long shipId = area.getShipId();
        if (shipId == -1L) {
            worldAreas.add(area);
        } else {
            areasPerShip.computeIfAbsent(shipId, k -> new ArrayList<>()).add(area);
        }
    }
    public static void unregister(HermeticArea area) {
        long shipId = area.getShipId();
        if (shipId == -1L) {
            worldAreas.remove(area);
        } else {
            List<HermeticArea> list = areasPerShip.get(shipId);
            if (list != null) {
                list.remove(area);
                if (list.isEmpty()) areasPerShip.remove(shipId);
            }
        }
    }

    public static void clearShip(long shipId) {
        areasPerShip.remove(shipId);
    }

    private static long getShipId(ServerLevel level, BlockPos pos) {
        var ship = org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos(level, pos);
        return (ship == null) ? -1L : ship.getId();
    }

}
