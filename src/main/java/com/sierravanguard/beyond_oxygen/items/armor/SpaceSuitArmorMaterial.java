package com.sierravanguard.beyond_oxygen.items.armor;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public enum SpaceSuitArmorMaterial implements ArmorMaterial {
    SPACESUIT("spacesuit", 10, new int[]{4, 6, 4, 3}, 22, SoundEvents.ARMOR_EQUIP_LEATHER, 0f, 0f,
            () -> Ingredient.of(BOItemTags.SPACE_SUIT_REPAIR_MATERIAL)),

    CRYO_SUIT("cryo_suit", 12, new int[]{5, 7, 5, 3}, 25, SoundEvents.ARMOR_EQUIP_CHAIN, 1.0f, 0.1f,
            () -> Ingredient.of(BOItemTags.CRYO_REPAIR_MATERIAL)),

    THERMAL_SUIT("thermal_suit", 13, new int[]{5, 7, 5, 3}, 25, SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 0.1f,
            () -> Ingredient.of(BOItemTags.THERMAL_REPAIR_MATERIAL));

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    SpaceSuitArmorMaterial(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantmentValue,
                           SoundEvent equipSound, float toughness, float knockbackResistance,
                           Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    private static final int[] DURABILITY = {13, 15, 16, 11}; 
    private static final int[] DEFENSE = {1, 3, 4, 2};

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILITY[type.ordinal()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DEFENSE[type.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return BeyondOxygen.MODID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
