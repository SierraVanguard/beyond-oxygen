package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOFluids;
import com.sierravanguard.beyond_oxygen.utils.HermeticArea;
import com.sierravanguard.beyond_oxygen.utils.HermeticAreaData;
import com.sierravanguard.beyond_oxygen.utils.HermeticAreaServerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OxygenHarvesterBlockEntity extends BlockEntity {

    private static final int ASSIGNMENT_INTERVAL = 120;
    private static final int ENERGY_COST_PER_HARVEST = 50;
    private static final int OXYGEN_PER_PLANT = 1;
    private static final int MAX_SCAN_PER_TICK = 32;

    private long savedAreaId = -1;
    private float oxygenLastHarvested = 0;
    private int assignmentCooldown = 0;
    private boolean isInHarvestCycle = false;

    private HermeticArea cachedSealedArea = null;
    private Iterator<BlockPos> scanIterator = null;

    private final FluidTank oxygenTank = new FluidTank(10000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return BOFluids.isOxygen(stack);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            if (level != null && !level.isClientSide)
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

        if (harvester.cachedSealedArea == null) {
            if (harvester.savedAreaId != -1) {
                HermeticArea area = HermeticAreaServerManager.getArea(serverLevel, pos, harvester.savedAreaId);
                if (area != null && area.isHermetic()) {
                    harvester.cachedSealedArea = area;
                }
            }
            if (harvester.cachedSealedArea == null) harvester.findAndAssignHermeticArea(serverLevel);
        }

        if (harvester.cachedSealedArea == null || !harvester.cachedSealedArea.isHermetic() || harvester.cachedSealedArea.isDirty()) {
            harvester.cachedSealedArea = null;
            harvester.savedAreaId = -1;
            return;
        }

        if (harvester.savedAreaId != harvester.cachedSealedArea.getId())
            harvester.savedAreaId = harvester.cachedSealedArea.getId();

        boolean canHarvest = harvester.energyStorage.getEnergyStored() >= ENERGY_COST_PER_HARVEST &&
                harvester.getOxygenTankSpace() >= OXYGEN_PER_PLANT;

        if (canHarvest || harvester.isInHarvestCycle) {
            harvester.harvest(serverLevel);
        } else {
            harvester.oxygenLastHarvested = 0;
            harvester.scanIterator = null;
            harvester.isInHarvestCycle = false;
        }
    }

    private void findAndAssignHermeticArea(ServerLevel level) {
        List<HermeticArea> areas = HermeticAreaData.get(level).getAreasAffecting(worldPosition);
        for (HermeticArea area : areas) {
            if (area.isHermetic() && area.getBlocks().contains(worldPosition)) {
                cachedSealedArea = area;
                savedAreaId = area.getId();
                break;
            }
        }
    }

    private void harvest(ServerLevel serverLevel) {
        if (cachedSealedArea == null) {
            findAndAssignHermeticArea(serverLevel);
            if (cachedSealedArea == null) return;
        }

        if (!cachedSealedArea.isHermetic() || cachedSealedArea.isDirty()) {
            cachedSealedArea = null;
            isInHarvestCycle = false;
            scanIterator = null;
            return;
        }

        if (scanIterator == null || !isInHarvestCycle) {
            List<BlockPos> blocks = new ArrayList<>(cachedSealedArea.getBlocks());
            scanIterator = blocks.iterator();
            isInHarvestCycle = true;
        }

        Optional<Fluid> oxygenFluid = BOFluids.getOxygenFluid();

        int oxygenHarvested = 0;
        int scanned = 0;

        if (oxygenFluid.isPresent()) {
            while (scanIterator.hasNext() && scanned < MAX_SCAN_PER_TICK &&
                    getOxygenTankSpace() >= OXYGEN_PER_PLANT &&
                    energyStorage.getEnergyStored() >= ENERGY_COST_PER_HARVEST) {

                BlockPos checkPos = scanIterator.next();
                var state = serverLevel.getBlockState(checkPos);
                if (state.is(BlockTags.LEAVES) || state.is(BlockTags.CROPS)) {
                    int filled = oxygenTank.fill(new FluidStack(oxygenFluid.get(), OXYGEN_PER_PLANT), IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) energyStorage.extractEnergy(ENERGY_COST_PER_HARVEST, false);
                    oxygenHarvested += filled;
                }
                scanned++;
            }
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
        if (cachedSealedArea != null) tag.putLong("savedAreaId", cachedSealedArea.getId());
        tag.put("oxygenTank", oxygenTank.writeToNBT(new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        oxygenTank.readFromNBT(tag.getCompound("oxygenTank"));
        energyStorage.receiveEnergy(tag.getInt("energy"), false);
        savedAreaId = tag.contains("savedAreaId") ? tag.getLong("savedAreaId") : -1;
        cachedSealedArea = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) findAndAssignHermeticArea(server);
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
