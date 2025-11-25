package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.items.OxygenTank;
import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OxygenManager {

    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        consumeOxygen(player);
    }

    
    public static void consumeOxygen(Player player) {
        if (!SpaceSuitHandler.isWearingFullSuit(player)) return;
        boolean hasOxygen = false;
        int mbToDrain = 1;


        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(BOItemTags.BREATHABLES)) {
                if (drainFluid(stack, mbToDrain)) hasOxygen = true;
            }
        }


        if (!hasOxygen) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!(armor.getItem() instanceof OxygenStorageArmorItem)) continue;
                if (drainFluid(armor, mbToDrain)) hasOxygen = true;
            }
        }


        if (hasOxygen) {
            player.addEffect(new MobEffectInstance(
                    BOEffects.OXYGEN_SATURATION.get(),
                    BOConfig.getTimeToImplode(),
                    0,
                    false,
                    false
            ));
        }
    }

    
    public static int getTotalOxygen(Player player) {
        int total = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(BOItemTags.BREATHABLES)) {
                total += getFluidAmount(stack);
            }
        }


        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) continue;
            total += getFluidAmount(player.getItemBySlot(slot));
        }

        return total;
    }

    
    private static int getFluidAmount(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> handler.getTanks() > 0 ? handler.getFluidInTank(0).getAmount() : 0)
                .orElse(0);
    }

    
    private static boolean drainFluid(ItemStack stack, int mb) {
        if (stack.isEmpty()) return false;
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> {
                    if (handler.getTanks() == 0) return false;
                    return handler.drain(mb, IFluidHandler.FluidAction.EXECUTE).getAmount() > 0;
                })
                .orElse(false);
    }
}
