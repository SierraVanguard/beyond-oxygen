package com.sierravanguard.beyond_oxygen.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.TicketType;

public class ChunkLoader {

    private static final TicketType<BlockPos> CRYOBED_TICKET =
            TicketType.create("beyond_oxygen:cryobed", (a, b) -> 0);

    public static boolean loadChunk(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        level.getChunkSource().addRegionTicket(CRYOBED_TICKET, chunkPos, 1, pos);
        return level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) != null;
    }
    public static void unloadChunk(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        level.getChunkSource().removeRegionTicket(CRYOBED_TICKET, chunkPos, 1, pos);
    }
}

