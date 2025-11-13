package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class SpaceSuitHandler {

    
    public static boolean isWearingFullSuit(Player player) {
        return isSlotValid(player, EquipmentSlot.HEAD, BOConfig.getSpaceHelmets()) &&
                isSlotValid(player, EquipmentSlot.CHEST, BOConfig.getSpaceChestplates()) &&
                isSlotValid(player, EquipmentSlot.LEGS, BOConfig.getSpaceLeggings()) &&
                isSlotValid(player, EquipmentSlot.FEET, BOConfig.getSpaceBoots()) &&
                isHelmetClosed(player);
    }

    
    public static boolean isWearingFullCryoSuit(Player player) {
        return isSlotValid(player, EquipmentSlot.HEAD, BOConfig.getCryoHelmets()) &&
                isSlotValid(player, EquipmentSlot.CHEST, BOConfig.getCryoChestplates()) &&
                isSlotValid(player, EquipmentSlot.LEGS, BOConfig.getCryoLeggings()) &&
                isSlotValid(player, EquipmentSlot.FEET, BOConfig.getCryoBoots()) &&
                isHelmetClosed(player);
    }

    
    public static boolean isWearingFullThermalSuit(Player player) {
        return isSlotValid(player, EquipmentSlot.HEAD, BOConfig.getThermalHelmets()) &&
                isSlotValid(player, EquipmentSlot.CHEST, BOConfig.getThermalChestplates()) &&
                isSlotValid(player, EquipmentSlot.LEGS, BOConfig.getThermalLeggings()) &&
                isSlotValid(player, EquipmentSlot.FEET, BOConfig.getThermalBoots()) &&
                isHelmetClosed(player);
    }

    
    private static boolean isSlotValid(Player player, EquipmentSlot slot, List<ResourceLocation> validList) {
        if (validList == null || validList.isEmpty()) return false;

        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) return false;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && validList.contains(itemId);
    }

    
    public static boolean isHelmetClosed(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) return false;
        if (!(helmet.getItem() instanceof OpenableSpacesuitHelmetItem)) return true;

        return player.getCapability(BOCapabilities.HELMET_STATE)
                .map(state -> !state.isOpen())
                .orElse(false);
    }
}
