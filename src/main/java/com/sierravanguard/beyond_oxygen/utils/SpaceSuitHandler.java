package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.capabilities.HelmetState;
import com.sierravanguard.beyond_oxygen.items.armor.IOpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

public class SpaceSuitHandler {

    
    public static boolean isWearingFullSuit(LivingEntity entity) {
        return isWearing(entity,
                BOItemTags.SPACE_SUIT_HELMETS,
                BOItemTags.SPACE_SUIT_CHESTPLATES,
                BOItemTags.SPACE_SUIT_LEGGINGS,
                BOItemTags.SPACE_SUIT_BOOTS) &&
                isHelmetClosed(entity);
    }

    
    public static boolean isWearingFullCryoSuit(LivingEntity entity) {
        return isWearing(entity,
                BOItemTags.CRYO_HELMETS,
                BOItemTags.CRYO_CHESTPLATES,
                BOItemTags.CRYO_LEGGINGS,
                BOItemTags.CRYO_BOOTS) &&
                isHelmetClosed(entity);
    }

    
    public static boolean isWearingFullThermalSuit(LivingEntity entity) {
        return isWearing(entity,
                BOItemTags.THERMAL_HELMETS,
                BOItemTags.THERMAL_CHESTPLATES,
                BOItemTags.THERMAL_LEGGINGS,
                BOItemTags.THERMAL_BOOTS) &&
                isHelmetClosed(entity);
    }

    public static boolean isWearing(LivingEntity entity, TagKey<Item> head, TagKey<Item> chest, TagKey<Item> legs, TagKey<Item> feet) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(head) &&
                entity.getItemBySlot(EquipmentSlot.CHEST).is(chest) &&
                entity.getItemBySlot(EquipmentSlot.LEGS).is(legs) &&
                entity.getItemBySlot(EquipmentSlot.FEET).is(feet);
    }

    
    public static boolean isHelmetClosed(LivingEntity entity) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) return false;
        else if (!(helmet.getItem() instanceof IOpenableSpacesuitHelmetItem)) return true;
        else {
            LazyOptional<HelmetState> cap = HelmetState.get(entity);
            return cap.isPresent() && !cap.resolve().get().isOpen();
        }
    }
}
