package com.sierravanguard.beyond_oxygen.client.overlay;

import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OxygenTankOverlay {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack chestplate = player.getInventory().armor.get(2);
        if (chestplate.isEmpty() || !(chestplate.getItem() instanceof OxygenStorageArmorItem)) return;

        AtomicInteger remainingTicks = new AtomicInteger(0);
        chestplate.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
            int amount = cap.getFluidInTank(0).getAmount();
            CompoundTag tag = chestplate.getOrCreateTag();
            remainingTicks.set(amount + getLeftTicks(tag));
        });

        int ticks = remainingTicks.get();
        if (ticks <= 0) return;

        String timeString = "O₂ REMAINING: " + OxygenStorageArmorItem.formatTicksToTime(ticks);
        renderOxygenText(event.getGuiGraphics(), timeString, event.getWindow().getGuiScaledWidth());
    }

    private static void renderOxygenText(GuiGraphics guiGraphics, String text, int screenWidth) {
        Minecraft mc = Minecraft.getInstance();
        int x = screenWidth / 2 - mc.font.width(text) / 2;
        int y = 10;

        guiGraphics.drawString(mc.font, text, x + 1, y + 1, 0xFF000000, false);
        guiGraphics.drawString(mc.font, text, x, y, 0xFF00FFFF, false);
    }

    private static int getLeftTicks(CompoundTag tag) {
        return tag != null ? tag.getInt("ticks") : 0;
    }
}
