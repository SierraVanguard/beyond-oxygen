package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.VentBlock;
import com.sierravanguard.beyond_oxygen.compat.CompatLoader;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.HermeticArea;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import com.sierravanguard.beyond_oxygen.utils.VentTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VentBlockEntity extends BlockEntity {

    private int leftTicks = 0;
    public int temperatureRegulatorCooldown = 0;
    public boolean temperatureRegulatorApplied = false;

    private HermeticArea hermeticArea;
    private final Set<Fluid> acceptedFluids = new HashSet<>();
    private final FluidTank tank = new FluidTank(1000, fluidStack -> acceptedFluids.contains(fluidStack.getFluid()));
    private LazyOptional<FluidTank> tankLazyOptional = LazyOptional.of(() -> tank);

    public VentBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.VENT_BLOCK_ENTITY.get(), pos, state);
        loadAcceptedFluidsFromConfig(BOConfig.oxygenFluids);
    }

    private void loadAcceptedFluidsFromConfig(List<ResourceLocation> fluidIds) {
        for (ResourceLocation fluidId : fluidIds) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid != null) {
                acceptedFluids.add(fluid);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return tankLazyOptional.cast();
        return super.getCapability(cap, side);
    }

    private boolean consumeOxygen(int amount) {
        int current = tank.getFluidAmount();
        if (current < amount) return false;
        tank.drain(amount, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (level.isClientSide) return;
        Direction facing = state.getValue(VentBlock.FACING);
        VentBlockEntity vent = (VentBlockEntity) blockEntity;
        ServerLevel serverLevel = (ServerLevel) level;
        if (vent.hermeticArea == null) {
            vent.hermeticArea = new HermeticArea(serverLevel, pos);
            vent.hermeticArea.markDirty();
        }
        if (vent.temperatureRegulatorCooldown > 0) {
            vent.temperatureRegulatorCooldown--;
        }
        if (vent.hermeticArea.isDirty()) {
            vent.hermeticArea.bake(pos.relative(facing));
        }
        for (ServerPlayer player : serverLevel.players()) {
            boolean isInside = vent.isPlayerInsideHermeticArea(player);
            VSCompat.applySealedEffects(player, pos, vent.hermeticArea,vent);
            if (isInside && !vent.tank.isEmpty()) {
                int oxygenNeeded = Math.max(1, vent.hermeticArea.getBlocks().size() / BOConfig.ventConsumption);
                if (vent.temperatureRegulatorApplied) oxygenNeeded /= 2;

                if (vent.consumeOxygen(oxygenNeeded)) {
                    player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
                    if (vent.temperatureRegulatorApplied) {
                        CompatLoader.setComfortableTemperature(player);
                    }
                }
            }
        }
    }


    private boolean isPlayerInsideHermeticArea(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        BlockPos eyeBlock;
        if (BeyondOxygen.ModsLoaded.VS && level instanceof ServerLevel serverLevel) {
            var ship = VSCompat.getShipAtPosition(serverLevel, this.worldPosition);
            if (ship != null) {
                var shipEye = ship.getTransform().getWorldToShip().transformPosition(
                        new org.joml.Vector3d(eyePos.x, eyePos.y, eyePos.z)
                );
                eyeBlock = new BlockPos(
                        (int) Math.floor(shipEye.x),
                        (int) Math.floor(shipEye.y),
                        (int) Math.floor(shipEye.z)
                );
            } else {
                eyeBlock = BlockPos.containing(eyePos);
            }
        } else {
            eyeBlock = BlockPos.containing(eyePos);
        }

        return hermeticArea != null && hermeticArea.isHermetic() && hermeticArea.contains(eyeBlock);
    }


    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankLazyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        tankLazyOptional = LazyOptional.of(() -> tank);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("leftTicks", leftTicks);
        tag.putBoolean("temperatureRegulatorApplied", temperatureRegulatorApplied);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        leftTicks = tag.getInt("leftTicks");
        temperatureRegulatorApplied = tag.getBoolean("temperatureRegulatorApplied");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            VentTracker.registerVent(serverLevel, worldPosition);
            if (hermeticArea == null) {
                hermeticArea = new HermeticArea(serverLevel, worldPosition);
            }
            hermeticArea.markDirty();
            hermeticArea.bake(worldPosition);
            for (ServerPlayer player : serverLevel.players()) {
                VSCompat.applySealedEffects(player, worldPosition, hermeticArea, this);
            }

        }
    }


    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            VentTracker.unregisterVent(serverLevel, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            VentTracker.unregisterVent(serverLevel, worldPosition);
            hermeticArea.clear();
        }
    }

    public float getCurrentOxygenRate() {
        return hermeticArea == null ? 0f : hermeticArea.getBlocks().size() / (float) BOConfig.ventConsumption;
    }

    public boolean isBlockInsideSealedArea(BlockPos pos) {
        return hermeticArea != null && hermeticArea.isHermetic() && hermeticArea.contains(pos);
    }

    public HermeticArea getHermeticArea() {
        return this.hermeticArea;
    }
}
