package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.utils.HermeticArea;
import com.sierravanguard.beyond_oxygen.utils.VentTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

public class OxygenHarvesterBlockEntity extends BlockEntity {
    private static final int ASSIGNMENT_INTERVAL = 120;
    private static final int ENERGY_COST_PER_HARVEST = 50;
    private static final int OXYGEN_PER_PLANT = 1;
    private static final int MAX_SCAN_PER_TICK = 32;

    private float oxygenLastHarvested = 0;
    private int assignmentCooldown = 0;

    private BlockPos assignedVentPos = null;
    private HermeticArea cachedSealedArea = null;
    private Iterator<BlockPos> scanIterator = null;
    private boolean isInHarvestCycle = false;

    private final FluidTank oxygenTank = new FluidTank(10000, stack -> BOConfig.oxygenFluids.stream()
            .map(ForgeRegistries.FLUIDS::getValue)
            .anyMatch(f -> f == stack.getFluid())) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };
    private LazyOptional<FluidTank> tankOptional = LazyOptional.of(() -> oxygenTank);

    private final EnergyStorage energyStorage = new EnergyStorage(100000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int r = super.receiveEnergy(maxReceive, simulate);
            if (r > 0 && !simulate) setChanged();
            return r;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int e = super.extractEnergy(maxExtract, simulate);
            if (e > 0 && !simulate) setChanged();
            return e;
        }
    };
    private LazyOptional<EnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);

    public OxygenHarvesterBlockEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(BOBlockEntities.OXYGEN_HARVESTER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, BlockEntity be) {
        if (level.isClientSide || !(be instanceof OxygenHarvesterBlockEntity harvester)) return;
        ServerLevel serverLevel = (ServerLevel) level;

 
        if (harvester.assignmentCooldown-- <= 0 ||
                harvester.cachedSealedArea == null ||
                !harvester.cachedSealedArea.isHermetic() ||
                harvester.cachedSealedArea.isDirty()) {

            harvester.assignmentCooldown = ASSIGNMENT_INTERVAL;
            harvester.findAndAssignVent(serverLevel);
            harvester.scanIterator = null;
        }

        boolean canHarvest = harvester.cachedSealedArea != null &&
                harvester.cachedSealedArea.isHermetic() &&
                harvester.energyStorage.getEnergyStored() >= ENERGY_COST_PER_HARVEST &&
                harvester.getOxygenTankSpace() >= OXYGEN_PER_PLANT;

        if (canHarvest || harvester.isInHarvestCycle) {
            harvester.harvest(serverLevel);
        } else {
            harvester.oxygenLastHarvested = 0;
            harvester.scanIterator = null;
            harvester.isInHarvestCycle = false;
        }
    }

    private void findAndAssignVent(ServerLevel serverLevel) {
        assignedVentPos = null;
        cachedSealedArea = null;

        for (BlockPos ventPos : VentTracker.getVentsIn(serverLevel)) {
            BlockEntity be = serverLevel.getBlockEntity(ventPos);
            if (!(be instanceof VentBlockEntity vent)) continue;

            HermeticArea area = vent.getHermeticArea();
            if (area != null && area.isHermetic() && area.getBlocks().contains(this.worldPosition)) {
                assignedVentPos = ventPos;
                cachedSealedArea = area;
                break;
            }
        }
    }

    private void harvest(ServerLevel serverLevel) {
        if (cachedSealedArea == null || getOxygenTankSpace() < OXYGEN_PER_PLANT || BOConfig.oxygenFluids.isEmpty()) {
            oxygenLastHarvested = 0;
            isInHarvestCycle = false;
            scanIterator = null;
            return;
        }

        if (scanIterator == null || !isInHarvestCycle) {
            scanIterator = new ArrayList<>(cachedSealedArea.getBlocks()).iterator();
            isInHarvestCycle = true;
        }

        var oxygenFluid = ForgeRegistries.FLUIDS.getValue(BOConfig.oxygenFluids.get(0));
        if (oxygenFluid == null) return;

        int oxygenHarvested = 0;
        int scanned = 0;

        while (scanIterator.hasNext() && scanned < MAX_SCAN_PER_TICK &&
                getOxygenTankSpace() >= OXYGEN_PER_PLANT &&
                energyStorage.getEnergyStored() >= ENERGY_COST_PER_HARVEST) {

            BlockPos checkPos = scanIterator.next();
            var state = serverLevel.getBlockState(checkPos);

            if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.CROPS)) {
                oxygenTank.fill(new net.minecraftforge.fluids.FluidStack(oxygenFluid, OXYGEN_PER_PLANT), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                energyStorage.extractEnergy(ENERGY_COST_PER_HARVEST, false);
                oxygenHarvested += OXYGEN_PER_PLANT;
            }

            scanned++;
        }

        if (!scanIterator.hasNext()) {
            scanIterator = null;
            isInHarvestCycle = false;
        }

        oxygenLastHarvested = oxygenHarvested;
    }

    private int getOxygenTankSpace() {
        return oxygenTank.getCapacity() - oxygenTank.getFluidAmount();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return tankOptional.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankOptional.invalidate();
        energyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        tankOptional = LazyOptional.of(() -> oxygenTank);
        energyOptional = LazyOptional.of(() -> energyStorage);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("oxygenTank", oxygenTank.writeToNBT(new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
        if (assignedVentPos != null) tag.putLong("assignedVentPos", assignedVentPos.asLong());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        oxygenTank.readFromNBT(tag.getCompound("oxygenTank"));
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
        if (tag.contains("assignedVentPos")) assignedVentPos = BlockPos.of(tag.getLong("assignedVentPos"));
    }

    public float getOxygenHarvested() {
        return oxygenLastHarvested;
    }

    public FluidTank getOxygenTank() {
        return oxygenTank;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
