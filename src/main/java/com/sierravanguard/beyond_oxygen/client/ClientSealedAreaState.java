package com.sierravanguard.beyond_oxygen.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSealedAreaState {
    private static final Map<UUID, Boolean> SEALED_MAP = new ConcurrentHashMap<>();

    public static void setSealedStatus(UUID playerId, boolean sealed) {
        SEALED_MAP.put(playerId, sealed);
    }

    public static boolean isInSealedArea(UUID playerId) {
        return SEALED_MAP.getOrDefault(playerId, false);
    }
}
