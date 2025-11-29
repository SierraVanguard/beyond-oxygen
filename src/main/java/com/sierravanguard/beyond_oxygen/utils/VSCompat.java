package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.VSCore;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.Ship;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VSCompat {
    public static final Map<LivingEntity, Set<HermeticArea>> entitiesInSealedAreas = new ConcurrentHashMap<>();
    public static boolean applySealedEffects(LivingEntity living, BlockPos pos, HermeticArea hermeticArea, VentBlockEntity entity) {
        ServerShip ship = (ServerShip) VSGameUtilsKt.getShipManagingPos(living.level(), pos);
        if (ship == null) return false;
        Vec3 eyePosition = living.getEyePosition();
        Vector3d shipPos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePosition.x, eyePosition.y, eyePosition.z)
        );
        BlockPos shipBlockPos = new BlockPos(
                (int) Math.floor(shipPos.x),
                (int) Math.floor(shipPos.y),
                (int) Math.floor(shipPos.z)
        );
        if (hermeticArea.isHermetic() && hermeticArea.contains(shipBlockPos)) {
            addEntityToHermeticArea(living, hermeticArea);
            if (hermeticArea.hasAir()) {
                living.addEffect(new MobEffectInstance(
                        BOEffects.OXYGEN_SATURATION.get(), BOConfig.getTimeToImplode(), 0, false, false
                ));
                living.setAirSupply(living.getMaxAirSupply());
                if (entity != null){
                    if (entity.temperatureRegulatorApplied) {
                        CompatLoader.setComfortableTemperature(living);
                    }
                }
            }
        } else {
            removeEntityFromHermeticArea(living, hermeticArea);
        }
        return false;
    }


    private static void addEntityToHermeticArea(LivingEntity entity, HermeticArea area) {
        Set<HermeticArea> areas = entitiesInSealedAreas.computeIfAbsent(entity, k -> ConcurrentHashMap.newKeySet());
        boolean wasSealed = !areas.isEmpty();
        areas.add(area);
        if (!wasSealed && entity instanceof ServerPlayer player) {
            NetworkHandler.sendSealedAreaStatusToClient(player, true);
        }
    }

    static void removeEntityFromHermeticArea(LivingEntity entity, HermeticArea area) {
        Set<HermeticArea> areas = entitiesInSealedAreas.get(entity);
        if (areas != null) {
            areas.remove(area);
            if (areas.isEmpty()) {
                entitiesInSealedAreas.remove(entity, areas);
                if (entity instanceof ServerPlayer player) {
                    NetworkHandler.sendSealedAreaStatusToClient(player, false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            Set<HermeticArea> areas = entitiesInSealedAreas.get(player);
            boolean sealed = areas != null && !areas.isEmpty();
            NetworkHandler.sendSealedAreaStatusToClient(player, sealed);
        }
    }

    public static boolean applyBubbleEffects(LivingEntity living, BlockPos origin, float radius, BubbleGeneratorBlockEntity entity) {
        Level level = living.level();
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, origin);
        if (ship == null) {
            removeAllHermeticAreasForEntity(living);
            return false;
        }

        Vec3 eyePos = living.getEyePosition();
        Vector3d shipEyePos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePos.x, eyePos.y, eyePos.z));
        Vector3d shipBubbleCenter = new Vector3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);

        double distanceSquared = shipEyePos.distanceSquared(shipBubbleCenter);
        if (distanceSquared <= radius * radius * 2) {
            living.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), BOConfig.getTimeToImplode(), 0, false, false));
            living.setAirSupply(living.getMaxAirSupply());
            if (entity.temperatureRegulatorApplied) CompatLoader.setComfortableTemperature(living);
            return true;
        } else {
            return false;
        }
    }

    private static void removeAllHermeticAreasForEntity(LivingEntity entity) {
        Set<HermeticArea> areas = entitiesInSealedAreas.remove(entity);
        if (areas != null && entity instanceof ServerPlayer player) {
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
    public static boolean isEntityInHermeticArea(LivingEntity entity, HermeticArea area) {
        if (entity == null || area == null) return false;

        Level level = entity.level();
        Vec3 eyePos = entity.getEyePosition();
        ServerShip areaShip = getShipById(area.getLevel(), area.getShipId());
        if (areaShip != null) {
            Vector3d shipLocal = areaShip.getTransform().getWorldToShip().transformPosition(
                    new Vector3d(eyePos.x, eyePos.y, eyePos.z)
            );
            BlockPos localPos = new BlockPos(
                    (int) Math.floor(shipLocal.x),
                    (int) Math.floor(shipLocal.y),
                    (int) Math.floor(shipLocal.z)
            );
            return area.contains(localPos);
        } else {
            BlockPos worldPos = new BlockPos((int) eyePos.x, (int) eyePos.y, (int) eyePos.z);
            return area.contains(worldPos);
        }
    }
    public static HermeticArea getHermeticAreaContaining(LivingEntity entity) {
        Set<HermeticArea> areas = entitiesInSealedAreas.get(entity);
        if (areas == null || areas.isEmpty()) return null;

        for (HermeticArea area : areas) {
            if (isEntityInHermeticArea(entity, area)) return area;
        }
        return null;
    }
}
