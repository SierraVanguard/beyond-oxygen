package com.kito1z.not_enough_oxygen.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class AtmosphericTankModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("not_enough_oxygen", "atmospheric_tank"), "main");

    private final ModelPart tank_medium;
    private final ModelPart tank_medium2;
    public AtmosphericTankModel(ModelPart root) {
        this.tank_medium = root.getChild("tank_medium");
        this.tank_medium2 = this.tank_medium.getChild("tank_medium2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition tank_medium = partdefinition.addOrReplaceChild("tank_medium",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(8.5F, 0.0F, -8.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 0).addBox(9.0F, -1.0F, -7.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, 16.0F, 8.0F)
        );

        tank_medium.addOrReplaceChild("tank_medium2",
                CubeListBuilder.create()
                        .texOffs(0, 11).addBox(4.5F, 0.0F, -8.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(12, 3).addBox(5.0F, -1.0F, -7.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 32, 32); // Same texture size
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, float red,
                               float green, float blue, float alpha) {
        tank_medium.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}