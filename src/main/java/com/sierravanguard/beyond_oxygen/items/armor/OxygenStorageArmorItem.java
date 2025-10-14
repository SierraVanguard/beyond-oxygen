package com.sierravanguard.beyond_oxygen.items.armor;

import com.sierravanguard.beyond_oxygen.cap.OxygenTankCap;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
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
        AtomicInteger result = new AtomicInteger();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            result.set(Math.round((float) handler.getFluidInTank(0).getAmount() * 13.0F / (float) capacity));
        });
        return result.get();
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();

        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
            int totalTicks = cap.getFluidInTank(0).getAmount() + tag.getInt("ticks");
            tooltip.add(Component.translatable("tooltip.beyond_oxygen.oxygen", formatTicksToTime(totalTicks))
                    .withStyle(ChatFormatting.AQUA));
        });

        tooltip.add(Component.translatable("tooltip.beyond_oxygen.decompression_warning")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW));
        if (customTooltipSupplier != null) {
            tooltip.addAll(customTooltipSupplier.get());
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (OxygenHelper.isInBreathableEnvironment(serverPlayer)) return;
        if (!SpaceSuitHandler.isWearingFullSuit(serverPlayer)) return;

        consumeOxygen(serverPlayer, stack);
    }

    private void consumeOxygen(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        AtomicInteger ticks = new AtomicInteger(tag.getInt("ticks"));

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
            ticks.decrementAndGet();
        }

        tag.putInt("ticks", ticks.get());
        stack.setTag(tag);
    }
    public static String formatTicksToTime(int ticks) {
        int totalSeconds = ticks / 20;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
