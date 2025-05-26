package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.VentBlock;
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
    private int checkTick = 0;
    public final HermeticArea hermeticArea = new HermeticArea();

    public VentBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BOBlockEntities.VENT_BLOCK_ENTITY.get(), p_155229_, p_155230_);
        loadAcceptedFluidsFromConfig(BOConfig.oxygenFluids);
    }

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

    private LazyOptional<FluidTank> tankLazyOptional = LazyOptional.of(()->tank);
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER) return tankLazyOptional.cast();
        return super.getCapability(cap, side);
    }

    private boolean consumeOxygen(int amount){
        int total = tank.getFluidAmount()* BOConfig.oxygenConsumption + leftTicks;
        if(total < amount) return false;
        total = total-amount;
        tank.getFluid().setAmount(total/10);
        leftTicks = total % 10;
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity t) {
        if(level.isClientSide) return;
        VentBlockEntity entity = (VentBlockEntity) t;
        Direction dir = state.getValue(VentBlock.FACING);
        if(entity.checkTick <= 0){
            entity.hermeticArea.bakeArea((ServerLevel) level, pos.offset(dir.getNormal()),dir.getOpposite());
            entity.checkTick = 60;
        }
        entity.checkTick--;
        if(entity.tank.isEmpty()) return;
        List<ServerPlayer> players = ((ServerLevel)level).players();
        if(entity.hermeticArea.isHermetic()&& entity.consumeOxygen(entity.hermeticArea.getArea().size()/ BOConfig.ventConsumption)) {
            for (ServerPlayer player : players) {
                boolean vsLogicSuccess = BeyondOxygen.ModsLoaded.VS && VSCompat.applySealedEffects(player, pos, entity.hermeticArea);
                if(!vsLogicSuccess){
                    Vec3 eyePos = player.getEyePosition();
                    BlockPos eyeBlockPos = new BlockPos((int)Math.floor(eyePos.x),(int)Math.floor(eyePos.y),(int)Math.floor(eyePos.z));
                    if(entity.hermeticArea.getArea().contains(eyeBlockPos)) player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
                }
            }
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankLazyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        tankLazyOptional = LazyOptional.of(()->tank);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("ticks", leftTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT((CompoundTag) tag.get("tank"));
        leftTicks = tag.getInt("ticks");
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            VentTracker.registerVent(serverLevel, this.getBlockPos());
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            VentTracker.unregisterVent(serverLevel, this.getBlockPos());
        }
    }
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!this.level.isClientSide && this.level instanceof ServerLevel serverLevel) {
            VentTracker.unregisterVent(serverLevel, this.getBlockPos());
        }
    }
    public float getCurrentOxygenRate() {
        int areaSize = hermeticArea.getArea().size();
        return areaSize / (float) BOConfig.ventConsumption;
    }

}
