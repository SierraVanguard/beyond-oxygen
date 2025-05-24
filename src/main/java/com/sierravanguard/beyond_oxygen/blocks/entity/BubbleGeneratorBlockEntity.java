package com.sierravanguard.beyond_oxygen.blocks.entity;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sierravanguard.beyond_oxygen.BOConfig.bubbleMaxRadius;

public class BubbleGeneratorBlockEntity extends BlockEntity {
    private float lastSentRadius = -1f;
    private final FluidTank tank = new FluidTank(1000, fluid -> fluid.getFluid() == MekanismFluids.OXYGEN.getFluid());
    private LazyOptional<FluidTank> tankLazyOptional = LazyOptional.of(() -> tank);

    private int leftTicks = 0;
    public float currentRadius = 0.0f;
    public float maxRadius = bubbleMaxRadius;

    public BubbleGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BOBlockEntities.BUBBLE_GENERATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (level.isClientSide) return;

        BubbleGeneratorBlockEntity entity = (BubbleGeneratorBlockEntity) be;

        if (entity.tank.isEmpty()) {
            return;
        }

        if (entity.currentRadius < entity.maxRadius) {
            entity.currentRadius += 0.01f;
        }
        float oxygenNeeded = (BOConfig.ventConsumption * 2.0f);

        if (entity.consumeOxygen((int) oxygenNeeded)) {
            for (ServerPlayer player : ((ServerLevel) level).players()) {
                boolean vsLogicSuccess = BeyondOxygen.ModsLoaded.VS &&
                        VSCompat.applyBubbleEffects(player, pos, entity.currentRadius);
                if (!vsLogicSuccess) {
                    Vec3 eyePos = player.getEyePosition();
                    if (eyePos.distanceTo(Vec3.atCenterOf(pos)) <= entity.currentRadius * 2) {
                        player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
                    }
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
        if (tank.getFluidAmount() < amount) {
            return false;
        }
        tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }


    public float getCurrentRadius() {
        return currentRadius;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return tankLazyOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("ticks", leftTicks);
        tag.putFloat("radius", currentRadius);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        leftTicks = tag.getInt("ticks");
        currentRadius = tag.getFloat("radius");
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

    // --- Syncing methods ---

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putFloat("radius", currentRadius);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("radius")) {
            currentRadius = tag.getFloat("radius");
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
}
