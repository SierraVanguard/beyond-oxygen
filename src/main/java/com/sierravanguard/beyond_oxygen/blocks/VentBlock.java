package com.sierravanguard.beyond_oxygen.blocks;

import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.network.VentInfoMessage;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VentBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public VentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return BOBlockEntities.VENT_BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BOBlockEntities.VENT_BLOCK_ENTITY.get() ? VentBlockEntity::tick : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VentBlockEntity vent) {
                ItemStack held = player.getItemInHand(hand);
                boolean isEmptyHand = held.isEmpty();
                boolean isShift = player.isShiftKeyDown();

                if (vent.temperatureRegulatorCooldown > 0) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Please wait before interacting again."), true);
                    return InteractionResult.SUCCESS;
                }

                if (isShift && isEmptyHand && vent.temperatureRegulatorApplied) {
                    vent.temperatureRegulatorApplied = false;
                    vent.temperatureRegulatorCooldown = 40;
                    vent.setChanged();

                    ItemStack regulator = new ItemStack(BOItems.THERMAL_REGULATOR.get());
                    if (!player.getInventory().add(regulator)) {
                        player.drop(regulator, false);
                    }

                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Thermal regulator removed."), true);
                    return InteractionResult.SUCCESS;
                }
                if (!held.isEmpty() && held.is(BOItems.THERMAL_REGULATOR.get())) {
                    return InteractionResult.PASS;
                }
                boolean sealed = vent.hermeticArea.isHermetic();
                float oxygenRate = sealed ? vent.getCurrentOxygenRate() : 0f;

                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new VentInfoMessage(sealed, oxygenRate)
                );

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof VentBlockEntity vent) {
                if (vent.temperatureRegulatorApplied) {
                    ItemStack regulator = new ItemStack(BOItems.THERMAL_REGULATOR.get());
                    popResource(level, pos, regulator);
                }
                dropResources(state, level, pos, blockEntity);
                level.removeBlockEntity(pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
