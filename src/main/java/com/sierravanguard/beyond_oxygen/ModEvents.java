package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.capabilities.BOCapabilities;
import com.sierravanguard.beyond_oxygen.items.CannedFoodItem;
import com.sierravanguard.beyond_oxygen.items.armor.OpenableSpacesuitHelmetItem;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.BODamageSources;
import com.sierravanguard.beyond_oxygen.registry.BOFluids;
import com.sierravanguard.beyond_oxygen.utils.*;
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
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

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
        HermeticAreaServerManager.onBlockChanged((ServerLevel) level, pos);

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
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!BOConfig.getUnbreathableDimensions().contains(level.dimension().location())) {
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

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        HermeticAreaServerManager.onBlockChanged(level, pos);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        HermeticAreaServerManager.onBlockChanged(level, pos);
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        HermeticAreaServerManager.onBlockChanged(level, pos);
    }
    @SubscribeEvent
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getLevel().isClientSide()) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        HermeticAreaServerManager.onBlockChanged(level, pos);
    }
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
 
            HermeticAreaData.get(serverLevel);
            System.out.printf("[DEBUG] Preloaded HermeticAreaData for %s\n", serverLevel.dimension().location());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        BODamageSources.releaseSources();
        BOFluids.releaseFluids();
    }
}
