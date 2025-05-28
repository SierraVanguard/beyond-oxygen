package com.sierravanguard.beyond_oxygen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VentInfoMessage {
    private final boolean isSealed;
    private final float oxygenRate;

    public VentInfoMessage(boolean isSealed, float oxygenRate) {
        this.isSealed = isSealed;
        this.oxygenRate = oxygenRate;
    }

    public static void encode(VentInfoMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isSealed);
        buf.writeFloat(msg.oxygenRate);
    }

    public static VentInfoMessage decode(FriendlyByteBuf buf) {
        boolean sealed = buf.readBoolean();
        float rate = buf.readFloat();
        return new VentInfoMessage(sealed, rate);
    }

    public static void handle(VentInfoMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                Component message = msg.isSealed
                        ? Component.translatable("message.vent.oxygen_flow", msg.oxygenRate).withStyle(ChatFormatting.AQUA)
                        : Component.translatable("message.vent.not_sealed").withStyle(ChatFormatting.RED);

                mc.player.displayClientMessage(message, true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
