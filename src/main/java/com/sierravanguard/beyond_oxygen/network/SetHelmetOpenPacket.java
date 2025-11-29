package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.capabilities.HelmetState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SetHelmetOpenPacket {
    private final boolean open;

    public SetHelmetOpenPacket(boolean open) {
        this.open = open;
    }

    public static void encode(SetHelmetOpenPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.open);
    }

    public static SetHelmetOpenPacket decode(FriendlyByteBuf buf) {
        return new SetHelmetOpenPacket(buf.readBoolean());
    }

    public static void handle(SetHelmetOpenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (!context.getDirection().getReceptionSide().isServer()) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            var sender = context.getSender();
            if (sender == null) return;

            HelmetState.get(sender).ifPresent(state -> {
                if (state.isOpen() != pkt.open) {
                    state.setOpen(pkt.open, false);
                    NetworkHandler.CHANNEL.sendTo(
                            new SyncHelmetStatePacket(pkt.open),
                            context.getNetworkManager(),
                            NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            });
        });

        context.setPacketHandled(true);
    }


}
