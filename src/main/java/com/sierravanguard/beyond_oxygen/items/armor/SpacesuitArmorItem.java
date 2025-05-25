package com.sierravanguard.beyond_oxygen.items.armor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class SpacesuitArmorItem extends ArmorItem {
    public SpacesuitArmorItem(ArmorMaterial material, Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    public boolean isHelmet() {
        return this.getType() == Type.HELMET;
    }
    public boolean canOpenHelmet() {
        return false;
    }
    public boolean isOpenableHelmet() {
        return this instanceof OpenableSpacesuitHelmetItem;
    }
}
