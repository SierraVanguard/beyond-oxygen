package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.compat.CompatLoader;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.Ship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VSCompat {
    public static final Map<Player, Set<HermeticArea>> playersInSealedAreas = new HashMap<>();

    public static boolean applySealedEffects(ServerPlayer player, BlockPos pos, HermeticArea hermeticArea, VentBlockEntity entity) {
        ServerShip ship = (ServerShip) VSGameUtilsKt.getShipManagingPos(player.level(), pos);
        if (ship == null) return false;

        Vec3 eyePosition = player.getEyePosition();
        Vector3d shipPos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePosition.x, eyePosition.y, eyePosition.z)
        );
        BlockPos shipBlockPos = new BlockPos(
                (int) Math.floor(shipPos.x),
                (int) Math.floor(shipPos.y),
                (int) Math.floor(shipPos.z)
        );

        if (hermeticArea.isHermetic() && hermeticArea.contains(shipBlockPos)) {
            player.addEffect(new MobEffectInstance(
                    BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false
            ));
            player.setAirSupply(player.getMaxAirSupply());
            if (entity.temperatureRegulatorApplied) CompatLoader.setComfortableTemperature(player);

            addPlayerToHermeticArea(player, hermeticArea);
        } else {
            removePlayerFromHermeticArea(player, hermeticArea);
        }

        return false;
    }

    private static void addPlayerToHermeticArea(ServerPlayer player, HermeticArea area) {
        Set<HermeticArea> areas = playersInSealedAreas.computeIfAbsent(player, k -> new HashSet<>());
        boolean wasSealed = !areas.isEmpty();
        areas.add(area);
        if (!wasSealed) {
            NetworkHandler.sendSealedAreaStatusToClient(player, true);
        }
    }

    private static void removePlayerFromHermeticArea(ServerPlayer player, HermeticArea area) {
        Set<HermeticArea> areas = playersInSealedAreas.get(player);
        if (areas != null) {
            areas.remove(area);
            if (areas.isEmpty()) {
                playersInSealedAreas.remove(player);
                NetworkHandler.sendSealedAreaStatusToClient(player, false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (Player player : playersInSealedAreas.keySet()) {
            NetworkHandler.sendSealedAreaStatusToClient((ServerPlayer) player, true);
        }
    }

    public static boolean applyBubbleEffects(ServerPlayer player, BlockPos origin, float radius, BubbleGeneratorBlockEntity entity) {
        Level level = player.level();
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, origin);
        if (ship == null) {
            removeAllHermeticAreasForPlayer(player);
            return false;
        }

        Vec3 eyePos = player.getEyePosition();
        Vector3d shipEyePos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePos.x, eyePos.y, eyePos.z));
        Vector3d shipBubbleCenter = new Vector3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);

        double distanceSquared = shipEyePos.distanceSquared(shipBubbleCenter);
        if (distanceSquared <= radius * radius * 2) {
            player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
            player.setAirSupply(player.getMaxAirSupply());
            if (entity.temperatureRegulatorApplied) CompatLoader.setComfortableTemperature(player);
            return true;
        } else {
            return false;
        }
    }

    private static void removeAllHermeticAreasForPlayer(ServerPlayer player) {
        if (playersInSealedAreas.containsKey(player)) {
            playersInSealedAreas.remove(player);
            NetworkHandler.sendSealedAreaStatusToClient(player, false);
        }
    }

    public static boolean isWithinShipRadius(Level level, Player player, BlockPos origin, double radius) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, origin);
        if (ship == null) return false;

        Vec3 eyePos = player.getEyePosition();
        Vector3d shipPos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePos.x, eyePos.y, eyePos.z)
        );

        Vector3d localOrigin = new Vector3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);
        return shipPos.distanceSquared(localOrigin) <= radius * radius;
    }

    public static ServerShip getShipAtPosition(ServerLevel level, BlockPos pos) {
        return (ServerShip) VSGameUtilsKt.getShipManagingPos(level, pos);
    }

    public static ServerShip getShipById(ServerLevel targetLevel, long shipId) {
        QueryableShipData<Ship> shipData = VSGameUtilsKt.getAllShips(targetLevel);
        if (shipData == null) {
            return null;
        }
        Ship ship = shipData.getById(shipId);
        if (ship instanceof ServerShip serverShip) {
            return serverShip;
        }
        return null;
    }
}
