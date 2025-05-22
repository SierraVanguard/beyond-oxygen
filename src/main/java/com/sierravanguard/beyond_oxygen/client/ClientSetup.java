package com.sierravanguard.beyond_oxygen.client;

import com.sierravanguard.beyond_oxygen.registry.BOCurioRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            BOCurioRenderers.register(modelSet);
        });
    }
}
