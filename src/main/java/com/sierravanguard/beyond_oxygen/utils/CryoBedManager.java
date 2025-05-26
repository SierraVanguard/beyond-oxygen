package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.blocks.CryoBedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.*;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CryoBedManager {

    public record CryoBedReference(
            ResourceKey<Level> dimension,
            BlockPos worldPos,
            Long shipId,             // nullable if no ship
            Vector3d shipLocalPos    // nullable if no ship
    ) {}
    private static final Map<ResourceKey<Level>, Set<BlockPos>> cryoBedPositions = new HashMap<>();

    private static final Map<UUID, CryoBedReference> playerCryoBedMap = new HashMap<>();

    /**
     * Assign a cryobed to a player (without ship data).
     */
    public static void assignCryoBed(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos) {
        playerCryoBedMap.put(playerUUID, new CryoBedReference(dimKey, pos, null, null));
        addCryoBed(dimKey, pos);
    }

    /**
     * Assign a cryobed to a player with ship data.
     */
    public static void assignCryoBed(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos, Long shipId, Vector3d shipLocalPos) {
        playerCryoBedMap.put(playerUUID, new CryoBedReference(dimKey, pos, shipId, shipLocalPos));
        addCryoBed(dimKey, pos);
    }

    /**
     * Checks if player has assigned cryobed.
     */
    public static Optional<CryoBedReference> getAssignedCryoBed(UUID playerUUID) {
        return Optional.ofNullable(playerCryoBedMap.get(playerUUID));
    }

    /**
     * Checks if the given bed is assigned to the player.
     */
    public static boolean isAssignedCryoBed(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos) {
        CryoBedReference assigned = playerCryoBedMap.get(playerUUID);
        return assigned != null &&
                assigned.dimension().equals(dimKey) &&
                assigned.worldPos.equals(pos);
    }


    public static void addCryoBed(ResourceKey<Level> dimKey, BlockPos pos) {
        cryoBedPositions.computeIfAbsent(dimKey, k -> new HashSet<>()).add(pos);
    }

    public static void removeCryoBed(ResourceKey<Level> dimKey, BlockPos pos) {
        Set<BlockPos> positions = cryoBedPositions.get(dimKey);
        if (positions != null) {
            positions.remove(pos);
            if (positions.isEmpty()) {
                cryoBedPositions.remove(dimKey);
            }
        }
        playerCryoBedMap.entrySet().removeIf(entry -> {
            CryoBedReference ref = entry.getValue();
            return ref.dimension().equals(dimKey) && ref.worldPos.equals(pos);
        });
    }

    public static boolean hasCryoBedAt(ResourceKey<Level> dimKey, BlockPos pos) {
        Set<BlockPos> positions = cryoBedPositions.get(dimKey);
        return positions != null && positions.contains(pos);
    }

    /**
     * Get all cryobed positions for a dimension.
     */
    public static Set<BlockPos> getCryoBeds(ResourceKey<Level> dimKey) {
        return cryoBedPositions.getOrDefault(dimKey, Collections.emptySet());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level level = event.level;
        if (level.isClientSide()) return;

        ResourceKey<Level> dimKey = level.dimension();

        if (!(level instanceof ServerLevel serverLevel)) return;

        for (Map.Entry<UUID, CryoBedReference> entry : playerCryoBedMap.entrySet()) {
            CryoBedReference ref = entry.getValue();

            if (!ref.dimension().equals(dimKey)) continue;

            ServerShip ship = (ServerShip) VSCompat.getShipAtPosition(serverLevel, ref.worldPos);
            if (ship != null) {
                var shipTransform = ship.getTransform();

                Vector3d worldCenterPos = new Vector3d(
                        ref.worldPos.getX() + 0.5,
                        ref.worldPos.getY() + 1.0,
                        ref.worldPos.getZ() + 0.5
                );

                Vector3d shipLocalPos = shipTransform.getWorldToShip().transformPosition(worldCenterPos);

                CryoBedReference updatedRef = new CryoBedReference(
                        ref.dimension(),
                        ref.worldPos,
                        ship.getId(),
                        shipLocalPos
                );

                entry.setValue(updatedRef);
            } else if (ref.shipId != null) {
                CryoBedReference updatedRef = new CryoBedReference(
                        ref.dimension(),
                        ref.worldPos,
                        null,
                        null
                );
                entry.setValue(updatedRef);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level)) return;

        BlockState placed = event.getPlacedBlock();
        if (placed.getBlock() instanceof CryoBedBlock) {
            addCryoBed(level.dimension(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof Level level)) return;

        if (event.getState().getBlock() instanceof CryoBedBlock) {
            removeCryoBed(level.dimension(), event.getPos());
        }
    }

    /**
     * Update cryobed dimension and ship local pos for all players assigned to this bed.
     * Use this when a cryobed moves dimension or ship local pos changes (like on a ship warp).
     */
    public static void updateCryoBedDimension(ServerLevel level, BlockPos pos, ResourceKey<Level> newDimension, Vector3d shipLocalPos) {
        ResourceKey<Level> oldDim = level.dimension();

        playerCryoBedMap.entrySet().forEach(entry -> {
            CryoBedReference ref = entry.getValue();
            if (ref.dimension().equals(oldDim) && ref.worldPos.equals(pos)) {
                CryoBedReference updatedRef = new CryoBedReference(
                        newDimension,
                        pos,
                        ref.shipId,
                        shipLocalPos
                );
                entry.setValue(updatedRef);
            }
        });
    }

    /**
     * Helper method: assign cryobed by UUID using BE data, including ship data if available.
     * This will add the bed position to the global set, and update player's assigned bed.
     */
    public static void assignCryoBedByUUID(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos, Long shipId, Vector3d shipLocalPos) {
        playerCryoBedMap.put(playerUUID, new CryoBedReference(dimKey, pos, shipId, shipLocalPos));
        addCryoBed(dimKey, pos);
    }

    /**
     * Utility to get ServerLevel by dimension key from server instance.
     */
    public static Optional<ServerLevel> getServerLevel(MinecraftServer server, ResourceKey<Level> dimensionKey) {
        return Optional.ofNullable(server.getLevel(dimensionKey));
    }
}
