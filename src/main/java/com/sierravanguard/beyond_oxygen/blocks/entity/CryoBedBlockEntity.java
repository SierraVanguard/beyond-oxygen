package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.utils.CryoBedManager;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;

import java.util.UUID;

public class CryoBedBlockEntity extends BlockEntity {

    private UUID ownerUUID = null;  // Nullable owner UUID

    ServerShip cachedShip;
    Vector3d cachedShipLocalPos;

    public CryoBedBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.CRYO_BED.get(), pos, state);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
        if (level != null && !level.isClientSide && uuid != null) {
            CryoBedManager.assignCryoBed(uuid, level.dimension(), worldPosition);
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void updatePlayerCryoBed(Player player) {
        setOwnerUUID(player.getUUID());
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
        ServerShip ship = VSCompat.getShipAtPosition(level, pos);
        if (ship != null) {
            var transform = ship.getTransform();
            Vector3d worldPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            Vector3d shipLocal = transform.getWorldToShip().transformPosition(worldPos);

            blockEntity.cachedShip = ship;
            blockEntity.cachedShipLocalPos = shipLocal;

            CryoBedManager.updateCryoBedDimension(level, pos, level.dimension(), shipLocal);
        } else {
            blockEntity.cachedShip = null;
            blockEntity.cachedShipLocalPos = null;

            CryoBedManager.updateCryoBedDimension(level, pos, level.dimension(), null);
        }
        if (blockEntity.getOwnerUUID() != null) {
            CryoBedManager.assignCryoBed(blockEntity.getOwnerUUID(), level.dimension(), pos);
        }
    }

}
