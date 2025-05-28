package com.sierravanguard.beyond_oxygen.items;

import com.sierravanguard.beyond_oxygen.cap.OxygenTankCap;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import com.sierravanguard.beyond_oxygen.client.ClientHelper;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SmallOxygenTank extends Item implements ICurioItem {

    private static final int CAPACITY = 4800;

    public SmallOxygenTank(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new OxygenTankCap(stack, CAPACITY);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 8900331;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        AtomicInteger result = new AtomicInteger();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            result.set(Math.round((float) handler.getFluidInTank(0).getAmount() * 13.0F / (float) CAPACITY));
        });
        return result.get();
    }

    private int getLeftTicks(CompoundTag tag) {
        return tag != null ? tag.getInt("ticks") : 0;
    }

    private void setLeftTicks(CompoundTag tag, int ticks) {
        if (tag != null) {
            tag.putInt("ticks", ticks);
        }
    }

    public static String formatTicksToTime(int ticks) {
        int totalSeconds = ticks / 20;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
            int totalTicks = cap.getFluidInTank(0).getAmount() + getLeftTicks(tag);
            tooltip.add(Component.translatable("tooltip.beyond_oxygen.oxygen", formatTicksToTime(totalTicks))
                    .withStyle(ChatFormatting.AQUA));
        });

        if (level != null && level.isClientSide) {
            if (ClientHelper.isPlayerNotWearingFullSuit()) {
                tooltip.add(Component.translatable("tooltip.beyond_oxygen.full_suit_required")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }
        }

        tooltip.add(Component.translatable("tooltip.beyond_oxygen.babys_first_air_tank")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN));

        super.appendHoverText(stack, level, tooltip, flag);
    }


    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;
        if (OxygenHelper.isInBreathableEnvironment(player)) return;
        consumeOxygen(player, stack);
    }

    private void consumeOxygen(ServerPlayer player, ItemStack stack) {
        if (!SpaceSuitHandler.isWearingFullSuit(player)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        AtomicInteger ticks = new AtomicInteger(getLeftTicks(tag));

        if (player.getEffect(BOEffects.OXYGEN_SATURATION.get()) != null &&
                player.getEffect(BOEffects.OXYGEN_SATURATION.get()).getDuration() >= 2) {
            return;
        }

        if (ticks.get() <= 0) {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
                if (!cap.getFluidInTank(0).isEmpty()) {
                    ticks.set(1);
                    cap.drain(1, IFluidHandler.FluidAction.EXECUTE);
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
}
