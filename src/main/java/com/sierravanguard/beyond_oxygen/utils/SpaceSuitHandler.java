package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpaceSuitHandler {

    
    public static boolean isWearingFullSuit(Player player) {
        return isWearing(player,
                BOItemTags.SPACE_SUIT_HELMETS,
                BOItemTags.SPACE_SUIT_CHESTPLATES,
                BOItemTags.SPACE_SUIT_LEGGINGS,
                BOItemTags.SPACE_SUIT_BOOTS) &&
                isHelmetClosed(player);
    }

    
    public static boolean isWearingFullCryoSuit(Player player) {
        return isWearing(player,
                BOItemTags.CRYO_HELMETS,
                BOItemTags.CRYO_CHESTPLATES,
                BOItemTags.CRYO_LEGGINGS,
                BOItemTags.CRYO_BOOTS) &&
                isHelmetClosed(player);
    }

    
    public static boolean isWearingFullThermalSuit(Player player) {
        return isWearing(player,
                BOItemTags.THERMAL_HELMETS,
                BOItemTags.THERMAL_CHESTPLATES,
                BOItemTags.THERMAL_LEGGINGS,
                BOItemTags.THERMAL_BOOTS) &&
                isHelmetClosed(player);
    }

    public static boolean isWearing(Player player, TagKey<Item> head, TagKey<Item> chest, TagKey<Item> legs, TagKey<Item> feet) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(head) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(chest) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(legs) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(feet);
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
