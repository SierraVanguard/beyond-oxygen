package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.BOKeybindings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;

import static com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities.HELMET_STATE;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "beyond_oxygen")
public class KeyInputHandler {

    private static final boolean wasPressedLastTick = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (Minecraft.getInstance().player == null) return;
        ItemStack helmet = Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.HEAD);
        if (BOKeybindings.TOGGLE_HELMET.consumeClick()) {
            if (!(helmet.getItem() instanceof OpenableSpacesuitHelmetItem)) {
                return;
            }
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("message.helmet.status", getHelmetStateString(Minecraft.getInstance().player)),
                    true);


            NetworkHandler.sendToggleHelmetPacket();
        }
    }
    public static Component getHelmetStateString(Player player) {
        return player.getCapability(HELMET_STATE)
                .map(state -> state.isOpen()
                        ? Component.translatable("message.helmet.closed")
                        : Component.translatable("message.helmet.open"))
                .orElse(Component.translatable("message.helmet.unknown"));
    }
}
