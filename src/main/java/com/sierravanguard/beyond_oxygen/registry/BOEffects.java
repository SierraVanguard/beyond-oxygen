package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.effects.OxygenMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BOEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BeyondOxygen.MODID);

    public static final RegistryObject<MobEffect> OXYGEN_SATURATION = EFFECTS.register("oxygen_saturation", OxygenMobEffect::new);
}
