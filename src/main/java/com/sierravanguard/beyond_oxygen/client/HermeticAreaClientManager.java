package com.sierravanguard.beyond_oxygen.client;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

public class HermeticAreaClientManager {

    private static final Map<Long, Set<Vec3>> hermeticBlocksPerShip = new HashMap<>();
    private static final Set<Vec3> worldHermeticBlocks = new HashSet<>();
    private static final Map<AABB, Long> aabbToShipId = new HashMap<>();

    public static void clear() {
        hermeticBlocksPerShip.clear();
        worldHermeticBlocks.clear();
        aabbToShipId.clear();
    }

    public static void setHermeticBlocksForShip(long shipId, Collection<Vec3> blocks) {
        hermeticBlocksPerShip.put(shipId, new HashSet<>(blocks));
    }

    public static void setWorldHermeticBlocks(Collection<Vec3> blocks) {
        worldHermeticBlocks.clear();
        worldHermeticBlocks.addAll(blocks);
    }

    public static Long getShipIdForAABB(AABB aabb) {
        return aabbToShipId.get(aabb);
    }
    public static List<AABB> getAll(Level level) {
        aabbToShipId.clear();
        List<AABB> result = new ArrayList<>();
        for (Vec3 block : worldHermeticBlocks) {
            AABB aabb = new AABB(
                    block.x, block.y, block.z,
                    block.x + 1.0, block.y + 1.0, block.z + 1.0
            );
            result.add(aabb);
        }
        for (Map.Entry<Long, Set<Vec3>> entry : hermeticBlocksPerShip.entrySet()) {
            long shipId = entry.getKey();
            Set<Vec3> localBlocks = entry.getValue();

            for (Vec3 block : localBlocks) {
                AABB aabb = new AABB(
                        block.x, block.y, block.z,
                        block.x + 1.0, block.y + 1.0, block.z + 1.0
                );
                result.add(aabb);
                aabbToShipId.put(aabb, shipId);
            }
        }
        return result;
    }

    public static Ship getClientShipById(long shipId, Level level) {
        try {
            var shipData = VSGameUtilsKt.getShipObjectWorld(level).getAllShips();
            if (shipData == null) {
                return null;
            }
            Ship ship = shipData.getById(shipId);
            if (ship == null) {
            }
            return ship;
        } catch (Exception e) {
            System.out.printf("[HermeticAreaClientManager] getClientShipById: Exception for id %d -> %s%n", shipId, e);
            return null;
        }
    }
    public static Set<Vec3> getBlocksForShip(long shipId) {
        Set<Vec3> s = hermeticBlocksPerShip.get(shipId);
        return (s == null) ? Collections.emptySet() : Collections.unmodifiableSet(s);
    }
    public static Set<Vec3> getWorldHermeticBlocks() {
        return Collections.unmodifiableSet(worldHermeticBlocks);
    }
    private static List<AABB> mergeBlocks(Collection<?> positions) {
        List<AABB> aabbs = new ArrayList<>();
        if (positions.isEmpty()) return aabbs;

        Set<Vector3d> remaining = new HashSet<>();
        for (Object pos : positions) {
            if (pos instanceof Vec3 vec) remaining.add(new Vector3d(vec.x, vec.y, vec.z));
            else if (pos instanceof Vector3d vec3d) remaining.add(vec3d);
        }
        while (!remaining.isEmpty()) {
            Vector3d start = remaining.iterator().next();
            remaining.remove(start);

            double minX = start.x, minY = start.y, minZ = start.z;
            double maxX = start.x, maxY = start.y, maxZ = start.z;

            Queue<Vector3d> queue = new ArrayDeque<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                Vector3d current = queue.poll();

                minX = Math.min(minX, current.x);
                minY = Math.min(minY, current.y);
                minZ = Math.min(minZ, current.z);
                maxX = Math.max(maxX, current.x);
                maxY = Math.max(maxY, current.y);
                maxZ = Math.max(maxZ, current.z);

                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++) {
                            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                            Vector3d neighbor = new Vector3d(current.x + dx, current.y + dy, current.z + dz);
                            if (remaining.remove(neighbor)) queue.add(neighbor);
                        }
            }

            aabbs.add(new AABB(minX, minY, minZ, maxX + 1.0, maxY + 1.0, maxZ + 1.0));
        }
        return aabbs;
    }
}