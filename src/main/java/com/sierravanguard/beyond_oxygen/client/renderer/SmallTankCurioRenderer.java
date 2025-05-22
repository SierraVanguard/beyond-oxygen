package com.sierravanguard.beyond_oxygen.client.renderer;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.client.model.SmallTankModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class SmallTankCurioRenderer implements ICurioRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BeyondOxygen.MODID, "textures/item/small_tank.png");

    private final SmallTankModel<LivingEntity> model;

    public SmallTankCurioRenderer(EntityModelSet modelSet) {
        this.model = new SmallTankModel<>(modelSet.bakeLayer(SmallTankModel.LAYER_LOCATION));
    }

    @Override
    public <T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, -1.0F, 0.1F);

        if (slotContext.entity().isCrouching()) {
            ICurioRenderer.translateIfSneaking(poseStack, slotContext.entity());
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);

        poseStack.popPose();
    }
}
