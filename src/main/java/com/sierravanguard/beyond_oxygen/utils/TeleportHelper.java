package com.sierravanguard.beyond_oxygen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class TeleportHelper {

    private static final Map<UUID, TeleportTask> activeTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, DelayedTeleport> delayedTeleports = new ConcurrentHashMap<>();

    private static class TeleportTask {
        final ServerPlayer player;
        final ServerLevel level;
        final BlockPos pos;
        int ticksWaited = 0;
        static final int MAX_TICKS_WAIT = 80;

        TeleportTask(ServerPlayer player, ServerLevel level, BlockPos pos) {
            this.player = player;
            this.level = level;
            this.pos = pos;
        }

        boolean tryTeleport() {
            boolean chunkLoaded = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
            System.out.println("[DEBUG] tryTeleport: Chunk loaded? " + chunkLoaded);
            System.out.println("[DEBUG] Player dimension: " + player.level().dimension().location());
            System.out.println("[DEBUG] Target dimension: " + level.dimension().location());

            if (chunkLoaded) {
                double x, y, z;

                ServerShip ship = VSCompat.getShipAtPosition(level, pos);
                if (ship != null) {
                    var transform = ship.getTransform();
                    Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                    Vector3d worldPos = transform.getShipToWorld().transformPosition(localPos);
                    x = worldPos.x;
                    y = worldPos.y;
                    z = worldPos.z;
                    System.out.println("[DEBUG] Teleporting to ship world coordinates: " + x + ", " + y + ", " + z);
                } else {
                    x = pos.getX() + 0.5;
                    y = pos.getY() + 1.0;
                    z = pos.getZ() + 0.5;
                }

                float yaw = player.getYRot();
                float pitch = player.getXRot();

                if (player.level() != level) {
                    System.out.println("[DEBUG] Performing cross-dimension teleport...");
                    player.changeDimension(level, new ITeleporter() {
                        @Override
                        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                            Entity placedEntity = repositionEntity.apply(false);
                            if (placedEntity instanceof ServerPlayer serverPlayer) {
                                serverPlayer.teleportTo(destWorld, x, y, z, yaw, serverPlayer.getXRot());
                                System.out.println("[DEBUG] Teleported to new dimension at: " + x + ", " + y + ", " + z);
                            }
                            return placedEntity;
                        }
                    });
                } else {
                    System.out.println("[DEBUG] Same-dimension teleport...");
                    player.teleportTo(level, x, y, z, yaw, pitch);
                    player.connection.send(new ClientboundPlayerPositionPacket(
                            x, y, z, yaw, pitch,
                            EnumSet.noneOf(RelativeMovement.class), 0
                    ));
                }

                ChunkLoader.unloadChunk(level, pos);
                return true;
            }

            ticksWaited++;
            return ticksWaited > MAX_TICKS_WAIT;
        }
    }

    private static class DelayedTeleport {
        final ServerLevel targetLevel;
        final BlockPos targetPos;
        int ticksWaited = 0;

        DelayedTeleport(ServerLevel level, BlockPos pos) {
            this.targetLevel = level;
            this.targetPos = pos;
        }
    }

    public static void teleportWithChunkLoad(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        Optional<CryoBedManager.CryoBedReference> optRef = CryoBedManager.getAssignedCryoBed(playerUUID);
        if (optRef.isEmpty()) {
            System.out.println("[WARN] No cryobed assigned for player " + player.getName().getString());
            return;
        }

        CryoBedManager.CryoBedReference ref = optRef.get();

        MinecraftServer server = player.getServer();
        if (server == null) {
            System.out.println("[WARN] Server instance null for player " + player.getName().getString());
            return;
        }

        Optional<ServerLevel> optLevel = CryoBedManager.getServerLevel(server, ref.dimension());
        if (optLevel.isEmpty()) {
            System.out.println("[WARN] ServerLevel not loaded for dimension " + ref.dimension().location());
            return;
        }

        ServerLevel level = optLevel.get();
        BlockPos pos = ref.worldPos();

        System.out.println("[DEBUG] teleportWithChunkLoad: Queuing teleport for player " + player.getName().getString() + " to cryobed at " + pos + " in " + level.dimension().location());

        ChunkLoader.loadChunk(level, pos);
        TeleportTask task = new TeleportTask(player, level, pos);
        activeTasks.put(playerUUID, task);

        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onServerTick(TickEvent.ServerTickEvent event) {
                if (event.phase == TickEvent.Phase.END) {
                    TeleportTask currentTask = activeTasks.get(playerUUID);
                    if (currentTask != null) {
                        boolean done = currentTask.tryTeleport();
                        if (done) {
                            activeTasks.remove(playerUUID);
                            MinecraftForge.EVENT_BUS.unregister(this);
                        }
                    } else {
                        MinecraftForge.EVENT_BUS.unregister(this);
                    }
                }
            }
        });
    }


    public static void scheduleCrossDimTeleport(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        Optional<CryoBedManager.CryoBedReference> optRef = CryoBedManager.getAssignedCryoBed(playerUUID);
        if (optRef.isEmpty()) {
            System.out.println("[WARN] No cryobed assigned for player " + player.getName().getString());
            return;
        }

        CryoBedManager.CryoBedReference ref = optRef.get();

        MinecraftServer server = player.getServer();
        if (server == null) {
            System.out.println("[WARN] Server instance null for player " + player.getName().getString());
            return;
        }

        Optional<ServerLevel> optLevel = CryoBedManager.getServerLevel(server, ref.dimension());
        if (optLevel.isEmpty()) {
            System.out.println("[WARN] ServerLevel not loaded for dimension " + ref.dimension().location());
            return;
        }

        ServerLevel level = optLevel.get();
        BlockPos pos = ref.worldPos();

        System.out.println("[DEBUG] scheduleCrossDimTeleport: Scheduling teleport for player " + player.getName().getString() + " to cryobed at " + pos + " in " + level.dimension().location());

        delayedTeleports.put(playerUUID, new DelayedTeleport(level, pos));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (Map.Entry<UUID, DelayedTeleport> entry : delayedTeleports.entrySet()) {
            UUID uuid = entry.getKey();
            DelayedTeleport task = entry.getValue();

            MinecraftServer server = task.targetLevel.getServer();
            if (server == null) continue;

            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null || !player.isAlive()) continue;

            task.ticksWaited++;
            if (task.ticksWaited < 5) continue;

            System.out.println("[DEBUG] Executing delayed cross-dimension teleport for " + player.getName().getString());

            double x = task.targetPos.getX() + 0.5;
            double y = task.targetPos.getY() + 1.0;
            double z = task.targetPos.getZ() + 0.5;
            float yaw = player.getYRot();
            float pitch = player.getXRot();

            player.changeDimension(task.targetLevel, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity placedEntity = repositionEntity.apply(false);
                    if (placedEntity instanceof ServerPlayer sp) {
                        sp.teleportTo(destWorld, x, y, z, yaw, pitch);
                        System.out.println("[DEBUG] Cross-dimension teleport success: " + x + ", " + y + ", " + z);
                    }
                    return placedEntity;
                }
            });

            delayedTeleports.remove(uuid);
        }
    }
}
