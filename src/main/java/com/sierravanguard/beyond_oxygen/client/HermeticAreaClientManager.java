package com.sierravanguard.beyond_oxygen.client;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HermeticAreaClientManager {

 
    private static final Map<Long, Set<Vec3>> hermeticBlocksPerArea = new HashMap<>();
 
    private static final Map<AABB, Long> aabbToAreaId = new HashMap<>();
 
    private static final Map<AABB, Long> aabbToShipId = new HashMap<>();

    public static void clear() {
        hermeticBlocksPerArea.clear();
        aabbToAreaId.clear();
        aabbToShipId.clear();
    }

    public static void clearArea(long areaId) {
        hermeticBlocksPerArea.remove(areaId);
        aabbToAreaId.entrySet().removeIf(e -> e.getValue().equals(areaId));
        aabbToShipId.entrySet().removeIf(e -> e.getValue().equals(areaId));
    }

    public static void registerHermeticBlocks(long areaId, Collection<Vec3> blocks, Long shipId) {
        hermeticBlocksPerArea.put(areaId, new HashSet<>(blocks));

        aabbToAreaId.entrySet().removeIf(e -> e.getValue() == areaId);
        aabbToShipId.entrySet().removeIf(e -> e.getValue() == areaId);

        for (Vec3 block : blocks) {
            AABB aabb = new AABB(block.x, block.y, block.z, block.x + 1, block.y + 1, block.z + 1);
            aabbToAreaId.put(aabb, areaId);
            if (shipId != null) {
                aabbToShipId.put(aabb, shipId);
            }
        }
    }

    public static List<AABB> getAll(Level level) {
        return new ArrayList<>(aabbToAreaId.keySet());
    }

    public static Long getShipIdForAABB(AABB aabb) {
        return aabbToShipId.get(aabb);
    }
}
