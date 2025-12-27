package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BOServerConfig;
import com.sierravanguard.beyond_oxygen.compat.CompatUtils;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.registry.BOFluids;
import com.sierravanguard.beyond_oxygen.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VentBlockEntity extends BlockEntity {
    private boolean pendingAreaClear = false;
    private static final long UNASSIGNED_AREA = Long.MIN_VALUE;
    private boolean isCenterVent = false;
    private HermeticArea hermeticArea;
    private long savedAreaId = UNASSIGNED_AREA;
    private boolean initialized = false;
    public int temperatureRegulatorCooldown = 0;
    public boolean temperatureRegulatorApplied = false;
    private int reattachCooldown = 0;
    private int ventConsumption;


    private final FluidTank tank = new FluidTank(1000, BOFluids::isOxygen);
    private LazyOptional<FluidTank> tankCap = LazyOptional.of(() -> tank);

    public VentBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.VENT_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return tankCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        tankCap = LazyOptional.of(() -> tank);
    }
    public void refreshConfigValues() {
        ventConsumption = Math.max(1, BOServerConfig.getVentConsumption());
    }
    private boolean consumeOxygen(int amount) {
        int available = tank.getFluidAmount();
        if (available < amount) return false;
        tank.drain(amount, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean isEntityInsideHermeticArea(LivingEntity entity) {
        return hermeticArea != null && HermeticAreaManager.updateIsEntityInHermeticArea(entity, hermeticArea);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putLong("areaId", hermeticArea != null
                ? hermeticArea.getId()
                : savedAreaId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        savedAreaId = tag.getLong("areaId");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide || !(level instanceof ServerLevel server)) return;
        refreshConfigValues();
        ensureHermeticArea(server);
        initialized = true;
    }


    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!(level instanceof ServerLevel server)) return;
        if (hermeticArea != null) {
            hermeticArea.removeVent(worldPosition, false);

        }
    }


    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!(level instanceof ServerLevel server)) return;
        if (hermeticArea != null) {
            hermeticArea.removeVent(worldPosition, true);
            hermeticArea = null;
        }
    }


    public float getCurrentOxygenRate() {
        return hermeticArea == null ? 0f : hermeticArea.getBlocks().size() / (float) BOServerConfig.getVentConsumption();
    }

    public boolean isBlockInsideSealedArea(BlockPos pos) {
        return hermeticArea != null && hermeticArea.isHermetic() && hermeticArea.contains(pos);
    }

    public HermeticArea getHermeticArea() {
        return hermeticArea;
    }

    public void setHermeticArea(HermeticArea area) {
        if (area == null) {
            pendingAreaClear = true;
            savedAreaId = UNASSIGNED_AREA;
            reattachCooldown = 20; 
        } else {
            this.hermeticArea = area;
            pendingAreaClear = false;
            savedAreaId = area.getId();
            reattachCooldown = 0;
        }

    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (level.isClientSide) return;
        VentBlockEntity vent = (VentBlockEntity) be;
        if (!vent.initialized) return;
        if (vent.reattachCooldown > 0) {
            vent.reattachCooldown--;
            return;
        }
        ServerLevel server = (ServerLevel) level;
        if (!vent.ensureHermeticArea(server)) return;
        if (vent.hermeticArea == null) return;
        if (vent.hermeticArea.isDirty() && vent.isCenterVent) {
            vent.hermeticArea.bake();
        }
        if (vent.temperatureRegulatorCooldown > 0)
            vent.temperatureRegulatorCooldown--;
        boolean hasAir;
        //System.out.println("Area hermetic? " + vent.hermeticArea.isHermetic());
        if (vent.hermeticArea.isHermetic()) {
            int oxygenNeeded = Math.max(1, vent.hermeticArea.getBlocks().size() / vent.ventConsumption);
            if (vent.temperatureRegulatorApplied) oxygenNeeded /= 2;
            hasAir = vent.consumeOxygen(oxygenNeeded) || BOConfig.getBabyMode();
        } else hasAir = false;

        vent.hermeticArea.setHasAir(hasAir);
        //System.out.println("Area has air? " + hasAir);
        if (hasAir && vent.hermeticArea.getBounds() != null) level.getEntities((Entity) null, vent.hermeticArea.getBounds(), e -> e instanceof LivingEntity).forEach(entity -> {
            LivingEntity living = (LivingEntity) entity;
            if (vent.isEntityInsideHermeticArea(living)) {
                living.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), BOConfig.getTimeToImplode(), 0, false, false));
                living.setAirSupply(living.getMaxAirSupply());
                if (vent.temperatureRegulatorApplied) CompatUtils.setComfortableTemperature(living);
            }
        });

 
        if (vent.pendingAreaClear) {
            vent.hermeticArea = null;
            vent.pendingAreaClear = false;
        }
    }

    private boolean ensureHermeticArea(ServerLevel server) {
        if (hermeticArea != null) return true;

 
        if (savedAreaId != UNASSIGNED_AREA) {
            hermeticArea = HermeticAreaServerManager.getArea(server, worldPosition, savedAreaId);
            if (hermeticArea != null) {
                hermeticArea.addVent(this);
                isCenterVent = hermeticArea.getCenterPos().equals(worldPosition);
                return true;
            }
        }

 
        HermeticAreaData data = HermeticAreaData.get(server);
        for (HermeticArea area : data.getAreasAffecting(worldPosition)) {
            if (area.maybeContains(worldPosition)) {
                hermeticArea = area;
                hermeticArea.addVent(this);
                isCenterVent = false;
                savedAreaId = area.getId();
                return true;
            }
        }

 
        hermeticArea = HermeticAreaServerManager.getArea(server, worldPosition, UNASSIGNED_AREA);
        if (hermeticArea != null) {
            hermeticArea.addVent(this);
            isCenterVent = true;
            savedAreaId = hermeticArea.getId();
            return true;
        }

        return false;
    }

    public void setCenterVent(boolean val) {
        this.isCenterVent = val;
    }


}
