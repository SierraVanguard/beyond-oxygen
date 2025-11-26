package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.capabilities.HelmetState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHelmetStatePacket {

    private final boolean open;

    public SyncHelmetStatePacket(boolean open) {
        this.open = open;
    }

    public static void encode(SyncHelmetStatePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.open);
    }

    public static SyncHelmetStatePacket decode(FriendlyByteBuf buf) {
        return new SyncHelmetStatePacket(buf.readBoolean());
    }

    public static void handle(SyncHelmetStatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null) return;
            HelmetState.get(player).ifPresent(state -> {
                state.setOpen(pkt.open);
            });
        });

        context.setPacketHandled(true);
    }
}
