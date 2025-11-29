package com.sierravanguard.beyond_oxygen.items.armor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IOpenableSpacesuitHelmetItem {
    default boolean canOpenHelmet(LivingEntity entity, ItemStack stack) {
        return true;
    }
}
