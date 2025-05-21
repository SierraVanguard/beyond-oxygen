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

public class SmallTankModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("not_enough_oxygen", "small_tank"), "main");

    private final ModelPart tank;

    public SmallTankModel(ModelPart root) {
        this.tank = root.getChild("tank");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition tank = root.addOrReplaceChild("tank", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(6.5F, 0.0F, -8.0F, 3.0F, 6.0F, 3.0F)  // Tank body
                        .texOffs(0, 11)
                        .addBox(7.0F, -1.0F, -7.5F, 2.0F, 1.0F, 2.0F), // Tank cap
                PartPose.offset(-8.0F, 16.0F, 8.0F)); // Offset matches Blockbench export

        return LayerDefinition.create(meshdefinition, 16, 16); // Texture size from BB
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        tank.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
