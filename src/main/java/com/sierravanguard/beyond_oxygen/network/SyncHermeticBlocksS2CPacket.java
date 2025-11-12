package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.client.HermeticAreaClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncHermeticBlocksS2CPacket {

    private final long areaId;
    private final Long shipId; 
    private final Set<Vec3> blocks;

    public SyncHermeticBlocksS2CPacket(long areaId, Set<Vec3> blocks, Long shipId) {
        this.areaId = areaId;
        this.blocks = blocks;
        this.shipId = shipId;
    }

    public static void encode(SyncHermeticBlocksS2CPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.areaId);
        buf.writeBoolean(pkt.shipId != null);
        if (pkt.shipId != null) buf.writeLong(pkt.shipId);

        buf.writeInt(pkt.blocks.size());
        for (Vec3 pos : pkt.blocks) {
            buf.writeDouble(pos.x);
            buf.writeDouble(pos.y);
            buf.writeDouble(pos.z);
        }
    }

    public static SyncHermeticBlocksS2CPacket decode(FriendlyByteBuf buf) {
        long areaId = buf.readLong();
        Long shipId = buf.readBoolean() ? buf.readLong() : null;

        int size = buf.readInt();
        Set<Vec3> positions = new HashSet<>();
        for (int i = 0; i < size; i++) {
            positions.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        return new SyncHermeticBlocksS2CPacket(areaId, positions, shipId);
    }

    public static void handle(SyncHermeticBlocksS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            HermeticAreaClientManager.registerHermeticBlocks(msg.areaId, msg.blocks, msg.shipId);
        });
        ctx.setPacketHandled(true);
    }
}
