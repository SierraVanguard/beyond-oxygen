package com.sierravanguard.beyond_oxygen.items;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.BOServerConfig;
import com.sierravanguard.beyond_oxygen.cap.OxygenTankCap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OxygenTank extends Item {
    public OxygenTank(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        int capacity = BOServerConfig.getOxygenTankCapacity();
        return new OxygenTankCap(stack, capacity);
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x87CEEB;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        AtomicInteger width = new AtomicInteger();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            int rawWidth = Math.round((float) handler.getFluidInTank(0).getAmount() * 13f / BOServerConfig.getOxygenTankCapacity());
            width.set(Math.max(0, Math.min(rawWidth, 13)));
        });
        return width.get();
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {

            int totalTicks = cap.getFluidInTank(0).getAmount();
            tooltip.add(Component.literal(formatTicksToTime(totalTicks))
                    .withStyle(ChatFormatting.AQUA));
        });
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static String formatTicksToTime(int ticks) {
        int totalSeconds = ticks / 20;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
