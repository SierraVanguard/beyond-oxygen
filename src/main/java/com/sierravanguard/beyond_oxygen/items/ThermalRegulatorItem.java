package com.sierravanguard.beyond_oxygen.items;

import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class ThermalRegulatorItem extends Item {

    public ThermalRegulatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        InteractionHand hand = context.getHand();

        if (!level.isClientSide && player != null && player.isShiftKeyDown()) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof BubbleGeneratorBlockEntity bubbleGen) {
                if (!bubbleGen.temperatureRegulatorApplied) {
                    bubbleGen.temperatureRegulatorApplied = true;
                    bubbleGen.setChanged();
                    stack.shrink(1);
                    player.displayClientMessage(Component.literal("Thermal regulator applied."), true);
                } else {
                    player.displayClientMessage(Component.literal("Thermal regulator already applied."), true);
                }
                return InteractionResult.SUCCESS;
            }

            if (be instanceof VentBlockEntity vent) {
                if (!vent.temperatureRegulatorApplied) {
                    vent.temperatureRegulatorApplied = true;
                    vent.setChanged();
                    stack.shrink(1);
                    player.displayClientMessage(Component.literal("Thermal regulator applied."), true);
                } else {
                    player.displayClientMessage(Component.literal("Thermal regulator already applied."), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
