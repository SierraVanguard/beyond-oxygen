package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.client.HermeticAreaClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncHermeticBlocksS2CPacket {

    private final long shipId; // use -1 for world-space blocks
    private final Set<Vec3> blocks;

    public SyncHermeticBlocksS2CPacket(long shipId, Set<Vec3> blocks) {
        this.shipId = shipId;
        this.blocks = blocks;
    }

    public static void encode(SyncHermeticBlocksS2CPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.shipId);
        buf.writeInt(pkt.blocks.size());
        for (Vec3 pos : pkt.blocks) {
            buf.writeDouble(pos.x);
            buf.writeDouble(pos.y);
            buf.writeDouble(pos.z);
        }
    }

    public static SyncHermeticBlocksS2CPacket decode(FriendlyByteBuf buf) {
        long shipId = buf.readLong();
        int size = buf.readInt();
        Set<Vec3> positions = new HashSet<>();
        for (int i = 0; i < size; i++) {
            positions.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        return new SyncHermeticBlocksS2CPacket(shipId, positions);
    }

    public static void handle(SyncHermeticBlocksS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.shipId == -1) {
                System.out.printf("[SyncHermeticBlocksS2CPacket] Received %d world-space blocks%n", msg.blocks.size());
                HermeticAreaClientManager.setWorldHermeticBlocks(msg.blocks);
            } else {
                System.out.printf("[SyncHermeticBlocksS2CPacket] Received %d blocks for ship %d%n", msg.blocks.size(), msg.shipId);
                HermeticAreaClientManager.setHermeticBlocksForShip(msg.shipId, msg.blocks);
            }
        });

        ctx.setPacketHandled(true);
    }
}
