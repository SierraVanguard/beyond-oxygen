package com.sierravanguard.beyond_oxygen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import com.sierravanguard.beyond_oxygen.client.ClientSealedAreaState;

public class SyncSealedAreaStatusPacket {
    private final UUID playerId;
    private final boolean isInSealedArea;

    public SyncSealedAreaStatusPacket(UUID playerId, boolean isInSealedArea) {
        this.playerId = playerId;
        this.isInSealedArea = isInSealedArea;
    }

    public static void encode(SyncSealedAreaStatusPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeBoolean(msg.isInSealedArea);
    }

    public static SyncSealedAreaStatusPacket decode(FriendlyByteBuf buf) {
        return new SyncSealedAreaStatusPacket(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(SyncSealedAreaStatusPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null && localPlayer.getUUID().equals(msg.playerId)) {
                ClientSealedAreaState.setSealedStatus(msg.playerId, msg.isInSealedArea);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
