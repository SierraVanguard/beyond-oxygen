package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.items.OxygenTank;
import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OxygenManager {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        consumeOxygen(player);
    }

    public static void consumeOxygen(ServerPlayer player) {
        if (!SpaceSuitHandler.isWearingFullSuit(player)) {
            return;
        }
        int oxygenPerMB = BOConfig.getOxygenConsumption();
        int mbToDrain = 1;
        boolean hasOxygen = false;


        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof OxygenTank)) continue;
            if (drainFluid(stack, mbToDrain)) {
                hasOxygen = true;
            }
        }


        if (!hasOxygen) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!(armor.getItem() instanceof OxygenStorageArmorItem)) continue;
                if (drainFluid(armor, mbToDrain)) {
                    hasOxygen = true;
                }
            }
        }

        int totalMB = getTotalOxygenMB(player);

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

    public static int getTotalOxygenMB(ServerPlayer player) {
        AtomicInteger total = new AtomicInteger(0);


        for (ItemStack stack : player.getInventory().items) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }


        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack armor = player.getItemBySlot(slot);
            armor.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }

        return total.get();
    }

    private static boolean drainFluid(ItemStack stack, int mb) {
        final boolean[] drainedAny = {false};
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            if (!handler.getFluidInTank(0).isEmpty()) {
                int drained = handler.drain(mb, IFluidHandler.FluidAction.EXECUTE).getAmount();
                if (drained > 0) drainedAny[0] = true;
            }
        });
        return drainedAny[0];
    }


    public static int getTotalOxygenTicks(ServerPlayer player) {
        AtomicInteger total = new AtomicInteger(0);


        for (ItemStack stack : player.getInventory().items) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }


        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack armor = player.getItemBySlot(slot);
            armor.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }

        return total.get();
    }
}
