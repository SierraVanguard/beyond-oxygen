package com.sierravanguard.beyond_oxygen.client;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.client.model.OpenableHelmetModel;
import com.sierravanguard.beyond_oxygen.client.renderer.RenderStateShardReflection;
import com.sierravanguard.beyond_oxygen.client.renderer.armor.SpacesuitHelmetLayer;
import com.sierravanguard.beyond_oxygen.registry.BOBlockEntities;
import com.sierravanguard.beyond_oxygen.registry.BOBlocks;
import com.sierravanguard.beyond_oxygen.registry.BOMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.sierravanguard.beyond_oxygen.client.renderer.BubbleGeneratorBlockEntityRenderer;
import com.sierravanguard.beyond_oxygen.client.menu.BubbleGeneratorScreen;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            BlockEntityRenderers.register(
                    BOBlockEntities.BUBBLE_GENERATOR.get(),
                    BubbleGeneratorBlockEntityRenderer::new
            );
        });
        event.enqueueWork(RenderStateShardReflection::initializeSafe);
        ItemBlockRenderTypes.setRenderLayer(BOBlocks.CRYO_BED.get(), RenderType.translucent());
        MenuScreens.register(BOMenus.BUBBLE_GENERATOR.get(), BubbleGeneratorScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpacesuitHelmetLayer.LAYER_LOCATION, OpenableHelmetModel::createLayerDefinition);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        ForgeRegistries.ENTITY_TYPES.forEach(entityType -> {
            try {
                @SuppressWarnings("unchecked")
                EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) entityType;
                LivingEntityRenderer<?, ?> renderer = event.getRenderer(livingType);
                if (renderer != null && renderer.getModel() instanceof HumanoidModel<?>) {
                    renderer.addLayer(new SpacesuitHelmetLayer(renderer, event.getEntityModels()));
                }
            } catch (ClassCastException ignored) {}
        });
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new SpacesuitHelmetLayer(renderer, event.getEntityModels()));
            }
        }
    }

}

