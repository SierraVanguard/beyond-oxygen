package com.sierravanguard.beyond_oxygen.capabilities;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public class BOCapabilities {
    public static Capability<IHelmetState> HELMET_STATE;

    public static void init() {
        HELMET_STATE = CapabilityManager.get(new CapabilityToken<>(){});
    }
    public static LazyOptional<IHelmetState> getHelmetState(Player player) {
        return player.getCapability(HELMET_STATE);
    }

}
