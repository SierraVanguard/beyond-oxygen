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

    /** Returns true if player wears any full spacesuit (thermal, cryo, or regular) */
    public static boolean isWearingFullSuit(Player player) {
        return isSlotValid(player, EquipmentSlot.HEAD) &&
                isSlotValid(player, EquipmentSlot.CHEST) &&
                isSlotValid(player, EquipmentSlot.LEGS) &&
                isSlotValid(player, EquipmentSlot.FEET) &&
                isHelmetClosed(player);
    }

    /** Returns true if player wears a full cryo suit */
    public static boolean isWearingFullCryoSuit(Player player) {
        return isWearingSpecificSuit(player, BOConfig.CRYO_HELMETS.get(),
                BOConfig.CRYO_CHESTPLATES.get(),
                BOConfig.CRYO_LEGGINGS.get(),
                BOConfig.CRYO_BOOTS.get());
    }

    /** Returns true if player wears a full thermal suit */
    public static boolean isWearingFullThermalSuit(Player player) {
        return isWearingSpecificSuit(player, BOConfig.THERMAL_HELMETS.get(),
                BOConfig.THERMAL_CHESTPLATES.get(),
                BOConfig.THERMAL_LEGGINGS.get(),
                BOConfig.THERMAL_BOOTS.get());
    }

    /** Checks a suit with explicit component lists */
    private static boolean isWearingSpecificSuit(Player player,
                                                 List<? extends String> helmets,
                                                 List<? extends String> chestplates,
                                                 List<? extends String> leggings,
                                                 List<? extends String> boots) {
        return isSlotMatchingAny(player, EquipmentSlot.HEAD, helmets) &&
                isSlotMatchingAny(player, EquipmentSlot.CHEST, chestplates) &&
                isSlotMatchingAny(player, EquipmentSlot.LEGS, leggings) &&
                isSlotMatchingAny(player, EquipmentSlot.FEET, boots) &&
                isHelmetClosed(player);
    }

    private static boolean isSlotMatchingAny(Player player, EquipmentSlot slot, List<? extends String> validIds) {
        if (validIds == null || validIds.isEmpty()) return false;

        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) return false;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && validIds.contains(itemId.toString());
    }

    private static boolean isSlotValid(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) return false;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return false;
        switch (slot) {
            case HEAD -> {
                return BOConfig.getSpaceHelmets().stream().anyMatch(id -> id.equals(itemId.toString()));
            }
            case CHEST -> {
                return BOConfig.getSpaceChestplates().stream().anyMatch(id -> id.equals(itemId.toString()));
            }
            case LEGS -> {
                return BOConfig.getSpaceLeggings().stream().anyMatch(id -> id.equals(itemId.toString()));
            }
            case FEET -> {
                return BOConfig.getSpaceBoots().stream().anyMatch(id -> id.equals(itemId.toString()));
            }
            default -> {
                return false;
            }
        }
    }

    /** Returns true if helmet is closed or non-openable */
    public static boolean isHelmetClosed(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) return false;
        if (!(helmet.getItem() instanceof OpenableSpacesuitHelmetItem)) return true;

        return player.getCapability(BOCapabilities.HELMET_STATE)
                .map(state -> !state.isOpen())
                .orElse(false);
    }
}

