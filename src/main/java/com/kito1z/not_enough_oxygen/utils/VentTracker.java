package com.kito1z.not_enough_oxygen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VentTracker {

    // Maps level -> vent positions
    private static final Map<ServerLevel, Set<BlockPos>> VENT_POSITIONS = new ConcurrentHashMap<>();

    public static void registerVent(ServerLevel level, BlockPos pos) {
        VENT_POSITIONS.computeIfAbsent(level, l -> ConcurrentHashMap.newKeySet()).add(pos);
    }

    public static void unregisterVent(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = VENT_POSITIONS.get(level);
        if (set != null) set.remove(pos);
    }

    public static Set<BlockPos> getVentsIn(ServerLevel level) {
        return VENT_POSITIONS.getOrDefault(level, Set.of());
    }
}
