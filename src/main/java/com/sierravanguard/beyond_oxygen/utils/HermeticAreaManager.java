package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.compat.CompatUtils;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HermeticAreaManager {
    public static final Map<LivingEntity, Set<HermeticArea>> entitiesInSealedAreas = new ConcurrentHashMap<>();

    private static void addEntityToHermeticArea(LivingEntity entity, HermeticArea area) {
        Set<HermeticArea> areas = entitiesInSealedAreas.computeIfAbsent(entity, k -> ConcurrentHashMap.newKeySet());
        boolean wasSealed = !areas.isEmpty();
        areas.add(area);
        if (!wasSealed && entity instanceof ServerPlayer player) {
            NetworkHandler.sendSealedAreaStatusToClient(player, true);
        }
    }

    private static void removeEntityFromHermeticArea(LivingEntity entity, HermeticArea area) {
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

    public static boolean updateIsEntityInHermeticArea(LivingEntity entity, HermeticArea area) {
        if (entity == null || area == null) return false;
        BlockPos localPos = CompatUtils.getAreaBlockPos(entity.getEyePosition(), area);
        if (area.contains(localPos)) {
            addEntityToHermeticArea(entity, area);
            return true;
        } else {
            removeEntityFromHermeticArea(entity, area);
            return false;
        }
    }

    public static HermeticArea getHermeticAreaContaining(LivingEntity entity) {
        Set<HermeticArea> areas = entitiesInSealedAreas.get(entity);
        if (areas == null || areas.isEmpty()) return null;

        for (HermeticArea area : areas) {
            if (updateIsEntityInHermeticArea(entity, area)) return area;
        }
        return null;
    }
}
