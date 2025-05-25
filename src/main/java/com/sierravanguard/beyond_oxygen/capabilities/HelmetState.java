package com.sierravanguard.beyond_oxygen.capabilities;

import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.network.SyncHelmetStatePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class HelmetState implements IHelmetState {
    private boolean open = false;
    private final Player player;
    public HelmetState(Player player) {
        this.player = player;
    }
    @Override
    public boolean isOpen() {
        return open;
    }
    @Override
    public void setOpen(boolean open) {
        if (this.open != open) {
            this.open = open;

            if (!player.level().isClientSide) {
                player.getPersistentData().putBoolean("HelmetOpen", open);

                if (player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.connection != null) {
                        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                                new SyncHelmetStatePacket(open));
                    } else {
                    }
                }
            }
        }
    }

}
