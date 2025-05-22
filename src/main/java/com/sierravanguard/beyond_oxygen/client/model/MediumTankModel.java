package com.sierravanguard.beyond_oxygen.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class MediumTankModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("beyond_oxygen", "medium_tank"), "main");

    private final ModelPart tankMedium;

    public MediumTankModel(ModelPart root) {
        this.tankMedium = root.getChild("tank_medium");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("tank_medium", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(6.5F, 0.0F, -8.0F, 3, 8, 3, new CubeDeformation(0.0F))
                        .texOffs(0, 11).addBox(7.0F, -1.0F, -7.5F, 2, 1, 2, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, 16.0F, 8.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        tankMedium.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
