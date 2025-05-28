package com.sierravanguard.beyond_oxygen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BubbleGeneratorTracker {

    private static final Map<ServerLevel, Set<BlockPos>> BUBBLE_GENERATORS = new ConcurrentHashMap<>();

    public static void register(ServerLevel level, BlockPos pos) {
        BUBBLE_GENERATORS.computeIfAbsent(level, l -> ConcurrentHashMap.newKeySet()).add(pos);
    }

    public static void unregister(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = BUBBLE_GENERATORS.get(level);
        if (set != null) set.remove(pos);
    }

    public static Set<BlockPos> getGeneratorsIn(ServerLevel level) {
        return BUBBLE_GENERATORS.getOrDefault(level, Set.of());
    }
}
