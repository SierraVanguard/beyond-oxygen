package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.utils.VentTracker;
import com.sierravanguard.beyond_oxygen.utils.HermeticArea;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OxygenHarvesterBlockEntity extends BlockEntity {
    private static final int ASSIGNMENT_INTERVAL = 120; // ticks (6 seconds)
    private static final int ENERGY_COST_PER_HARVEST = 50;
    private static final int OXYGEN_PER_PLANT = 10;
    private static final int MAX_SCAN_PER_TICK = 32;

    private float oxygenLastHarvested = 0;
    private int assignmentCooldown = 0;
    private BlockPos assignedVentPos = null;
    private HermeticArea cachedSealedArea = null;
    private BlockPos lastScanPos = null;

    // Initialize oxygen tank with proper capacity and validator
    private final FluidTank oxygenTank = new FluidTank(10000, fluidStack -> {
        for (var fluidId : BOConfig.oxygenFluids) {
            if (fluidStack.getFluid() == ForgeRegistries.FLUIDS.getValue(fluidId)) {
                return true;
            }
        }
        return false;
    }) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<FluidTank> tankLazyOptional = LazyOptional.of(() -> oxygenTank);

    private final EnergyStorage energyStorage = new EnergyStorage(100000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                setChanged();
            }
            return extracted;
        }
    };

    private LazyOptional<EnergyStorage> energyLazyOptional = LazyOptional.of(() -> energyStorage);

    public OxygenHarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.OXYGEN_HARVESTER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (level.isClientSide) return;
        if (!(be instanceof OxygenHarvesterBlockEntity harvester)) return;

        // Handle vent assignment
        if (harvester.assignmentCooldown-- <= 0 || harvester.cachedSealedArea == null || !harvester.cachedSealedArea.isHermetic()) {
            harvester.assignmentCooldown = ASSIGNMENT_INTERVAL;
            harvester.findAndAssignVent((ServerLevel) level);
        }

        // Only harvest if we have a valid area, enough energy, and tank space
        if (harvester.cachedSealedArea != null &&
                harvester.energyStorage.getEnergyStored() >= ENERGY_COST_PER_HARVEST &&
                harvester.oxygenTank.getSpace() >= OXYGEN_PER_PLANT) {
            harvester.harvest((ServerLevel) level);
        } else {
            harvester.oxygenLastHarvested = 0;
        }
    }

    private void findAndAssignVent(ServerLevel serverLevel) {
        this.assignedVentPos = null;
        this.cachedSealedArea = null;
        this.lastScanPos = null;

        for (BlockPos ventPos : VentTracker.getVentsIn(serverLevel)) {
            BlockEntity ventBE = serverLevel.getBlockEntity(ventPos);
            if (!(ventBE instanceof VentBlockEntity vent)) continue;

            HermeticArea area = vent.hermeticArea;
            if (area != null && area.isHermetic() && area.getArea().contains(this.worldPosition)) {
                this.assignedVentPos = ventPos;
                this.cachedSealedArea = area;
                break;
            }
        }
    }

    private void harvest(ServerLevel serverLevel) {
        if (cachedSealedArea == null || oxygenTank.getSpace() < OXYGEN_PER_PLANT) {
            oxygenLastHarvested = 0;
            return;
        }

        int oxygenHarvested = 0;
        int blocksScanned = 0;
        var area = cachedSealedArea.getArea();
        var iterator = area.iterator();

        // Resume scanning from last position
        if (lastScanPos != null) {
            while (iterator.hasNext() && !iterator.next().equals(lastScanPos)) {
                // Fast-forward to last position
            }
        }

        while (iterator.hasNext() && blocksScanned < MAX_SCAN_PER_TICK &&
                oxygenHarvested < oxygenTank.getSpace() &&
                energyStorage.getEnergyStored() >= ENERGY_COST_PER_HARVEST) {

            BlockPos checkPos = iterator.next();
            BlockState state = serverLevel.getBlockState(checkPos);

            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.CROPS)) {
                // Only harvest if we can store the oxygen
                if (oxygenTank.getSpace() >= OXYGEN_PER_PLANT) {
                    var oxygenFluid = ForgeRegistries.FLUIDS.getValue(BOConfig.oxygenFluids.get(0));
                    if (oxygenFluid != null) {
                        oxygenTank.fill(new FluidStack(oxygenFluid, OXYGEN_PER_PLANT), IFluidHandler.FluidAction.EXECUTE);
                        oxygenHarvested += OXYGEN_PER_PLANT;
                        energyStorage.extractEnergy(ENERGY_COST_PER_HARVEST, false);
                    }
                }
            }

            blocksScanned++;
            lastScanPos = checkPos;
        }

        oxygenLastHarvested = oxygenHarvested;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return tankLazyOptional.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankLazyOptional.invalidate();
        energyLazyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        tankLazyOptional = LazyOptional.of(() -> oxygenTank);
        energyLazyOptional = LazyOptional.of(() -> energyStorage);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("oxygenTank", oxygenTank.writeToNBT(new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
        if (assignedVentPos != null) {
            tag.putLong("assignedVentPos", assignedVentPos.asLong());
        }
        if (lastScanPos != null) {
            tag.putLong("lastScanPos", lastScanPos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        oxygenTank.readFromNBT(tag.getCompound("oxygenTank"));
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
        if (tag.contains("assignedVentPos")) {
            assignedVentPos = BlockPos.of(tag.getLong("assignedVentPos"));
        }
        if (tag.contains("lastScanPos")) {
            lastScanPos = BlockPos.of(tag.getLong("lastScanPos"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("oxygenTank", oxygenTank.writeToNBT(new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        oxygenTank.readFromNBT(tag.getCompound("oxygenTank"));
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    public float getOxygenHarvesting() {
        return oxygenLastHarvested;
    }

    public FluidTank getOxygenTank() {
        return oxygenTank;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}