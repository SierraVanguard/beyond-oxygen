package com.sierravanguard.beyond_oxygen.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import com.sierravanguard.beyond_oxygen.BeyondOxygen;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BeyondOxygen.MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static int nextID() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.registerMessage(nextID(), ToggleHelmetPacket.class,
                ToggleHelmetPacket::encode,
                ToggleHelmetPacket::decode,
                ToggleHelmetPacket::handle);
        CHANNEL.registerMessage(nextID(), SyncHelmetStatePacket.class,
                SyncHelmetStatePacket::encode,
                SyncHelmetStatePacket::decode,
                SyncHelmetStatePacket::handle);
        CHANNEL.registerMessage(nextID(), SyncSealedAreaStatusPacket.class,
                SyncSealedAreaStatusPacket::encode,
                SyncSealedAreaStatusPacket::decode,
                SyncSealedAreaStatusPacket::handle);
    }

    public static void sendToggleHelmetPacket() {
        CHANNEL.sendToServer(new ToggleHelmetPacket());
    }
    public static void sendSealedAreaStatusToClient(Player player, boolean isInSealedArea) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SyncSealedAreaStatusPacket(player.getUUID(), isInSealedArea));
    }
}
