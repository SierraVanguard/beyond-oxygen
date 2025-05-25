package com.sierravanguard.beyond_oxygen.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;

import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelmetStateProvider implements ICapabilitySerializable<CompoundTag> {

    private final Player player;
    private final HelmetState instance;
    private final LazyOptional<IHelmetState> optional;
    public HelmetStateProvider(Player player) {
        this.player = player;
        this.instance = new HelmetState(player);
        this.optional = LazyOptional.of(() -> instance);
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == BOCapabilities.HELMET_STATE ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Open", instance.isOpen());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setOpen(nbt.getBoolean("Open"));
    }
}
