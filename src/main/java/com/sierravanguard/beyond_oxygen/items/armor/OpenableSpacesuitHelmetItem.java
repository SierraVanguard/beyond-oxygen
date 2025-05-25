package com.sierravanguard.beyond_oxygen.items.armor;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class OpenableSpacesuitHelmetItem extends SpacesuitArmorItem {
    private final String textureName;

    public OpenableSpacesuitHelmetItem(ArmorMaterial material, Type type, Item.Properties properties, String textureName) {
        super(material, type, properties);
        this.textureName = textureName;
    }

    @Override
    public boolean canOpenHelmet() {
        return true;
    }
    public String getTextureName() {
        return textureName;
    }
}
