package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.network.SyncHermeticBlocksS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

 
public class HermeticArea {
    private boolean hasActiveTemperatureRegulator = false;
    private final ServerLevel level;
    private final long id;
    private long shipId;
    private boolean hermetic;
    private boolean hasAir;
    private boolean dirty;
    private AABB bounds;

 
    private final Set<BlockPos> knownVents = new HashSet<>();
 
    private final Set<BlockPos> activeVents = new HashSet<>();

    private final Set<BlockPos> blocks = new HashSet<>();
    private final Set<BlockPos> boundaryBlocks = new HashSet<>();

    private BlockPos centerVentPos = BlockPos.ZERO;

    double lastComputedVolume;
    private boolean dormant = false;
    private int dormantTicks = 0;
    private static final int DORMANT_TICK_LIMIT = 20 * 60 * 5; 

    public HermeticArea(ServerLevel level, long id) {
        this.level = level;
        this.id = id;
        this.shipId = -1L;
        this.dirty = true;
    }

    public HermeticArea(ServerLevel level, BlockPos start, long id) {
        this.level = level;
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, start);
        this.shipId = (ship == null) ? -1L : ship.getId();
        this.id = id;
        this.dirty = true;
    }

 
    public long getId() { return id; }
    public boolean isHermetic() { return hermetic; }
    public boolean hasAir() { return hasAir; }
    public void setHasAir(boolean hasAir) { this.hasAir = hasAir; }
    public boolean isDirty() { return dirty; }
    public void markDirty() { dirty = true; }
    public ServerLevel getLevel() { return level; }
    public long getShipId() { return shipId; }

 
    public Set<BlockPos> getVents() { return Collections.unmodifiableSet(knownVents); }
 
    public Set<BlockPos> getActiveVents() { return Collections.unmodifiableSet(activeVents); }
    public Set<BlockPos> getBlocks() { return blocks; }
    public Set<BlockPos> getBoundaryBlocks() { return boundaryBlocks; }

    public boolean contains(BlockPos pos) { return blocks.contains(pos); }
    public boolean containsInclusive(BlockPos pos) { return blocks.contains(pos) || boundaryBlocks.contains(pos); }

     
    public void addVent(VentBlockEntity vent) {
        BlockPos pos = vent.getBlockPos();
        knownVents.add(pos);
        activeVents.add(pos);
        if (centerVentPos == BlockPos.ZERO) {
            centerVentPos = pos;
 
            vent.setCenterVent(true);
        }
        dormant = false;
        dormantTicks = 0;
        markDirty();
    }

    public void removeVent(BlockPos pos, boolean destroyed) {
        activeVents.remove(pos);
        if (destroyed) knownVents.remove(pos);
        markDirty();
        if (pos.equals(centerVentPos) && !knownVents.isEmpty()) {
            updateCenterVent();
        }
        if (knownVents.isEmpty()) {
            hermetic = false;
            dirty = false;
            clearAndNotifyPlayers();
            return;
        }
        if (activeVents.isEmpty()) {
            dormant = true;
            dormantTicks = 0;
            hermetic = false;
            dirty = false;
            return;
        }
        bake();
        syncToClients();
    }

 
    public boolean bake() {

        if (!dirty) return hermetic;
        dirty = false;
        int LIMIT = Math.max(2048, BOConfig.VENT_RANGE.get());

        Set<BlockPos> newBlocks = new HashSet<>();
        Set<BlockPos> newBoundary = new HashSet<>();

        BlockPos centerVent = getCenterPos();
        if (centerVent.equals(BlockPos.ZERO)) {
            hermetic = false;
            clearAndNotifyPlayers();
            return false;
        }

        blocks.add(centerVent);

        BlockState ventState = level.getBlockState(centerVent);
        Direction ventFacing = ventState.hasProperty(com.sierravanguard.beyond_oxygen.blocks.VentBlock.FACING)
                ? ventState.getValue(com.sierravanguard.beyond_oxygen.blocks.VentBlock.FACING)
                : Direction.UP;

        Deque<AirBlockData> queue = new ArrayDeque<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = centerVent.relative(dir);
            if (neighbor.equals(centerVent)) continue;

            boolean hermeticCheck = dir != ventFacing && HermeticUtils.isHermetic(level, neighbor, dir.getOpposite());
            if (!hermeticCheck && !newBlocks.contains(neighbor)) {
                newBlocks.add(neighbor);
                queue.add(new AirBlockData(neighbor).setSource(dir.getOpposite()));
            } else if (hermeticCheck) {
                newBoundary.add(neighbor);
            }
        }

        while (!queue.isEmpty()) {
            if (newBlocks.size() >= LIMIT) {
                clearAndNotifyPlayers();
                return false;
            }
            AirBlockData current = queue.poll();

            for (Direction dir : Direction.values()) {
                if (current.isSource(dir)) continue;

                BlockPos neighbor = current.relative(dir);
                if (newBlocks.contains(neighbor)) continue;

                BlockState neighborState = level.getBlockState(neighbor);
                boolean isVent = neighborState.getBlock() instanceof com.sierravanguard.beyond_oxygen.blocks.VentBlock;
                boolean hermeticWall = HermeticUtils.isHermetic(level, neighbor, dir.getOpposite());
                boolean canFlow = !hermeticWall && (isVent || HermeticUtils.canFlowTrough(level, current, current.getSource(), dir));

                if (canFlow) {
                    newBlocks.add(neighbor);
                    queue.add(new AirBlockData(neighbor).setSource(dir.getOpposite()));

                    if (isVent) {
                        VentBlockEntity neighborVent = (VentBlockEntity) level.getBlockEntity(neighbor);
                        if (neighborVent != null && neighborVent.getHermeticArea() != this) {
                            HermeticArea other = neighborVent.getHermeticArea();
                            if (other != null && other != this) {
                                if (tiebreaker(other)) {
                                    mergeFrom(other);
                                } else {
                                    clearAndNotifyPlayers();
                                    return false;
                                }
                            }
                        }
                    }
                } else if (hermeticWall) {
                    newBoundary.add(neighbor);
                }
            }
        }
        boolean newHermetic = queue.isEmpty();
        blocks.clear();
        blocks.addAll(newBlocks);
        boundaryBlocks.clear();
        boundaryBlocks.addAll(newBoundary);
        hermetic = true;
        this.lastComputedVolume = this.getBlocks().size();
        recalcTemperatureRegulator();
        HermeticAreaServerManager.markDirty(level);
        recalcBounds();
        HermeticAreaData data = HermeticAreaData.get(level);
        data.indexArea(this);
        syncToClients();
        return hermetic;
    }

    private void mergeFrom(HermeticArea other) {
 
        for (BlockPos ventPos : other.knownVents) {
            knownVents.add(ventPos);
            activeVents.add(ventPos);
            var be = level.getBlockEntity(ventPos);
            if (be instanceof VentBlockEntity vbe) {
                vbe.setHermeticArea(this);
 
                vbe.setCenterVent(false);
                addVent(vbe); 
            }
        }
        blocks.addAll(other.getBlocks());
 
        HermeticAreaServerManager.removeArea(level, other.getId());
        other.clearAndNotifyPlayers();
    }

    private void syncToClients() {
        Set<Vec3> vecs = new HashSet<>();
        for (BlockPos p : blocks) vecs.add(new Vec3(p.getX(), p.getY(), p.getZ()));
        if (hermetic) {
            NetworkHandler.sendToAllPlayers(new SyncHermeticBlocksS2CPacket(id, vecs, shipId));
        } else {
 
 
            NetworkHandler.sendInvalidateHermeticAreas(id, false);
        }
    }

     
    public void clearAndNotifyPlayers() {
        if (!dirty && blocks.isEmpty()) return; 
        blocks.clear();
        boundaryBlocks.clear();
        hermetic = false;
        dirty = false;

 
        for (BlockPos ventPos : new HashSet<>(activeVents)) {
            var be = level.getBlockEntity(ventPos);
            if (be instanceof VentBlockEntity vent) {
                vent.setHermeticArea(null);
            }
        }

 
        activeVents.clear();
        knownVents.clear();

 
        List<Player> toRemove = new ArrayList<>();
        for (Map.Entry<Player, Set<HermeticArea>> entry : VSCompat.playersInSealedAreas.entrySet()) {
            Set<HermeticArea> areas = entry.getValue();
            if (areas.remove(this) && areas.isEmpty()) {
                toRemove.add(entry.getKey());
                NetworkHandler.sendSealedAreaStatusToClient(entry.getKey(), false);
            }
        }
        for (Player p : toRemove) VSCompat.playersInSealedAreas.remove(p);

 
        HermeticAreaServerManager.removeAreaDeferred(level, id);
    }

 
    private boolean tiebreaker(HermeticArea other) {
        if (other == null || other == this) return true;
        BlockPos myMax = this.getCenterPos();
        BlockPos otherMax = other.getCenterPos();
        int dimCompare = this.level.dimension().location().compareTo(other.level.dimension().location());
        if (dimCompare != 0) return dimCompare > 0;
        return myMax.asLong() > otherMax.asLong();
    }

 
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Id", id);
        tag.putBoolean("Hermetic", hermetic);
        tag.putBoolean("HasAir", hasAir);
        tag.putLong("ShipId", shipId);
        tag.put("Vents", writePositions(knownVents));
        tag.put("Blocks", writePositions(blocks));
        tag.put("Boundary", writePositions(boundaryBlocks));
        tag.putDouble("Volume", lastComputedVolume);
        return tag;
    }

    public static HermeticArea load(ServerLevel level, CompoundTag tag) {
        long id = tag.getLong("Id");
        HermeticArea area = new HermeticArea(level, id);
        area.hermetic = tag.getBoolean("Hermetic");
        area.hasAir = tag.getBoolean("HasAir");
        area.shipId = tag.getLong("ShipId");
        area.knownVents.addAll(readPositions(tag.getList("Vents", 10)));
        area.blocks.addAll(readPositions(tag.getList("Blocks", 10)));
        area.boundaryBlocks.addAll(readPositions(tag.getList("Boundary", 10)));
        area.lastComputedVolume = tag.getDouble("Volume");
        return area;
    }

    private static ListTag writePositions(Set<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) list.add(NbtUtils.writeBlockPos(pos));
        return list;
    }

    private static Set<BlockPos> readPositions(ListTag list) {
        Set<BlockPos> result = new HashSet<>();
        for (int i = 0; i < list.size(); i++) result.add(NbtUtils.readBlockPos(list.getCompound(i)));
        return result;
    }

    @Override
    public String toString() {
        return String.format("HermeticArea{id=%d, shipId=%d, knownVents=%d, activeVents=%d, blocks=%d}", id, shipId, knownVents.size(), activeVents.size(), blocks.size());
    }

    public void recalcBounds() {
        if (boundaryBlocks.isEmpty()) {
            bounds = new AABB(BlockPos.ZERO);
            return;
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : boundaryBlocks) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        bounds = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public boolean maybeContains(BlockPos pos) {
        return bounds != null && bounds.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    public AABB getBounds(){ return bounds; }

    public BlockPos getCenterPos() {
        if (centerVentPos != BlockPos.ZERO && knownVents.contains(centerVentPos)) return centerVentPos;
        if (knownVents.isEmpty()) return BlockPos.ZERO;
        centerVentPos = knownVents.stream().max(Comparator.comparingLong(BlockPos::asLong)).orElse(BlockPos.ZERO);
        return centerVentPos;
    }

    private void updateCenterVent() {
        centerVentPos = knownVents.stream().max(Comparator.comparingLong(BlockPos::asLong)).orElse(BlockPos.ZERO);
        if (centerVentPos != BlockPos.ZERO) {
            var be = level.getBlockEntity(centerVentPos);
            if (be instanceof VentBlockEntity vbe) {
                vbe.setCenterVent(true);
            }
        }
    }

 
    public boolean tickDormant() {
        if (!dormant) return false;
        dormantTicks++;
        if (dormantTicks > DORMANT_TICK_LIMIT) {
            clearAndNotifyPlayers();
            return true;
        }
        return false;
    }

    public void activateVent(BlockPos pos) {
        activeVents.add(pos);
        knownVents.add(pos); 
        dormant = false;
        dormantTicks = 0;
        markDirty();
    }

    public void deactivateVent(BlockPos pos) {
        activeVents.remove(pos);
 
        if (activeVents.isEmpty()) {
            dormant = true;
            dormantTicks = 0;
        }
    }

    public boolean isDormant() {
        return dormant;
    }

    public boolean hasActiveTemperatureRegulator() {
        return hasActiveTemperatureRegulator;
    }

    public void recalcTemperatureRegulator() {
        hasActiveTemperatureRegulator = false;
        for (BlockPos ventPos : activeVents) {
            BlockEntity be = level.getBlockEntity(ventPos);
            if (be instanceof VentBlockEntity vent && vent.temperatureRegulatorApplied) {
                hasActiveTemperatureRegulator = true;
                break;
            }
        }
    }


}
