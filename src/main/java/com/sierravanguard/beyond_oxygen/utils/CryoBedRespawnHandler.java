package com.sierravanguard.beyond_oxygen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "beyond_oxygen")
public class CryoBedRespawnHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID playerId = player.getUUID();
        Optional<CryoBedManager.CryoBedReference> cryoBedOpt = CryoBedManager.getAssignedCryoBed(playerId);

        if (cryoBedOpt.isEmpty()) {
            return;
        }

        CryoBedManager.CryoBedReference cryoBed = cryoBedOpt.get();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel targetLevel = server.getLevel(cryoBed.dimension());
        if (targetLevel == null) return;

        BlockPos respawnPos = calculateRespawnPosition(targetLevel, cryoBed);

        player.setRespawnPosition(targetLevel.dimension(), respawnPos, 0.0f, true, false);

        if (!player.level().dimension().equals(targetLevel.dimension())) {
            handleCrossDimensionRespawn(player, targetLevel, respawnPos);
        }
    }

    private static BlockPos calculateRespawnPosition(ServerLevel level, CryoBedManager.CryoBedReference cryoBed) {
        if (cryoBed.shipId() != null && cryoBed.shipLocalPos() != null) {
            ServerShip ship = VSCompat.getShipById(level, cryoBed.shipId());
            if (ship != null) {
                var shipTransform = ship.getTransform();
                Vector3d worldPos = shipTransform.getShipToWorld().transformPosition(cryoBed.shipLocalPos());
                return new BlockPos(
                        (int) Math.floor(worldPos.x),
                        (int) Math.floor(worldPos.y),
                        (int) Math.floor(worldPos.z)
                );
            }
        }

        return cryoBed.worldPos();
    }

    private static void handleCrossDimensionRespawn(ServerPlayer player, ServerLevel targetLevel, BlockPos respawnPos) {
        player.teleportTo(
                targetLevel,
                respawnPos.getX() + 0.5,
                respawnPos.getY() + 1.0,
                respawnPos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal() instanceof ServerPlayer originalPlayer) {
            ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
            UUID playerId = newPlayer.getUUID();

            Optional<CryoBedManager.CryoBedReference> cryoBedOpt = CryoBedManager.getAssignedCryoBed(playerId);
            if (cryoBedOpt.isPresent()) {
                CryoBedManager.CryoBedReference cryoBed = cryoBedOpt.get();
                MinecraftServer server = newPlayer.getServer();
                if (server != null) {
                    ServerLevel targetLevel = server.getLevel(cryoBed.dimension());
                    if (targetLevel != null) {
                        BlockPos respawnPos = calculateRespawnPosition(targetLevel, cryoBed);
                        newPlayer.setRespawnPosition(targetLevel.dimension(), respawnPos, 0.0f, true, false);
                        if (!newPlayer.level().dimension().equals(targetLevel.dimension())) {
                            server.execute(() -> {
                                newPlayer.teleportTo(
                                        targetLevel,
                                        respawnPos.getX() + 0.5,
                                        respawnPos.getY() + 1.0,
                                        respawnPos.getZ() + 0.5,
                                        newPlayer.getYRot(),
                                        newPlayer.getXRot()
                                );
                            });
                        }
                    }
                }
            }
        }
    }
}