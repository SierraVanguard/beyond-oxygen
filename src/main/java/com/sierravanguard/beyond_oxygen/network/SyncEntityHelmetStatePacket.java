package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.capabilities.HelmetState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEntityHelmetStatePacket {
    private final int entityId;
    private final boolean open;

    public SyncEntityHelmetStatePacket(int entityId, boolean open) {
        this.entityId = entityId;
        this.open = open;
    }

    public static void encode(SyncEntityHelmetStatePacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.entityId);
        buf.writeBoolean(pkt.open);
    }

    public static SyncEntityHelmetStatePacket decode(FriendlyByteBuf buf) {
        return new SyncEntityHelmetStatePacket(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SyncEntityHelmetStatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null) return;
            Entity entity = player.level().getEntity(pkt.entityId);
            if (!(entity instanceof LivingEntity livingEntity)) return;
            HelmetState.get(livingEntity).ifPresent(state -> {
                state.setOpen(pkt.open);
            });
        });

        context.setPacketHandled(true);
    }
}
