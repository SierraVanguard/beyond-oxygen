package com.sierravanguard.beyond_oxygen.client.overlay;

import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.utils.OxygenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OxygenTankOverlay {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !(player instanceof ServerPlayer serverPlayer)) return;

        int totalTicks = OxygenManager.getTotalOxygenTicks(serverPlayer);
        if (totalTicks <= 0) return;

        String timeString = "O₂ REMAINING: " + OxygenStorageArmorItem.formatTicksToTime(totalTicks);
        renderOxygenText(event.getGuiGraphics(), timeString, event.getWindow().getGuiScaledWidth());
    }

    private static void renderOxygenText(GuiGraphics guiGraphics, String text, int screenWidth) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int x = screenWidth / 2 - font.width(text) / 2;
        int y = 10;
        guiGraphics.drawString(font, text, x + 1, y + 1, 0xFF000000, false);
        guiGraphics.drawString(font, text, x, y, 0xFF00FFFF, false);
    }
}
