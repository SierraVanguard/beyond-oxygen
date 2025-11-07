package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Set;

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

    public static void sendToggleHelmetPacket() {
        CHANNEL.sendToServer(new ToggleHelmetPacket());
    }

    public static void sendSealedAreaStatusToClient(Player player, boolean isInSealedArea) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new SyncSealedAreaStatusPacket(player.getUUID(), isInSealedArea));
    }

    public static void sendHermeticBlocksToPlayersInLevel(ServerLevel level, int shipId, Set<Vec3> blocks) {
        SyncHermeticBlocksS2CPacket pkt = new SyncHermeticBlocksS2CPacket(shipId, blocks);
        for (ServerPlayer player : level.players()) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);
        }
    }

    public static void sendToAllPlayers(Object pkt) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);
    }
    public static void sendInvalidateHermeticAreas(long shipId, boolean clearAll) {
        InvalidateHermeticAreasPacket pkt = new InvalidateHermeticAreasPacket(shipId, clearAll);
        CHANNEL.send(PacketDistributor.ALL.noArg(), pkt);
    }

}
