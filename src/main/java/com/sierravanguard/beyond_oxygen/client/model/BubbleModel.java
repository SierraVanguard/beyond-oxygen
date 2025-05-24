package com.sierravanguard.beyond_oxygen.client.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.MOD)
public class BubbleModel {
    private static BakedModel bubbleModel;
    @SubscribeEvent
    public static void onModelRegistry(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation("beyond_oxygen", "entity/bubble"));
        System.out.println("Bubble model registered!");
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        bubbleModel = event.getModelManager().getModel(new ResourceLocation("beyond_oxygen", "entity/bubble"));
        System.out.println("Got bubble model, is it null?" + (bubbleModel == null));
    }

    public static BakedModel getBubbleModel() {
        return bubbleModel;
    }

}