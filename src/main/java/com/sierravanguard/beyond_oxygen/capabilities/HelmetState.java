package com.sierravanguard.beyond_oxygen.capabilities;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.network.SyncEntityHelmetStatePacket;
import com.sierravanguard.beyond_oxygen.network.SyncHelmetStatePacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelmetState implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(BeyondOxygen.MODID, "helmet_state");
    private static final Capability<HelmetState> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static LazyOptional<HelmetState> get(LivingEntity entity) {
        return entity.getCapability(CAPABILITY);
    }

    private final LazyOptional<HelmetState> lazy = LazyOptional.of(() -> this);
    public final LivingEntity owner;
    private boolean isOpen;

    public HelmetState(LivingEntity owner) {
        this.owner = owner;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        setOpen(open, true);
    }

    public void setOpen(boolean open, boolean syncSelf) {
        if (isOpen != open) {
            this.isOpen = open;
            if (!owner.level().isClientSide()) {
                NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> owner),
                        new SyncEntityHelmetStatePacket(owner.getId(), open));
                if (syncSelf && owner instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new SyncHelmetStatePacket(open));
                }
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? lazy.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Open", isOpen);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setOpen(nbt.getBoolean("Open"));
    }
}
