package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";
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
        CHANNEL.registerMessage(nextID(), SetHelmetOpenPacket.class,
                SetHelmetOpenPacket::encode,
                SetHelmetOpenPacket::decode,
                SetHelmetOpenPacket::handle);

        CHANNEL.registerMessage(nextID(), SyncHelmetStatePacket.class,
                SyncHelmetStatePacket::encode,
                SyncHelmetStatePacket::decode,
                SyncHelmetStatePacket::handle);

        CHANNEL.registerMessage(nextID(), SyncEntityHelmetStatePacket.class,
                SyncEntityHelmetStatePacket::encode,
                SyncEntityHelmetStatePacket::decode,
                SyncEntityHelmetStatePacket::handle);

        CHANNEL.registerMessage(nextID(), SyncSealedAreaStatusPacket.class,
                SyncSealedAreaStatusPacket::encode,
                SyncSealedAreaStatusPacket::decode,
                SyncSealedAreaStatusPacket::handle);

        CHANNEL.registerMessage(nextID(), BubbleRadiusPacket.class,
                BubbleRadiusPacket::encode,
                BubbleRadiusPacket::decode,
                BubbleRadiusPacket::handle);

        CHANNEL.registerMessage(nextID(), VentInfoMessage.class,
                VentInfoMessage::encode,
                VentInfoMessage::decode,
                VentInfoMessage::handle);

        CHANNEL.registerMessage(nextID(), SyncHermeticBlocksS2CPacket.class,
                SyncHermeticBlocksS2CPacket::encode,
                SyncHermeticBlocksS2CPacket::decode,
                SyncHermeticBlocksS2CPacket::handle);

        CHANNEL.registerMessage(nextID(), InvalidateHermeticAreasPacket.class,
                InvalidateHermeticAreasPacket::encode,
                InvalidateHermeticAreasPacket::decode,
                InvalidateHermeticAreasPacket::handle);
    }

    public static void sendSetHelmetOpenPacket(boolean open) {
        CHANNEL.sendToServer(new SetHelmetOpenPacket(open));
    }

    public static void sendSealedAreaStatusToClient(Player player, boolean isInSealedArea) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SyncSealedAreaStatusPacket(player.getUUID(), isInSealedArea));
    }

    public static void sendToAllPlayers(Object pkt) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);
    }
    public static void sendInvalidateHermeticAreas(long areaId, boolean clearAll) {
        InvalidateHermeticAreasPacket pkt = new InvalidateHermeticAreasPacket(areaId, clearAll);
        CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);
    }

}
