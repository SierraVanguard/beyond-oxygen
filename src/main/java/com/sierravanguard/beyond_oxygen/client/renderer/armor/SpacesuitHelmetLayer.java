package com.sierravanguard.beyond_oxygen.client.renderer.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.capabilities.HelmetState;
import com.sierravanguard.beyond_oxygen.client.model.OpenableHelmetModel;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

public class SpacesuitHelmetLayer<T extends LivingEntity> extends RenderLayer<T, HumanoidModel<T>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(BeyondOxygen.MODID, "armor"), "openable_helmet");
    public final OpenableHelmetModel<T> model;

    public SpacesuitHelmetLayer(RenderLayerParent<T, HumanoidModel<T>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new OpenableHelmetModel<>(modelSet.bakeLayer(LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof OpenableSpacesuitHelmetItem helmetItem)) return;
        LazyOptional<HelmetState> lazyState = HelmetState.get(entity);
        if (lazyState.isPresent() && lazyState.resolve().get().isOpen()) return;
        HelmetState.get(entity).ifPresent(state -> {
            ResourceLocation texture = helmetItem.getHelmetTexture(helmet, entity);
            model.helmet.copyFrom(getParentModel().head);
            model.helmet.visible = getParentModel().head.visible;
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(texture));
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        });
    }
}
