package com.sierravanguard.beyond_oxygen.client;

import com.sierravanguard.beyond_oxygen.client.model.AtmosphericTankModel;
import com.sierravanguard.beyond_oxygen.client.model.LargeTankModel;
import com.sierravanguard.beyond_oxygen.client.model.MediumTankModel;
import com.sierravanguard.beyond_oxygen.client.model.SmallTankModel;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.MOD)
public class LayerSetup {

    @SubscribeEvent
    public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MediumTankModel.LAYER_LOCATION, MediumTankModel::createBodyLayer);
        event.registerLayerDefinition(SmallTankModel.LAYER_LOCATION, SmallTankModel::createBodyLayer);
        event.registerLayerDefinition(LargeTankModel.LAYER_LOCATION, LargeTankModel::createBodyLayer);
        event.registerLayerDefinition(AtmosphericTankModel.LAYER_LOCATION, AtmosphericTankModel::createBodyLayer);
    }
}
