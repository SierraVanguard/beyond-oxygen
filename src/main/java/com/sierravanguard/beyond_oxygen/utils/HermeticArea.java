package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.network.SyncHermeticBlocksS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

public class HermeticArea {
    private static final int LIMIT = BOConfig.ventRange;

    private final Set<BlockPos> blocks = new HashSet<>();
    private final Set<BlockPos> boundaryBlocks = new HashSet<>();
    private final ServerLevel level;
    private final long shipId;

    private boolean hermetic;
    private boolean dirty;

    public HermeticArea(ServerLevel level, BlockPos start) {
        this.level = level;
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, start);
        this.shipId = (ship == null) ? -1L : ship.getId();
        this.dirty = true;
        HermeticAreaServerManager.register(this);
    }

    public boolean bake(BlockPos ventPos) {
        if (!dirty) return hermetic;

        dirty = false;
        blocks.clear();
        boundaryBlocks.clear();

        Deque<AirBlockData> queue = new ArrayDeque<>();
        BlockState ventState = level.getBlockState(ventPos);
        blocks.add(ventPos);
        Direction ventFacing = ventState.hasProperty(com.sierravanguard.beyond_oxygen.blocks.VentBlock.FACING)
                ? ventState.getValue(com.sierravanguard.beyond_oxygen.blocks.VentBlock.FACING)
                : Direction.UP;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = ventPos.relative(dir);
            if (neighbor.equals(ventPos)) continue;

            BlockState state = level.getBlockState(neighbor);
            boolean hermeticCheck;
            if (dir == ventFacing) {
                hermeticCheck = false;
            } else {
                hermeticCheck = HermeticUtils.isHermetic(level, neighbor, dir.getOpposite());
            }


            if (!hermeticCheck) {
                if (!blocks.contains(neighbor)) {
                    blocks.add(neighbor);
                    queue.add(new AirBlockData(neighbor).setSource(dir.getOpposite()));
                }
            } else {
                boundaryBlocks.add(neighbor);
            }
        }
        while (!queue.isEmpty() && blocks.size() < LIMIT) {
            AirBlockData current = queue.poll();

            for (Direction dir : Direction.values()) {
                if (current.isSource(dir)) continue;

                BlockPos neighbor = current.relative(dir);
                if (blocks.contains(neighbor)) continue;

                BlockState neighborState = level.getBlockState(neighbor);
                if (neighbor.equals(ventPos) || neighborState.getBlock() instanceof com.sierravanguard.beyond_oxygen.blocks.VentBlock) {
                    continue;
                }

                boolean hermetic = HermeticUtils.isHermetic(level, neighbor, dir.getOpposite());
                boolean canFlow = !hermetic && HermeticUtils.canFlowTrough(level, current, current.getSource(), dir);

                if (canFlow) {
                    blocks.add(neighbor);
                    queue.add(new AirBlockData(neighbor).setSource(dir.getOpposite()));
                } else if (hermetic) {
                    boundaryBlocks.add(neighbor);
                }
            }
        }

        hermetic = queue.isEmpty();
        syncToClients();
        return hermetic;
    }
    public static ListTag serializePositions(Set<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag pTag = new CompoundTag();
            pTag.putInt("x", pos.getX());
            pTag.putInt("y", pos.getY());
            pTag.putInt("z", pos.getZ());
            list.add(pTag);
        }
        return list;
    }


    public static Set<BlockPos> deserializePositions(ListTag list) {
        Set<BlockPos> positions = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pTag = list.getCompound(i);
            positions.add(new BlockPos(pTag.getInt("x"), pTag.getInt("y"), pTag.getInt("z")));
        }
        return positions;
    }
    public void setBlocks(Set<BlockPos> newBlocks) {
        blocks.clear();
        blocks.addAll(newBlocks);
    }

    public void setBoundaryBlocks(Set<BlockPos> newBoundary) {
        boundaryBlocks.clear();
        boundaryBlocks.addAll(newBoundary);
    }

    public void setHermetic(boolean value) {
        hermetic = value;
    }

    public Set<BlockPos> getBoundaryBlocks() {
        return Collections.unmodifiableSet(boundaryBlocks);
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean contains(BlockPos pos) {
        return blocks.contains(pos);
    }

    public boolean isHermetic() {
        return hermetic;
    }

    public long getShipId() {
        return shipId;
    }

    public Set<BlockPos> getBlocks() {
        return Collections.unmodifiableSet(blocks);
    }

    private void syncToClients() {
        Set<Vec3> vecs = new HashSet<>();
        for (BlockPos p : blocks) vecs.add(new Vec3(p.getX(), p.getY(), p.getZ()));

        if (hermetic)
            NetworkHandler.sendToAllPlayers(new SyncHermeticBlocksS2CPacket(shipId, vecs));
        else
            NetworkHandler.sendInvalidateHermeticAreas(shipId, false);
    }
    public void clear() {
        blocks.clear();
        boundaryBlocks.clear();
        hermetic = false;
        dirty = false;
        HermeticAreaServerManager.unregister(this);
        NetworkHandler.sendInvalidateHermeticAreas(shipId, false);
    }
}
