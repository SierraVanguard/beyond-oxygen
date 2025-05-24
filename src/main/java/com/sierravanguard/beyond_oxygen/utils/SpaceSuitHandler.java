package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SpaceSuitHandler {
    public static boolean isWearingFullSuit(Player player) {
        return isSlotValid(player, EquipmentSlot.HEAD) &&
                isSlotValid(player, EquipmentSlot.CHEST) &&
                isSlotValid(player, EquipmentSlot.LEGS) &&
                isSlotValid(player, EquipmentSlot.FEET);
    }
    private static boolean isSlotValid(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) return false;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;

        return BOConfig.isItemValidForSlot(itemId, slot);
    }
}
