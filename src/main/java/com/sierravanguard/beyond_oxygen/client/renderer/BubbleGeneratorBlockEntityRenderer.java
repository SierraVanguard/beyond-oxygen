package com.sierravanguard.beyond_oxygen.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.client.model.BubbleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class BubbleGeneratorBlockEntityRenderer implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<BubbleGeneratorBlockEntity> {

    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(
            new net.minecraft.resources.ResourceLocation("beyond_oxygen", "textures/entity/bubble.png")
    );

    private final BakedModel bubbleModel;

    public BubbleGeneratorBlockEntityRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context) {
        this.bubbleModel = BubbleModel.getBubbleModel();
    }

    @Override
    public void render(@NotNull BubbleGeneratorBlockEntity entity, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (entity == null) return;
        Level level = entity.getLevel();
        if (level == null || bubbleModel == null) return;
        if (entity.getCurrentRadius() <= 1.0f) return;
        BlockPos blockPos = entity.getBlockPos();

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (com.sierravanguard.beyond_oxygen.BeyondOxygen.ModsLoaded.VS && level.isClientSide) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);
            if (ship != null) {
                Matrix4dc shipToWorld = ship.getTransform().getShipToWorld();
                Matrix4f shipToWorldMojang = new Matrix4f();
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 4; col++) {
                        shipToWorldMojang.set(row, col, (float) shipToWorld.get(row, col));
                    }
                }
                poseStack.last().pose().mul(shipToWorldMojang);
            }
        }
        float scale = entity.getCurrentRadius() / 1.5f;
        poseStack.scale(scale, scale, scale);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RENDER_TYPE);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                vertexConsumer,
                null,
                bubbleModel,
                1f, 1f, 1f,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                net.minecraftforge.client.model.data.ModelData.EMPTY,
                RENDER_TYPE
        );

        poseStack.popPose(); // POP LAST
    }


}

