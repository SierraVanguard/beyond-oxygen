package com.kito1z.not_enough_oxygen.client.overlay;

import com.kito1z.not_enough_oxygen.items.LargeOxygenTank;
import com.kito1z.not_enough_oxygen.registry.NEOItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OxygenTankOverlay {

    private static final String BACK_SLOT = "back";

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack tankStack = getEquippedOxygenTank(player);
        if (tankStack.isEmpty()) return;

        AtomicInteger remainingTicks = new AtomicInteger(0);
        tankStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
            int amount = cap.getFluidInTank(0).getAmount();
            CompoundTag tag = tankStack.getOrCreateTag();
            remainingTicks.set(amount + getLeftTicks(tag));
        });

        int ticks = remainingTicks.get();
        if (ticks <= 0) return;

        String timeString = "O2 REMAINING: " + LargeOxygenTank.formatTicksToTime(ticks);
        renderOxygenText(event.getGuiGraphics(), timeString, event.getWindow().getGuiScaledWidth());
    }

    private static void renderOxygenText(GuiGraphics guiGraphics, String text, int screenWidth) {
        Minecraft mc = Minecraft.getInstance();
        int x = screenWidth / 2 - mc.font.width(text) / 2;
        int y = 10;

        // Shadow
        guiGraphics.drawString(
                mc.font,
                text,
                x + 1,
                y + 1,
                0xFF000000,
                false
        );

        // Main text
        guiGraphics.drawString(
                mc.font,
                text,
                x,
                y,
                0xFF00FFFF,
                false
        );
    }

    private static ItemStack getEquippedOxygenTank(Player player) {
        ItemStack backTank = getBackSlotTank(player);
        if (!backTank.isEmpty()) {
            return backTank;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (isOxygenTank(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (isOxygenTank(offHand)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack getBackSlotTank(Player player) {
        try {
            List<SlotResult> backItems = CuriosApi.getCuriosHelper().findCurios(player, BACK_SLOT);
            for (SlotResult slotResult : backItems) {
                ItemStack stack = slotResult.stack();
                if (isOxygenTank(stack)) {
                    return stack;
                }
            }
        } catch (Exception e) {
            // Curios not installed or failed to get items
        }
        return ItemStack.EMPTY;
    }

    private static int getLeftTicks(CompoundTag tag) {
        return tag != null ? tag.getInt("ticks") : 0;
    }

    private static boolean isOxygenTank(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == NEOItems.SMALL_OXYGEN_TANK.get() ||
                stack.getItem() == NEOItems.MEDIUM_OXYGEN_TANK.get() ||
                stack.getItem() == NEOItems.LARGE_OXYGEN_TANK.get() ||
                stack.getItem() == NEOItems.ATMOSPHERIC_OXYGEN_TANK.get();
    }
}