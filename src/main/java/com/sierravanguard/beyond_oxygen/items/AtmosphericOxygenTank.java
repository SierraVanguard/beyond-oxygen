package com.sierravanguard.beyond_oxygen.items;

import com.sierravanguard.beyond_oxygen.cap.OxygenTankCap;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AtmosphericOxygenTank extends Item implements ICurioItem {

    private static final int CAPACITY = 24000; // Larger capacity for passive refill
    private static final int REFILL_RATE = 1; // Refill 1 unit per second (20 ticks)
    private static final String REFILLING_TAG = "Refilling";
    private static final String TICKS_TAG = "ticks";

    public AtmosphericOxygenTank(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new OxygenTankCap(stack, CAPACITY);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;

        CompoundTag tag = stack.getOrCreateTag();
        boolean isBreathable = OxygenHelper.isInBreathableEnvironment(player);
        tag.putBoolean(REFILLING_TAG, isBreathable);

        if (isBreathable) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
                if (handler.getFluidInTank(0).isEmpty()) {
                    handler.fill(new FluidStack(handler.getFluidInTank(0), 0), IFluidHandler.FluidAction.EXECUTE);
                }
                refillTank(stack);
            });
        } else {
            consumeOxygen(player, stack);
        }
    }

    private void refillTank(ItemStack stack) {
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            FluidStack fluid = handler.getFluidInTank(0);
            if (fluid.isEmpty()) {
                fluid = new FluidStack(handler.getFluidInTank(0), 0);
            }

            if (fluid.getAmount() < CAPACITY) {
                int fillAmount = Math.min(REFILL_RATE, CAPACITY - fluid.getAmount());
                handler.fill(new FluidStack(fluid.getFluid(), fillAmount), IFluidHandler.FluidAction.EXECUTE);
            }
        });
    }

    private void consumeOxygen(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        AtomicInteger ticks = new AtomicInteger(getLeftTicks(tag));
        if (!SpaceSuitHandler.isWearingFullSuit(player)) {
            return;
        }
        if (ticks.get() <= 0) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
                if (!handler.getFluidInTank(0).isEmpty()) {
                    ticks.set(1);
                    handler.drain(1, IFluidHandler.FluidAction.EXECUTE);
                }
            });
        }

        if (ticks.get() > 0) {
            player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 2, 0, false, false));
            ticks.set(ticks.get() - 1);
        }

        setLeftTicks(tag, ticks.get());
        stack.setTag(tag);
    }

    private int getLeftTicks(CompoundTag tag) {
        return tag != null ? tag.getInt(TICKS_TAG) : 0;
    }

    private void setLeftTicks(CompoundTag tag, int ticks) {
        if (tag != null) {
            tag.putInt(TICKS_TAG, ticks);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x87D0E6;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        AtomicInteger result = new AtomicInteger();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            result.set(Math.round((float)handler.getFluidInTank(0).getAmount() * 13.0F / CAPACITY));
        });
        return result.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int totalTicks = handler.getFluidInTank(0).getAmount() + getLeftTicks(tag);
            tooltip.add(Component.literal("Oxygen: " + formatTicksToTime(totalTicks))
                    .withStyle(ChatFormatting.AQUA));
            if (tag.getBoolean(REFILLING_TAG)) {
                tooltip.add(Component.literal("Siphon Active").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.literal("Refilling: " + REFILL_RATE + " units/sec").withStyle(ChatFormatting.DARK_GREEN));
            } else {
                tooltip.add(Component.literal("Needs Breathable Air").withStyle(ChatFormatting.RED));
            }
            if (level != null && level.isClientSide) {
                if (net.minecraft.client.Minecraft.getInstance().player != null &&
                        !SpaceSuitHandler.isWearingFullSuit(net.minecraft.client.Minecraft.getInstance().player)) {
                    tooltip.add(Component.literal("Full pressure suit required!")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                }
            }

            tooltip.add(Component.literal("Warranty void if used on Tier 3+ Planets").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.DARK_PURPLE));
        });
    }

    public static String formatTicksToTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}