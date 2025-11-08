package com.sierravanguard.beyond_oxygen.blocks;

import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.blocks.entity.OxygenHarvesterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OxygenHarvesterBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public OxygenHarvesterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return BOBlockEntities.OXYGEN_HARVESTER.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == BOBlockEntities.OXYGEN_HARVESTER.get()
                ? (lvl, pos1, st, be) -> OxygenHarvesterBlockEntity.tick(lvl, pos1, st, be)
                : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OxygenHarvesterBlockEntity harvester) {
                player.displayClientMessage(
                        Component.literal(String.format("Harvesting %.2f mB per cycle", harvester.getOxygenHarvested())),
                        true
                );
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof OxygenHarvesterBlockEntity harvester) {
                harvester.invalidateCaps();
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }



}