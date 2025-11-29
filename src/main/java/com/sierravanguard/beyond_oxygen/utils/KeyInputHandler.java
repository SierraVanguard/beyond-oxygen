package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.capabilities.HelmetState;
import com.sierravanguard.beyond_oxygen.items.armor.IOpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.BOKeybindings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "beyond_oxygen")
public class KeyInputHandler {

    private static final boolean wasPressedLastTick = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (BOKeybindings.TOGGLE_HELMET.consumeClick()) {
            if (!(helmet.getItem() instanceof IOpenableSpacesuitHelmetItem openableSpacesuitHelmetItem) || !openableSpacesuitHelmetItem.canOpenHelmet(player, helmet)) {
                return;
            }
            LazyOptional<HelmetState> helmetStateLazy = HelmetState.get(player);
            player.displayClientMessage(
                    Component.translatable("message.helmet.status", getHelmetStateString(helmetStateLazy)),
                    true);


            helmetStateLazy.ifPresent(state -> NetworkHandler.sendSetHelmetOpenPacket(!state.isOpen()));
        }
    }
    public static Component getHelmetStateString(LazyOptional<HelmetState> helmetStateLazy) {
        return helmetStateLazy
                .map(state -> state.isOpen()
                        ? Component.translatable("message.helmet.closed")
                        : Component.translatable("message.helmet.open"))
                .orElse(Component.translatable("message.helmet.unknown"));
    }
}
