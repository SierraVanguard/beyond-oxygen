package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities;
import com.sierravanguard.beyond_oxygen.items.CannedFoodItem;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.utils.CryoBedManager;
import com.sierravanguard.beyond_oxygen.utils.OxygenHelper;
import com.sierravanguard.beyond_oxygen.utils.SpaceSuitHandler;
import com.sierravanguard.beyond_oxygen.utils.VSCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem().isEdible() &&
                !(stack.getItem() instanceof CannedFoodItem) &&
                SpaceSuitHandler.isWearingFullSuit(player)) {

            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.spacefood"));
            }
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof BedBlock) {
            CryoBedManager.getAssignedCryoBed(player.getUUID()).ifPresent(ref -> {
                CryoBedManager.removeCryoBed(ref.dimension(), ref.worldPos());
                player.sendSystemMessage(Component.translatable("message.cryo_bed.unassigned"));
            });
        }
    }
    @SubscribeEvent
    public static void onHelmetChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getSlot() != EquipmentSlot.HEAD) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        boolean wasOpenableHelmet = from.getItem() instanceof OpenableSpacesuitHelmetItem;
        boolean isOpenableHelmetNow = to.getItem() instanceof OpenableSpacesuitHelmetItem;

        if (wasOpenableHelmet && !isOpenableHelmetNow) {
            player.getCapability(BOCapabilities.HELMET_STATE).ifPresent(state -> {
                state.setOpen(false);
            });
        }
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        NetworkHandler.sendSealedAreaStatusToClient(player, false);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        NetworkHandler.sendSealedAreaStatusToClient(player, false);
    }
    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!BOConfig.unbreathableDimensions.contains(level.dimension().location())) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();
        if (!(block instanceof CropBlock)) {
            if (OxygenHelper.isBlockPosInsideBreathableArea(level, pos)){
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            }
        }
    }
}
