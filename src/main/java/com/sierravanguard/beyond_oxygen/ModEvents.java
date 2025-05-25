package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities;
import com.sierravanguard.beyond_oxygen.items.CannedFoodItem;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem().isEdible() &&
                !(stack.getItem() instanceof CannedFoodItem) &&
                SpaceSuitHandler.isWearingFullSuit(player)) {

            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.literal("Can't eat that in a spacesuit!"));
            }
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onHelmetChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getSlot() != EquipmentSlot.HEAD) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        boolean wasOpenableHelmet = from.getItem() instanceof OpenableSpacesuitHelmetItem;
        boolean isOpenableHelmetNow = to.getItem() instanceof OpenableSpacesuitHelmetItem;

        if (wasOpenableHelmet && !isOpenableHelmetNow) {
            player.getCapability(BOCapabilities.HELMET_STATE).ifPresent(state -> {
                state.setOpen(false);
            });
        }
    }
}
