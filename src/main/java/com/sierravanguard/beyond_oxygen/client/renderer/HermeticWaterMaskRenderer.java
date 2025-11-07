package com.sierravanguard.beyond_oxygen.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.sierravanguard.beyond_oxygen.client.ClientSealedAreaState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.Ship;
import com.sierravanguard.beyond_oxygen.client.HermeticAreaClientManager;

import java.util.*;

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
                    .createCompositeState(false)
    );


    private HermeticWaterMaskRenderer() {}
    private static final Map<Long, CachedSurface> WATER_SURFACE_CACHE = new HashMap<>();
    private static final int SURFACE_UPDATE_INTERVAL = 20; // ticks between recomputes

    private static class CachedSurface {
        double y;
        long lastTick;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

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
        if (aabbs == null || aabbs.isEmpty()) return;
        UUID playerId = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getUUID()
                : null;
        long gameTime = level.getGameTime();
        boolean inSealed = ClientSealedAreaState.isInSealedArea(playerId);

        for (AABB aabb : aabbs) {
            Long shipId = HermeticAreaClientManager.getShipIdForAABB(aabb);
            if (shipId == null) continue;

            Ship ship = HermeticAreaClientManager.getClientShipById(shipId, level);
            if (ship == null) continue;

            Matrix4dc shipToWorld = null;
            try {
                shipToWorld = ship.getTransform().getShipToWorld();
            } catch (Throwable ignored) {}

            if (shipToWorld == null) continue;
            CachedSurface cached = WATER_SURFACE_CACHE.get(shipId);
            double sliceY;
            if (cached == null || gameTime - cached.lastTick >= SURFACE_UPDATE_INTERVAL) {
                AABBdc worldAABBdc = ship.getWorldAABB();
                AABB worldAABB = new AABB(
                        worldAABBdc.minX(), worldAABBdc.minY(), worldAABBdc.minZ(),
                        worldAABBdc.maxX(), worldAABBdc.maxY(), worldAABBdc.maxZ()
                );
                Double detectedY = detectTopWaterSurfaceY(level, worldAABB);
                if (detectedY == null) continue;
                cached = new CachedSurface();
                cached.y = detectedY;
                cached.lastTick = gameTime;
                WATER_SURFACE_CACHE.put(shipId, cached);
                sliceY = detectedY;
            } else {
                sliceY = cached.y;
            }
            double offset = 0.0;
            if (inSealed && cameraPos.y <= sliceY) {
                offset = -0.002;
            } else if (!inSealed && cameraPos.y > sliceY) {
                offset = +0.002;
            }

            renderAABB(consumer, poseMatrix, shipToWorld, aabb, cameraPos, sliceY + offset);
        }

        buffer.endBatch();
    }

    private static void renderAABB(VertexConsumer consumer, Matrix4f poseMatrix,
                                   Matrix4dc shipToWorld, AABB aabb, Vec3 cameraPos, double sliceY) {

        Vector3d[] local = new Vector3d[]{
                new Vector3d(aabb.minX, aabb.minY, aabb.minZ),
                new Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
                new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
                new Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
                new Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
                new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
                new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ),
                new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ)
        };
        Vector3d[] w = new Vector3d[8];
        for (int i = 0; i < 8; i++) {
            w[i] = shipToWorld != null ? shipToWorld.transformPosition(local[i], new Vector3d()) : local[i];
        }
        final double yPlane = sliceY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Vector3d v : w) {
            if (v.y < minY) minY = v.y;
            if (v.y > maxY) maxY = v.y;
        }
        if (yPlane < minY || yPlane > maxY) return;
        final int[][] EDGES = {
                {0,1},{1,2},{2,3},{3,0},
                {4,5},{5,6},{6,7},{7,4},
                {0,4},{1,5},{2,6},{3,7}
        };

        List<Vector3d> pts = new ArrayList<>(6);
        for (int[] e : EDGES) {
            Vector3d a = w[e[0]], b = w[e[1]];
            double ya = a.y, yb = b.y;
            if ((ya < yPlane && yb > yPlane) || (yb < yPlane && ya > yPlane)
                    || Math.abs(ya - yPlane) < 1e-9 || Math.abs(yb - yPlane) < 1e-9) {
                double t = (yPlane - ya) / (yb - ya);
                if (Double.isFinite(t) && t >= 0.0 && t <= 1.0) {
                    double ix = a.x + (b.x - a.x) * t;
                    double iz = a.z + (b.z - a.z) * t;
                    pts.add(new Vector3d(ix, yPlane, iz));
                }
            }
        }

        if (pts.size() < 3) return;

        final double EPS = 1e-6;
        List<Vector3d> uniq = new ArrayList<>();
        outer: for (Vector3d p : pts) {
            for (Vector3d q : uniq)
                if (Math.abs(p.x - q.x) < EPS && Math.abs(p.z - q.z) < EPS) continue outer;
            uniq.add(p);
        }
        if (uniq.size() < 3) return;

        double cx = 0, cz = 0;
        for (Vector3d v : uniq) { cx += v.x; cz += v.z; }
        cx /= uniq.size(); cz /= uniq.size();
        final double finalCx = cx;
        final double finalCz = cz;

        uniq.sort(Comparator.comparingDouble(v -> Math.atan2(v.z - finalCz, v.x - finalCx)));

        Vector3d base = uniq.get(0);
        for (int i = 1; i + 1 < uniq.size(); i++) {
            Vector3d p1 = uniq.get(i);
            Vector3d p2 = uniq.get(i + 1);
            emitTri(consumer, poseMatrix,
                    toCamera(base, cameraPos),
                    toCamera(p1, cameraPos),
                    toCamera(p2, cameraPos));
        }
    }

    private static float[] toCamera(Vector3d world, Vec3 cam) {
        return new float[]{
                (float) (world.x - cam.x),
                (float) (world.y - cam.y + Z_FIGHT_OFFSET),
                (float) (world.z - cam.z)
        };
    }


    private static void emitTri(VertexConsumer consumer, Matrix4f poseMatrix,
                                float[] p0, float[] p1, float[] p2) {
        consumer.vertex(poseMatrix, p0[0], p0[1], p0[2]).endVertex();
        consumer.vertex(poseMatrix, p1[0], p1[1], p1[2]).endVertex();
        consumer.vertex(poseMatrix, p2[0], p2[1], p2[2]).endVertex();
    }

    public static Double detectTopWaterSurfaceY(Level level, AABB shipWorldAABB) {
        double centerX = (shipWorldAABB.minX + shipWorldAABB.maxX) * 0.5;
        double centerZ = (shipWorldAABB.minZ + shipWorldAABB.maxZ) * 0.5;

        int minY = (int) Math.floor(shipWorldAABB.minY);
        int maxY = (int) Math.ceil(shipWorldAABB.maxY);

        Double topSurfaceY = null;

        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos((int) centerX, y, (int) centerZ);
            FluidState fluid = level.getFluidState(pos);

            if (fluid.is(Fluids.WATER) && level.isEmptyBlock(pos.above())) {
                double surfaceY = y + fluid.getHeight(level, pos);
                topSurfaceY = surfaceY;
                break;
            }
        }

        return topSurfaceY;
    }
}