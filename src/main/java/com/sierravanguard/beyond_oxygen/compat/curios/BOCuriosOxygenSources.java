package com.sierravanguard.beyond_oxygen.compat.curios;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.registry.BOOxygenSources;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import com.sierravanguard.beyond_oxygen.utils.OxygenSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BOCuriosOxygenSources {
    public static final int DEFAULT_PRIORITY_CURIOS = 4000;

    private static DeferredRegister<OxygenSource<?>> registry = DeferredRegister.create(BOOxygenSources.REGISTRY_KEY, BeyondOxygen.MODID);
    public static final RegistryObject<OxygenSource<ItemStack>> CURIOS = registry.register("curios", () -> OxygenSource.forItems(
            DEFAULT_PRIORITY_CURIOS,
            entity ->
                    BOCuriosCompatServerConfig.getCurios(entity)
                    .filter(stack -> stack.is(BOItemTags.BREATHABLES))));

    public static void register(IEventBus eventBus) {
        registry.register(eventBus);
        registry = null;
    }
}
