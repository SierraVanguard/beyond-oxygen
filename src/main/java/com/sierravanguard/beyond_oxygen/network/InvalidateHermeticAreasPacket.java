package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.client.HermeticAreaClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to clear or invalidate cached hermetic areas on the client.
 * Optional: include a specific shipId to only clear that one.
 */
public class InvalidateHermeticAreasPacket {
    private final long shipId;
    private final boolean clearAll;

    public InvalidateHermeticAreasPacket(long shipId, boolean clearAll) {
        this.shipId = shipId;
        this.clearAll = clearAll;
    }

    public static void encode(InvalidateHermeticAreasPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.shipId);
        buf.writeBoolean(msg.clearAll);
    }

    public static InvalidateHermeticAreasPacket decode(FriendlyByteBuf buf) {
        return new InvalidateHermeticAreasPacket(buf.readLong(), buf.readBoolean());
    }

    public static void handle(InvalidateHermeticAreasPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {

            if (context.getDirection().getReceptionSide().isClient()) {
                if (msg.clearAll) {
                    HermeticAreaClientManager.clear();
                    System.out.println("[Beyond Oxygen] Cleared all client hermetic areas.");
                } else {
                    HermeticAreaClientManager.clearShip(msg.shipId);
                    System.out.println("[Beyond Oxygen] Cleared client hermetic areas for ship " + msg.shipId);
                }
            }
        });
        context.setPacketHandled(true);
    }
}

