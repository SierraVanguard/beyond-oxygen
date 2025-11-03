package com.sierravanguard.beyond_oxygen.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import com.sierravanguard.beyond_oxygen.client.HermeticAreaClientManager;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HermeticWaterMaskRenderer {

    private static final double Z_FIGHT_OFFSET = 0.002;

    private static final RenderType WATER_MASK = RenderType.create(
            "hermetic_water_mask",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShardReflection.WATER_MASK_SHADER)
                    .setTextureState(RenderStateShardReflection.NO_TEXTURE)
                    .setWriteMaskState(RenderStateShardReflection.DEPTH_WRITE)
                    .setCullState(RenderStateShardReflection.NO_CULL)
                    .setDepthTestState(RenderStateShardReflection.LEQUAL_DEPTH_TEST) // Add this
                    .createCompositeState(false)
    );

    private HermeticWaterMaskRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Camera camera = event.getCamera();
        if (camera == null) return;

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        if (buffer == null) return;

        VertexConsumer consumer = buffer.getBuffer(WATER_MASK);
        if (consumer == null) return;

        Matrix4f poseMatrix = event.getPoseStack().last().pose();
        Vec3 cameraPos = camera.getPosition();

        List<AABB> aabbs = HermeticAreaClientManager.getAll(level);
        if (aabbs == null || aabbs.isEmpty()) {
            buffer.endBatch(WATER_MASK);
            return;
        }

        for (AABB aabb : aabbs) {
            Long shipId = HermeticAreaClientManager.getShipIdForAABB(aabb);
            Matrix4dc shipToWorld = null;
            if (shipId != null) {
                Ship ship = HermeticAreaClientManager.getClientShipById(shipId, level);
                if (ship != null) {
                    try {
                        shipToWorld = ship.getTransform().getShipToWorld();
                    } catch (Throwable ignored) {
                        shipToWorld = null;
                    }
                }
            }

            renderAABB(consumer, poseMatrix, shipToWorld, aabb, cameraPos);
        }

        buffer.endBatch(WATER_MASK);
    }

    private static void renderAABB(VertexConsumer consumer, Matrix4f poseMatrix,
                                   Matrix4dc shipToWorld, AABB aabb, Vec3 cameraPos) {

        Vector3d[] corners = new Vector3d[8];
        corners[0] = new Vector3d(aabb.minX, aabb.minY, aabb.minZ);
        corners[1] = new Vector3d(aabb.maxX, aabb.minY, aabb.minZ);
        corners[2] = new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ);
        corners[3] = new Vector3d(aabb.minX, aabb.maxY, aabb.minZ);
        corners[4] = new Vector3d(aabb.minX, aabb.minY, aabb.maxZ);
        corners[5] = new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ);
        corners[6] = new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ);
        corners[7] = new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ);

        float[][] finalVertices = new float[8][3];
        for (int i = 0; i < 8; i++) {
            Vector3d worldPos;

            if (shipToWorld != null) {

                worldPos = shipToWorld.transformPosition(corners[i], new Vector3d());
            } else {

                worldPos = corners[i];
            }

            finalVertices[i][0] = (float) (worldPos.x - cameraPos.x);
            finalVertices[i][1] = (float) (worldPos.y - cameraPos.y + Z_FIGHT_OFFSET);
            finalVertices[i][2] = (float) (worldPos.z - cameraPos.z);
        }

        int[][] faces = {
                {0, 1, 2, 3},
                {4, 5, 6, 7},
                {0, 3, 7, 4},
                {1, 5, 6, 2},
                {0, 4, 5, 1},
                {3, 2, 6, 7}
        };

        for (int[] face : faces) {
            int a = face[0], b = face[1], c = face[2], d = face[3];

            emitTri(consumer, poseMatrix, finalVertices[a], finalVertices[b], finalVertices[c]);
            emitTri(consumer, poseMatrix, finalVertices[c], finalVertices[d], finalVertices[a]);

            emitTri(consumer, poseMatrix, finalVertices[c], finalVertices[b], finalVertices[a]);
            emitTri(consumer, poseMatrix, finalVertices[a], finalVertices[d], finalVertices[c]);
        }
    }

    private static void emitTri(VertexConsumer consumer, Matrix4f poseMatrix,
                                float[] p0, float[] p1, float[] p2) {
        consumer.vertex(poseMatrix, p0[0], p0[1], p0[2]).endVertex();
        consumer.vertex(poseMatrix, p1[0], p1[1], p1[2]).endVertex();
        consumer.vertex(poseMatrix, p2[0], p2[1], p2[2]).endVertex();
    }
}
