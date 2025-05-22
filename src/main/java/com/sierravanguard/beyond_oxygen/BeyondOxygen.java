package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.registry.*;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(BeyondOxygen.MODID)
@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeyondOxygen {

    public static final String MODID = "beyond_oxygen";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BeyondOxygen() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(VSCompat.class);
        BOBlocks.BLOCKS.register(modEventBus);
        BOBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        BOItems.ITEMS.register(modEventBus);
        BOEffects.EFFECTS.register(modEventBus);
        BOCreativeTabs.TABS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BOConfig.SPEC);
    }

    public static class ModsLoaded{
        public static final boolean VS = ModList.get().isLoaded("valkyrienskies");
    }

}
