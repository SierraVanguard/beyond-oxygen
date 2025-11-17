package com.sierravanguard.beyond_oxygen.client.overlay;

import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.OxygenManager;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OxygenTankOverlay {

    private static final int MAX_SECONDS = 120;
    private static final int YELLOW_START = 60;
    private static final int RED_START = 10;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (OxygenHelper.isInBreathableEnvironment(player) && !player.isUnderWater() && !SpaceSuitHandler.isWearingFullSuit(player)) return;
        int totalTicks = OxygenManager.getTotalOxygen(player);
        if (totalTicks <= 0) return;

        int totalSeconds = totalTicks / 20;
        String timeString = "O₂ REMAINING: " + OxygenStorageArmorItem.formatTicksToTime(totalTicks);

        renderOxygenText(event.getGuiGraphics(), timeString, event.getWindow().getGuiScaledWidth(), totalSeconds);
    }

    private static void renderOxygenText(GuiGraphics guiGraphics, String text, int screenWidth, int seconds) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int x = screenWidth / 2 - font.width(text) / 2;
        int y = 10;

        int color = calculateColor(seconds);


        guiGraphics.drawString(font, text, x + 1, y + 1, 0xFF000000, false);


        guiGraphics.drawString(font, text, x, y, color, false);
    }

    private static int calculateColor(int seconds) {
        if (seconds > YELLOW_START) {

            float t = (float)(MAX_SECONDS - seconds) / (MAX_SECONDS - YELLOW_START);
            return lerpColor(0x00FFFF, 0xFFFF00, t);
        } else if (seconds > RED_START) {

            float t = (float)(YELLOW_START - seconds) / (YELLOW_START - RED_START);
            return lerpColor(0xFFFF00, 0xFF0000, t);
        } else {

            long tick = System.currentTimeMillis() / 250;
            return (tick % 2 == 0) ? 0xFFFF0000 : 0xFF000000;
        }
    }

    
    private static int lerpColor(int color1, int color2, float t) {
        t = Math.max(0f, Math.min(1f, t));

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
