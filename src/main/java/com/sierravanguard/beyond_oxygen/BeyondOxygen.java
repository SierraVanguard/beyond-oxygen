package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.capabilities.*;
import com.sierravanguard.beyond_oxygen.compat.CompatLoader;
import com.sierravanguard.beyond_oxygen.items.OxygenTank;
import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.*;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BOConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(VSCompat.class);
        BOBlocks.BLOCKS.register(modEventBus);
        BOBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        BOItems.ITEMS.register(modEventBus);
        BOEffects.EFFECTS.register(modEventBus);
        BOMenus.MENUS.register(modEventBus);
        BOCreativeTabs.TABS.register(modEventBus);
        BOCapabilities.init();
        NetworkHandler.register();
        CompatLoader.init();
    }

    public static class ModsLoaded{
        public static final boolean VS = ModList.get().isLoaded("valkyrienskies");
        public static final boolean CS = ModList.get().isLoaded("coldsweat");
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IHelmetState.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(new ResourceLocation(MODID, "helmet_state"), new HelmetStateProvider(player));
        }
    }

}
