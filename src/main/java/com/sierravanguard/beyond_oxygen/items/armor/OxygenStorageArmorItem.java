package com.sierravanguard.beyond_oxygen.items.armor;

import com.sierravanguard.beyond_oxygen.BOConfig;
import com.sierravanguard.beyond_oxygen.cap.OxygenTankCap;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class OxygenStorageArmorItem extends SpacesuitArmorItem {

    private final int capacity;
    private final Supplier<List<Component>> customTooltipSupplier;

    public OxygenStorageArmorItem(ArmorMaterial material, Type type, Properties properties, int capacity,
                                  @Nullable Supplier<List<Component>> customTooltipSupplier) {
        super(material, type, properties);
        this.capacity = capacity;
        this.customTooltipSupplier = customTooltipSupplier;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
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
            width.set(Math.round((float) handler.getFluidInTank(0).getAmount() * 13f / capacity));
        });
        return width.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {

            int oxygenTicks = cap.getFluidInTank(0).getAmount();
            tooltip.add(Component.translatable("tooltip.beyond_oxygen.oxygen", formatTicksToTime(oxygenTicks))
                    .withStyle(ChatFormatting.AQUA));
        });

        tooltip.add(Component.translatable("tooltip.beyond_oxygen.decompression_warning")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW));

        if (customTooltipSupplier != null) {
            tooltip.addAll(customTooltipSupplier.get());
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static String formatTicksToTime(int ticks) {
        int seconds = ticks / 20;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
