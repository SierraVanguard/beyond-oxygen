package com.sierravanguard.beyond_oxygen.network;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BubbleRadiusPacket {
    private final BlockPos pos;
    private final boolean increase;

    public BubbleRadiusPacket(BlockPos pos, boolean increase) {
        this.pos = pos;
        this.increase = increase;
    }

    public static void encode(BubbleRadiusPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.increase);
    }

    public static BubbleRadiusPacket decode(FriendlyByteBuf buf) {
        return new BubbleRadiusPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(BubbleRadiusPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level().getBlockEntity(msg.pos) instanceof BubbleGeneratorBlockEntity be) {
                float step = 0.5f;
                float newRadius = be.controlledMaxRadius + (msg.increase ? step : -step);
                newRadius = clamp(newRadius, 0.5f, BOConfig.getBubbleMaxRadius());

                be.controlledMaxRadius = newRadius;

                be.setChanged();
                be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
