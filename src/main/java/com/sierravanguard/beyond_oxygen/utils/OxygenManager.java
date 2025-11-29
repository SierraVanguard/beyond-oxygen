package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.items.OxygenTank;
import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "beyond_oxygen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OxygenManager {
    
    public static void consumeOxygen(LivingEntity entity) {
        if (!SpaceSuitHandler.isWearingFullSuit(entity)) return;
        boolean hasOxygen = false;
        int mbToDrain = 1;



        //we drain held items first
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                ItemStack held = entity.getItemBySlot(slot);
                if (held.is(BOItemTags.BREATHABLES)) {
                    if (drainFluid(held, mbToDrain)) {
                        hasOxygen = true;
                        break;
                    }
                }
            }
        }

        //then we drain inventory
        if (!hasOxygen) if (entity instanceof Player player) {
            Inventory inventory = player.getInventory();
            List<ItemStack> items = inventory.items;
            int selected = Inventory.isHotbarSlot(inventory.selected) ? inventory.selected : -1;
            for (int i = 0; i < items.size(); ++i) {
                if (i != selected) {
                    ItemStack stack = items.get(i);
                    if (stack.is(BOItemTags.BREATHABLES)) {
                        if (drainFluid(stack, mbToDrain)) {
                            hasOxygen = true;
                            break;
                        }
                    }
                }
            }
        }

        //then we drain armor
        if (!hasOxygen) for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack armor = entity.getItemBySlot(slot);
                if (!(armor.getItem() instanceof OxygenStorageArmorItem)) continue;
                if (drainFluid(armor, mbToDrain)) {
                    hasOxygen = true;
                    break;
                }
            }
        }


        if (hasOxygen) {
            entity.addEffect(new MobEffectInstance(
                    BOEffects.OXYGEN_SATURATION.get(),
                    BOConfig.getTimeToImplode(),
                    0,
                    false,
                    false
            ));
        }
    }

    
    public static int getTotalOxygen(LivingEntity entity) {
        int total = 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack armor = entity.getItemBySlot(slot);
                if (!(armor.getItem() instanceof OxygenStorageArmorItem)) continue;
                total += getFluidAmount(armor);
            } else {
                ItemStack held = entity.getItemBySlot(slot);
                if (held.is(BOItemTags.BREATHABLES)) {
                    total += getFluidAmount(held);
                }
            }
        }


        if (entity instanceof Player player) {
            Inventory inventory = player.getInventory();
            List<ItemStack> items = inventory.items;
            int selected = Inventory.isHotbarSlot(inventory.selected) ? inventory.selected : -1;
            for (int i = 0; i < items.size(); ++i) {
                if (i != selected) {
                    ItemStack stack = items.get(i);
                    if (stack.is(BOItemTags.BREATHABLES)) {
                        total += getFluidAmount(stack);
                    }
                }
            }
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
