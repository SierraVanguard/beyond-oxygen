package com.sierravanguard.beyond_oxygen.blocks;

import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class BubbleGeneratorBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public BubbleGeneratorBlock(Properties properties) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BubbleGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BOBlockEntities.BUBBLE_GENERATOR.get() ? BubbleGeneratorBlockEntity::tick : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BubbleGeneratorBlockEntity generator) {
                ItemStack held = player.getItemInHand(hand);
                boolean isEmptyHand = held.isEmpty();
                boolean isShift = player.isShiftKeyDown();
                if (generator.temperatureRegulatorCooldown > 0) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Please wait before interacting again."), true
                    );
                    return InteractionResult.SUCCESS;
                }
                if (isShift && isEmptyHand && generator.temperatureRegulatorApplied) {
                    generator.temperatureRegulatorApplied = false;
                    generator.temperatureRegulatorCooldown = 40;
                    generator.setChanged();

                    ItemStack regulator = new ItemStack(BOItems.THERMAL_REGULATOR.get());
                    if (!player.getInventory().add(regulator)) player.drop(regulator, false);

                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Thermal regulator removed."), true
                    );
                    return InteractionResult.SUCCESS;
                }
                if (!held.isEmpty() && held.is(BOItems.THERMAL_REGULATOR.get())) return InteractionResult.PASS;
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, generator, pos);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BubbleGeneratorBlockEntity generator) {
                if (!player.isCreative()) {
                    dropResources(state, level, pos, blockEntity);
                    if (generator.temperatureRegulatorApplied) {
                        ItemStack regulator = new ItemStack(BOItems.THERMAL_REGULATOR.get());
                        popResource(level, pos, regulator);
                    }
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BubbleGeneratorBlockEntity generator) {
                generator.invalidateCaps();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

}
