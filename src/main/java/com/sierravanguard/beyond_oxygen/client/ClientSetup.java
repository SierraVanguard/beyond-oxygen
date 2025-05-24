package com.sierravanguard.beyond_oxygen.client;

import com.sierravanguard.beyond_oxygen.items.CannedFoodItem;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOCurioRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.sierravanguard.beyond_oxygen.client.renderer.BubbleGeneratorBlockEntityRenderer;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
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

    }
}

