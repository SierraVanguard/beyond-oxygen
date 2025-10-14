package com.sierravanguard.beyond_oxygen.items;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.cap.OxygenTankCap;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OxygenTank extends Item{

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new OxygenTankCap(stack, BOConfig.oxygenTankCapacity);
    }

    public OxygenTank(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean isBarVisible(ItemStack p_150899_) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack p_150901_) {
        return 8900331;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        AtomicInteger result = new AtomicInteger();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler->{
            result.set(Math.round((float) handler.getFluidInTank(0).getAmount() * 13.0F / (float) BOConfig.oxygenTankCapacity));
        });
        return result.get();
    }

    public static String formatTicksToTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> text, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap->{
            text.add(Component.literal(formatTicksToTime(cap.getFluidInTank(0).getAmount()* BOConfig.oxygenConsumption+getLeftTicks(stack.getTag()))).withStyle(ChatFormatting.AQUA));
        });
        super.appendHoverText(stack, level, text, flag);
    }

    private int getLeftTicks(CompoundTag tag) {
        if(tag==null) return 0;
        return tag.getInt("ticks");
    }
    private void setLeftTicks(CompoundTag tag, int value) {
        if(tag==null) return;
        tag.putInt("ticks", value);
    }
}
