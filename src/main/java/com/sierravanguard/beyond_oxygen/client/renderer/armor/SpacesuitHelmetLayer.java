package com.sierravanguard.beyond_oxygen.client.renderer.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.items.armor.SpacesuitArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SpacesuitHelmetLayer<T extends LivingEntity> extends RenderLayer<T, HumanoidModel<T>> {

    public SpacesuitHelmetLayer(RenderLayerParent<T, HumanoidModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!(entity instanceof Player player)) return;

        ItemStack helmet = player.getInventory().armor.get(3);
        if (helmet.isEmpty()) return;
        if (!(helmet.getItem() instanceof SpacesuitArmorItem)) return;

        player.getCapability(BOCapabilities.HELMET_STATE).ifPresent(state -> {
            String textureBaseName = "spacesuit_helmet";

            if (helmet.getItem() instanceof OpenableSpacesuitHelmetItem openableHelmet) {
                textureBaseName = openableHelmet.getTextureName();
            }
            String texturePath = "textures/models/armor/" + textureBaseName + (state.isOpen() ? "_transparent.png" : ".png");
            ResourceLocation texture = new ResourceLocation("beyond_oxygen", texturePath);

            HumanoidModel<T> armorModel = new HumanoidModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)
            );
            armorModel.head.copyFrom(getParentModel().head);
            armorModel.head.visible = true;
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(texture));
            armorModel.head.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        });
    }
}
