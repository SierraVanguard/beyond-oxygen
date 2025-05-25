package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.client.menu.BubbleGeneratorMenu;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BubbleGeneratorBlockEntity extends BlockEntity implements MenuProvider{
    private float lastSentRadius = -1f;

    private final Set<Fluid> acceptedFluids = new HashSet<>();

    public void loadAcceptedFluidsFromConfig(List<ResourceLocation> fluidIds) {
        for (ResourceLocation fluidId : fluidIds) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid != null) {
                acceptedFluids.add(fluid);
            }
        }
    }

    private final FluidTank tank = new FluidTank(1000, fluidStack -> acceptedFluids.contains(fluidStack.getFluid()));
    private LazyOptional<FluidTank> tankLazyOptional = LazyOptional.of(() -> tank);

    private final EnergyStorage energyStorage = new EnergyStorage(10000);
    private LazyOptional<EnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    private int leftTicks = 0;
    public float currentRadius = 0.0f;
    public float maxRadius = BOConfig.bubbleMaxRadius;

    public BubbleGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.BUBBLE_GENERATOR.get(), pos, state);
        loadAcceptedFluidsFromConfig(BOConfig.oxygenFluids);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (level.isClientSide) return;
        BubbleGeneratorBlockEntity entity = (BubbleGeneratorBlockEntity) be;

        int energyRequired = 20;
        float oxygenNeeded = BOConfig.ventConsumption * 2.0f;
        boolean hasEnergy = entity.energyStorage.extractEnergy(energyRequired, true) == energyRequired;
        boolean hasOxygen = entity.tank.getFluidAmount() >= oxygenNeeded;

        if (hasEnergy && hasOxygen) {
            entity.energyStorage.extractEnergy(energyRequired, false);
            entity.consumeOxygen((int) oxygenNeeded);
            if (entity.currentRadius < entity.maxRadius) entity.currentRadius += 0.01f;

            for (ServerPlayer player : ((ServerLevel) level).players()) {
                boolean success = BeyondOxygen.ModsLoaded.VS && VSCompat.applyBubbleEffects(player, pos, entity.currentRadius);
                if (!success) {
                    Vec3 eyePos = player.getEyePosition();
                    if (eyePos.distanceTo(Vec3.atCenterOf(pos)) <= entity.currentRadius * 2)
                        player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
                }
            }
        } else {
            entity.currentRadius = Math.max(0, entity.currentRadius - 0.1f);
        }

        if (Math.abs(entity.currentRadius - entity.lastSentRadius) > 0.01f) {
            entity.lastSentRadius = entity.currentRadius;
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private boolean consumeOxygen(int amount) {
        if (tank.getFluidAmount() < amount) return false;
        tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    public float getCurrentRadius() {
        return currentRadius;
    }

    public int getPowerLevel() {
        return energyStorage.getEnergyStored() * 100 / energyStorage.getMaxEnergyStored();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return tankLazyOptional.cast();
        if (cap == ForgeCapabilities.ENERGY) return energy.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("ticks", leftTicks);
        tag.putFloat("radius", currentRadius);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        leftTicks = tag.getInt("ticks");
        currentRadius = tag.getFloat("radius");
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankLazyOptional.invalidate();
        energy.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        tankLazyOptional = LazyOptional.of(() -> tank);
        energy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putFloat("radius", currentRadius);
        tag.putInt("tankAmount", tank.getFluidAmount());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("radius")) currentRadius = tag.getFloat("radius");
        if (tag.contains("tankAmount")) {
            int tankAmount = tag.getInt("tankAmount");
            //TODO: Tank client-side logic implementation for GUI rendering.
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.beyond_oxygen.bubble_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
        return new BubbleGeneratorMenu(id, inventory, this.getBlockPos());
    }
    public double getOxygenRatio() {
        if (this.tank.getFluidAmount() == 0) return 0.0;
        return (double) this.tank.getFluidAmount() / 1000;
    }
}
