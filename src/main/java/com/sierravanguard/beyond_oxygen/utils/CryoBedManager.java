package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.blocks.CryoBedBlock;
import com.sierravanguard.beyond_oxygen.compat.CompatUtils;
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

import java.util.*;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CryoBedManager {

    public record CryoBedReference(
            ResourceKey<Level> dimension,
            BlockPos worldPos,
            Long shipId, 
            Vector3d shipLocalPos 
    ) {}
    private static final Map<ResourceKey<Level>, Set<BlockPos>> cryoBedPositions = new HashMap<>();

    private static final Map<UUID, CryoBedReference> playerCryoBedMap = new HashMap<>();

     
    public static void assignCryoBed(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos) {
        playerCryoBedMap.put(playerUUID, new CryoBedReference(dimKey, pos, null, null));
        addCryoBed(dimKey, pos);
    }

     
    public static void assignCryoBed(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos, Long shipId, Vector3d shipLocalPos) {
        playerCryoBedMap.put(playerUUID, new CryoBedReference(dimKey, pos, shipId, shipLocalPos));
        addCryoBed(dimKey, pos);
    }

     
    public static Optional<CryoBedReference> getAssignedCryoBed(UUID playerUUID) {
        return Optional.ofNullable(playerCryoBedMap.get(playerUUID));
    }

     
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
            CompatUtils.updateCryoBedReference(serverLevel, ref, entry::setValue);
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

     
    public static void assignCryoBedByUUID(UUID playerUUID, ResourceKey<Level> dimKey, BlockPos pos, Long shipId, Vector3d shipLocalPos) {
        playerCryoBedMap.put(playerUUID, new CryoBedReference(dimKey, pos, shipId, shipLocalPos));
        addCryoBed(dimKey, pos);
    }

     
    public static Optional<ServerLevel> getServerLevel(MinecraftServer server, ResourceKey<Level> dimensionKey) {
        return Optional.ofNullable(server.getLevel(dimensionKey));
    }
}
