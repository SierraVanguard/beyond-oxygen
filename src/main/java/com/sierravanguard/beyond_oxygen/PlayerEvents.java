package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.utils.CryoBedManager;
import com.sierravanguard.beyond_oxygen.utils.TeleportHelper;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID)
public class PlayerEvents {

    private static final Map<UUID, TeleportTarget> pendingTeleports = new ConcurrentHashMap<>();

    private record TeleportTarget(ServerLevel level, BlockPos pos) {}

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID playerId = player.getUUID();

        System.out.println("[DEBUG] PlayerRespawnEvent: Player = " + player.getName().getString());

        Optional<CryoBedManager.CryoBedReference> refOpt = CryoBedManager.getAssignedCryoBed(playerId);
        if (refOpt.isEmpty()) return;

        CryoBedManager.CryoBedReference ref = refOpt.get();
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel targetLevel = server.getLevel(ref.dimension());
        if (targetLevel == null) return;

        BlockPos targetPos;

        if (ref.shipId() != null && ref.shipLocalPos() != null) {
            ServerShip ship = VSCompat.getShipById(targetLevel, ref.shipId());
            if (ship != null) {
                System.out.println("[DEBUG] Ship detected via shipId. Preparing to teleport on ship.");
                var shipTransform = ship.getTransform();
                Vector3d worldPos = shipTransform.getShipToWorld().transformPosition(ref.shipLocalPos());

                targetPos = new BlockPos(
                        (int) Math.floor(worldPos.x),
                        (int) Math.floor(worldPos.y),
                        (int) Math.floor(worldPos.z)
                );

                System.out.println("[DEBUG] Transformed ship-local position to world position: " + targetPos);
            } else {
                System.out.println("[DEBUG] Ship not found, falling back to world position.");
                targetPos = ref.worldPos();
            }
        } else {
            targetPos = ref.worldPos();
        }

        pendingTeleports.put(playerId, new TeleportTarget(targetLevel, targetPos));
        System.out.println("[DEBUG] Storing teleport target for tick event: " + targetPos + " in " + targetLevel.dimension().location());
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        TeleportTarget target = pendingTeleports.remove(player.getUUID());
        if (target != null) {
            System.out.println("[DEBUG] Tick teleport: Attempting teleport for player " + player.getName().getString());
            System.out.println("[DEBUG] Current dimension: " + player.level().dimension().location());
            System.out.println("[DEBUG] Target dimension: " + target.level.dimension().location());
            System.out.println("[DEBUG] Target position: " + target.pos);
            TeleportHelper.teleportWithChunkLoad(player);
        }
    }
}
