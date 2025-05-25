package com.sierravanguard.beyond_oxygen.client;

import com.sierravanguard.beyond_oxygen.client.renderer.armor.SpacesuitHelmetLayer;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOCurioRenderers;
import com.sierravanguard.beyond_oxygen.registry.BOMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.sierravanguard.beyond_oxygen.client.renderer.BubbleGeneratorBlockEntityRenderer;
import com.sierravanguard.beyond_oxygen.client.menu.BubbleGeneratorScreen;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            BOCurioRenderers.register(modelSet);
            BlockEntityRenderers.register(
                    BOBlockEntities.BUBBLE_GENERATOR.get(),
                    BubbleGeneratorBlockEntityRenderer::new
            );
        });
        MenuScreens.register(BOMenus.BUBBLE_GENERATOR.get(), BubbleGeneratorScreen::new);
    }
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // Iterate all player skins (vanilla + modded)
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                // Add your custom helmet layer
                renderer.addLayer(new SpacesuitHelmetLayer(renderer));
            }
        }
    }

}

