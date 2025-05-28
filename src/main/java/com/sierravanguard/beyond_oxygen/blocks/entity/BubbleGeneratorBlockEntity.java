package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.client.menu.BubbleGeneratorMenu;
import com.sierravanguard.beyond_oxygen.compat.CompatLoader;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.BubbleGeneratorTracker;
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
    private int clientOxygenLevel = 0;
    private int clientEnergyLevel = 0;
    private int lastSentEnergy = -1;
    private int lastSentOxygen = -1;
    public int temperatureRegulatorCooldown = 0;
    public boolean temperatureRegulatorApplied = false;
    private final Set<Fluid> acceptedFluids = new HashSet<>();
    public float controlledMaxRadius = BOConfig.bubbleMaxRadius;

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
        if (entity.temperatureRegulatorCooldown > 0) {
            entity.temperatureRegulatorCooldown--;
        }

        int energyRequired = 20;
        float oxygenNeeded = BOConfig.ventConsumption * 2.0f;
        boolean hasEnergy = entity.energyStorage.extractEnergy(energyRequired, false) == energyRequired;
        boolean hasOxygen = entity.tank.getFluidAmount() >= oxygenNeeded;

        if (hasEnergy && hasOxygen) {
            entity.energyStorage.extractEnergy(energyRequired, false);
            entity.consumeOxygen((int) oxygenNeeded);
            if (entity.currentRadius < entity.controlledMaxRadius) {
                entity.currentRadius = Math.min(entity.currentRadius + 0.01f, entity.controlledMaxRadius);
            } else if (entity.currentRadius > entity.controlledMaxRadius) {
                entity.currentRadius = Math.max(entity.controlledMaxRadius, entity.currentRadius - 0.01f);
            } else {
                entity.currentRadius = entity.controlledMaxRadius;
            }

            for (ServerPlayer player : ((ServerLevel) level).players()) {
                boolean success = BeyondOxygen.ModsLoaded.VS && VSCompat.applyBubbleEffects(player, pos, entity.currentRadius, entity);
                if (!success) {
                    Vec3 eyePos = player.getEyePosition();
                    if (eyePos.distanceTo(Vec3.atCenterOf(pos)) <= entity.currentRadius * 2){
                        player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
                        if (entity.GetRegulator()) CompatLoader.setComfortableTemperature(player);
                    }
                }
            }
        } else {
            entity.currentRadius = Math.max(0, entity.currentRadius - 0.1f);
        }
        boolean radiusChanged = Math.abs(entity.currentRadius - entity.lastSentRadius) > 0.01f;
        boolean energyChanged = Math.abs(entity.energyStorage.getEnergyStored() - entity.lastSentEnergy) > 40;
        boolean oxygenChanged = Math.abs(entity.tank.getFluidAmount() - entity.lastSentOxygen) > 50;

        if (radiusChanged || energyChanged || oxygenChanged) {
            if (radiusChanged) entity.lastSentRadius = entity.currentRadius;
            if (energyChanged) entity.lastSentEnergy = entity.energyStorage.getEnergyStored();
            if (oxygenChanged) entity.lastSentOxygen = entity.tank.getFluidAmount();

            entity.clientEnergyLevel = entity.lastSentEnergy;
            entity.clientOxygenLevel = entity.lastSentOxygen;

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
        return (int) ((clientEnergyLevel / (float) energyStorage.getMaxEnergyStored()) * 100);
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
        tag.putBoolean("TemperatureRegulatorApplied", temperatureRegulatorApplied);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("ticks", leftTicks);
        tag.putFloat("radius", currentRadius);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putFloat("controlledMaxRadius", controlledMaxRadius);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        temperatureRegulatorApplied = tag.getBoolean("TemperatureRegulatorApplied");
        leftTicks = tag.getInt("ticks");
        currentRadius = tag.getFloat("radius");
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
        if (tag.contains("controlledMaxRadius")) {
            controlledMaxRadius = tag.getFloat("controlledMaxRadius");
        }
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
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putFloat("controlledMaxRadius", controlledMaxRadius);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("radius")) currentRadius = tag.getFloat("radius");
        if (tag.contains("tankAmount")) {
            clientOxygenLevel = tag.getInt("tankAmount");
        }
        if (tag.contains("energy")) clientEnergyLevel = tag.getInt("energy");
        if (tag.contains("controlledMaxRadius")) {
            controlledMaxRadius = tag.getFloat("controlledMaxRadius");
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
        if (level != null && level.isClientSide) {
            return clientOxygenLevel / 1000.0;
        }
        return tank.getFluidAmount() / 1000.0;
    }
    public boolean GetRegulator(){
        return temperatureRegulatorApplied;
    }
    public boolean isBlockInsideBubble(BlockPos pos) {
        double distance = pos.distSqr(this.worldPosition);
        double radiusSquared = currentRadius * currentRadius;
        return distance <= radiusSquared;
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            BubbleGeneratorTracker.register(serverLevel, this.getBlockPos());
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            BubbleGeneratorTracker.unregister(serverLevel, this.getBlockPos());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            BubbleGeneratorTracker.unregister(serverLevel, this.getBlockPos());
        }
    }



}
