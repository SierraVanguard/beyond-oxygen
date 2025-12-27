package com.sierravanguard.beyond_oxygen.client.model;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BubbleModel {
    private static ObjModel bubbleModel;

    public static ObjModel getBubbleModel() {
        return bubbleModel;
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) BubbleModel::reloadResources);
    }

    private static void reloadResources(ResourceManager resourceManager) {
        try {
            bubbleModel = ObjModel.load(resourceManager, new ResourceLocation(BeyondOxygen.MODID, "models/entity/bubble.obj"));
        } catch (Throwable t) {
            bubbleModel = null;
            BeyondOxygen.LOGGER.error("Failed to load oxygen bubble model!", t);
        }
    }
}