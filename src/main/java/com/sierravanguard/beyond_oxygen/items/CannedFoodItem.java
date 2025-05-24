package com.sierravanguard.beyond_oxygen.items;

import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CannedFoodItem extends Item {
    public CannedFoodItem(Properties properties, Item returnItem) {
        super(properties);
        this.returnItem = returnItem;
    }
    private final Item returnItem;

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            // Check if player is wearing full spacesuit
            if (SpaceSuitHandler.isWearingFullSuit(player)) {
                // Allow eating canned food
                player.startUsingItem(hand);
                return InteractionResultHolder.success(stack);
            } else {
                player.displayClientMessage(Component.literal("You need to wear a full spacesuit to eat this!"), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32; // normal eating time
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack emptyCan = new ItemStack(returnItem);

            if (!player.getInventory().add(emptyCan)) {
                player.drop(emptyCan, false);
            }
        }

        return resultStack;
    }
}
