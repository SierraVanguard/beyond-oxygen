package com.sierravanguard.beyond_oxygen.client;

import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHelper {
    public static boolean isPlayerNotWearingFullSuit() {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        return player != null && !SpaceSuitHandler.isWearingFullSuit(player);
    }
}
