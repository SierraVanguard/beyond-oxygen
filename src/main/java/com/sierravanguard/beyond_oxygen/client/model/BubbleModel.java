package com.sierravanguard.beyond_oxygen.client.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BubbleModel {
    private static BakedModel bubbleModel;
    @SubscribeEvent
    public static void onModelRegistry(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation("beyond_oxygen", "entity/bubble"));
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        bubbleModel = event.getModelManager().getModel(new ResourceLocation("beyond_oxygen", "entity/bubble"));
    }

    public static BakedModel getBubbleModel() {
        return bubbleModel;
    }

}