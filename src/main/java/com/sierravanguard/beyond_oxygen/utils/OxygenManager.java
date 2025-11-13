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
        System.out.println("[OxygenManager] Tick for player: " + player.getName().getString());

        if (OxygenHelper.isInBreathableEnvironment(player)) {
            System.out.println("[OxygenManager] Player is in breathable environment.");
            return;
        }

        if (!SpaceSuitHandler.isWearingFullSuit(player)) {
            System.out.println("[OxygenManager] Player is NOT wearing full suit.");
            return;
        } else {
            System.out.println("[OxygenManager] Player is wearing full suit.");
        }

        int oxygenPerMB = BOConfig.getOxygenConsumption(); // units per mB
        int mbToDrain = 1; // always drain 1 mB per tick, convert internally to units
        boolean hasOxygen = false;

        // Inventory tanks first
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof OxygenTank)) continue;
            if (drainFluid(stack, mbToDrain)) {
                hasOxygen = true;
                System.out.println("[OxygenManager] Drained 1 mB from inventory tank: " + stack);
            }
        }

        // Armor tanks if inventory didn't have oxygen
        if (!hasOxygen) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!(armor.getItem() instanceof OxygenStorageArmorItem)) continue;
                if (drainFluid(armor, mbToDrain)) {
                    hasOxygen = true;
                    System.out.println("[OxygenManager] Drained 1 mB from armor: " + armor + " at slot " + slot);
                }
            }
        }

        int totalMB = getTotalOxygenMB(player);
        System.out.println("[OxygenManager] Total oxygen mB remaining: " + totalMB);

        if (hasOxygen) {
            // Apply effect based on oxygen units
            player.addEffect(new MobEffectInstance(
                    BOEffects.OXYGEN_SATURATION.get(),
                    BOConfig.getTimeToImplode(),
                    0,
                    false,
                    false
            ));
        } else {
            System.out.println("[OxygenManager] No oxygen available to consume!");
        }
    }

    public static int getTotalOxygenMB(ServerPlayer player) {
        AtomicInteger total = new AtomicInteger(0);

        // Inventory tanks
        for (ItemStack stack : player.getInventory().items) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }

        // Armor tanks
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

        // Inventory tanks
        for (ItemStack stack : player.getInventory().items) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }

        // Armor tanks
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack armor = player.getItemBySlot(slot);
            armor.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                total.addAndGet(cap.getFluidInTank(0).getAmount());
            });
        }

        return total.get();
    }
}
