package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.compat.CompatUtils;
import com.sierravanguard.beyond_oxygen.utils.CryoBedManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;

import java.util.Optional;
import java.util.UUID;

public class CryoBedBlockEntity extends BlockEntity {

    private UUID ownerUUID = null;

    Long cachedShipId;
    Vector3d cachedShipLocalPos;

    public CryoBedBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.CRYO_BED.get(), pos, state);
    }

    public void setOwnerUUID(UUID uuid) {
        if (this.ownerUUID != null && !this.ownerUUID.equals(uuid)) {
            System.out.println("[DEBUG] Cryobed ownership changing from " + this.ownerUUID + " to " + uuid);
        }

        this.ownerUUID = uuid;
        if (level != null && !level.isClientSide && uuid != null) {
            if (cachedShipId != null && cachedShipLocalPos != null) {
                CryoBedManager.assignCryoBed(uuid, level.dimension(), worldPosition, cachedShipId, cachedShipLocalPos);
            } else {
                CryoBedManager.assignCryoBed(uuid, level.dimension(), worldPosition);
            }
        }
        setChanged();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void updatePlayerCryoBed(Player player) {
        UUID playerUUID = player.getUUID();
        Optional<CryoBedManager.CryoBedReference> currentAssignment = CryoBedManager.getAssignedCryoBed(playerUUID);
        if (currentAssignment.isPresent() && !currentAssignment.get().worldPos().equals(this.worldPosition)) {
            System.out.println("[DEBUG] Player " + player.getName().getString() + " already has cryobed at " +
                    currentAssignment.get().worldPos() + ", reassigning to " + this.worldPosition);
        }

        setOwnerUUID(playerUUID);
    }

    public void clearOwner() {
        if (this.ownerUUID != null) {
            System.out.println("[DEBUG] Clearing owner " + this.ownerUUID + " from cryobed at " + worldPosition);
            CryoBedManager.removeCryoBed(level.dimension(), worldPosition);
            this.ownerUUID = null;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        } else {
            ownerUUID = null;
        }
    }

    public static <T extends CryoBedBlockEntity> void tick(ServerLevel level, BlockPos pos, BlockState state, T blockEntity) {
        Pair<Long, Vector3d> shipAndPos = CompatUtils.getCryoBedShipAndPosition(level, pos);
        blockEntity.cachedShipId = shipAndPos.getLeft();
        blockEntity.cachedShipLocalPos = shipAndPos.getRight();
        CryoBedManager.updateCryoBedDimension(level, pos, level.dimension(), shipAndPos.getRight());

        if (blockEntity.getOwnerUUID() != null) {
            if (blockEntity.cachedShipId != null && blockEntity.cachedShipLocalPos != null) {
                CryoBedManager.assignCryoBed(blockEntity.getOwnerUUID(), level.dimension(), pos,
                        blockEntity.cachedShipId, blockEntity.cachedShipLocalPos);
            } else {
                CryoBedManager.assignCryoBed(blockEntity.getOwnerUUID(), level.dimension(), pos);
            }
        }
    }
}