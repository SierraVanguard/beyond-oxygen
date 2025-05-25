package com.sierravanguard.beyond_oxygen.client.menu;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.registry.BOMenus;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BubbleGeneratorMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final Level level;
    private final BubbleGeneratorBlockEntity blockEntity;

    public double maxRadius = 10.0;
    public BubbleGeneratorMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, buffer.readBlockPos());
    }
    public BubbleGeneratorMenu(int id, Inventory playerInventory, BlockPos pos) {
        super(BOMenus.BUBBLE_GENERATOR.get(), id);
        this.blockPos = pos;
        this.level = playerInventory.player.level();

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BubbleGeneratorBlockEntity generator) {
            this.blockEntity = generator;
        } else {
            this.blockEntity = null; // Prevent crashes, but you should log this
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.blockPosition().closerThan(blockPos, 8)) {
            return true;
        }
        if (BeyondOxygen.ModsLoaded.VS) {
            return VSCompat.isWithinShipRadius(level, player, blockPos, 8);
        }

        return false;
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public double getCurrentRadius() {
        return blockEntity != null ? blockEntity.getCurrentRadius() : 0.0;
    }

    public int getPowerLevel() {
        return blockEntity != null ? blockEntity.getPowerLevel() : 0;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Level getLevel() {
        return level;
    }

    public double getOxygenRatio() {
    return this.blockEntity.getOxygenRatio();
    }
}
