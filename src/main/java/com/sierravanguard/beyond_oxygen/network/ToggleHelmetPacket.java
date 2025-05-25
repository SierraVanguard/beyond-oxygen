package com.sierravanguard.beyond_oxygen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ToggleHelmetPacket {

    public ToggleHelmetPacket() {}

    public static void encode(ToggleHelmetPacket pkt, FriendlyByteBuf buf) {
    }

    public static ToggleHelmetPacket decode(FriendlyByteBuf buf) {
        return new ToggleHelmetPacket();
    }

    public static void handle(ToggleHelmetPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (!context.getDirection().getReceptionSide().isServer()) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            var sender = context.getSender();
            if (sender == null) return;
            if (sender.connection == null) return;
            if (sender.connection.connection == null) return;

            sender.getCapability(com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities.HELMET_STATE).ifPresent(state -> {
                boolean newOpenState = !state.isOpen();
                state.setOpen(newOpenState);
                NetworkHandler.CHANNEL.sendTo(
                        new SyncHelmetStatePacket(newOpenState),
                        context.getNetworkManager(),
                        NetworkDirection.PLAY_TO_CLIENT
                );
            });
        });

        context.setPacketHandled(true);
    }


}
