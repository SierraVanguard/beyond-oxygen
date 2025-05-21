package com.kito1z.not_enough_oxygen.client;

import com.kito1z.not_enough_oxygen.client.model.AtmosphericTankModel;
import com.kito1z.not_enough_oxygen.client.model.LargeTankModel;
import com.kito1z.not_enough_oxygen.client.model.MediumTankModel;
import com.kito1z.not_enough_oxygen.client.model.SmallTankModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "not_enough_oxygen", bus = Mod.EventBusSubscriber.Bus.MOD)
public class LayerSetup {

    @SubscribeEvent
    public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MediumTankModel.LAYER_LOCATION, MediumTankModel::createBodyLayer);
        event.registerLayerDefinition(SmallTankModel.LAYER_LOCATION, SmallTankModel::createBodyLayer);
        event.registerLayerDefinition(LargeTankModel.LAYER_LOCATION, LargeTankModel::createBodyLayer);
        event.registerLayerDefinition(AtmosphericTankModel.LAYER_LOCATION, AtmosphericTankModel::createBodyLayer);
    }
}
