package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.capabilities.*;
import com.sierravanguard.beyond_oxygen.compat.CompatLoader;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.*;
import com.sierravanguard.beyond_oxygen.utils.HermeticAreaManager;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    public static final Logger LOGGER = LogUtils.getLogger();

    public BeyondOxygen() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.COMMON, BOConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, BOServerConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(HermeticAreaManager.class);
        BOBlocks.BLOCKS.register(modEventBus);
        BOBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        BOItems.ITEMS.register(modEventBus);
        BOEffects.EFFECTS.register(modEventBus);
        BOMenus.MENUS.register(modEventBus);
        BOCreativeTabs.TABS.register(modEventBus);
        BOOxygenSources.register(modEventBus);
        BOCapabilities.init();
        NetworkHandler.register();
        CompatLoader.init(context, modEventBus);
    }

    public static class ModsLoaded{
        public static final boolean VS = ModList.get().isLoaded("valkyrienskies");
        public static final boolean CS = ModList.get().isLoaded("coldsweat");
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(HelmetState.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity) {
            event.addCapability(HelmetState.ID, new HelmetState(entity));
        }
    }

}
