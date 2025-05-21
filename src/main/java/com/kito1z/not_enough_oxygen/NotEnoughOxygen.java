package com.kito1z.not_enough_oxygen;

import com.kito1z.not_enough_oxygen.registry.*;
import com.kito1z.not_enough_oxygen.utils.VSCompat;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(NotEnoughOxygen.MODID)
@Mod.EventBusSubscriber(modid = "not_enough_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NotEnoughOxygen {

    public static final String MODID = "not_enough_oxygen";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NotEnoughOxygen() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(VSCompat.class);
        NEOBlocks.BLOCKS.register(modEventBus);
        NEOBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        NEOItems.ITEMS.register(modEventBus);
        NEOEffects.EFFECTS.register(modEventBus);
        NEOCreativeTabs.TABS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NEOConfig.SPEC);
    }

    public static class ModsLoaded{
        public static final boolean VS = ModList.get().isLoaded("valkyrienskies");
    }

}
