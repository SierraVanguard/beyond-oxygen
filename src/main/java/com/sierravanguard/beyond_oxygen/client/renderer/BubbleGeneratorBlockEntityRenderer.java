package com.sierravanguard.beyond_oxygen.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.client.model.BubbleModel;
import com.sierravanguard.beyond_oxygen.client.model.ObjModel;
import com.sierravanguard.beyond_oxygen.compat.ClientCompatUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BubbleGeneratorBlockEntityRenderer implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<BubbleGeneratorBlockEntity> {
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(
            new ResourceLocation("beyond_oxygen", "textures/entity/bubble.png")
    );

    public BubbleGeneratorBlockEntityRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull BubbleGeneratorBlockEntity entity, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ObjModel bubbleModel = BubbleModel.getBubbleModel();
        Level level = entity.getLevel();
        if (level == null || bubbleModel == null) return;
        if (entity.getCurrentRadius() <= 1.0f) return;
        BlockPos blockPos = entity.getBlockPos();

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        ClientCompatUtils.applyPoseTransforms(poseStack, level, blockPos);
        float scale = entity.getCurrentRadius() / 1.5f;
        poseStack.scale(scale, scale, scale);

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer buffer = bufferSource.getBuffer(RENDER_TYPE);
        bubbleModel.visit((x, y, z, nX, nY, nZ, u, v) -> buffer
                .vertex(pose, x, y, z)
                .color(1f, 1f, 1f, 1f)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, nX, nY, nZ)
                .endVertex());
        poseStack.popPose(); 
    }


}

