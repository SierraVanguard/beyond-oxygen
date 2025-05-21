package com.kito1z.not_enough_oxygen.utils;

import com.kito1z.not_enough_oxygen.registry.NEOEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = com.kito1z.not_enough_oxygen.NotEnoughOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VSCompat {
    public static final Map<Player, Integer> playersInSealedShips = new HashMap<>();
    public static boolean applySealedEffects(ServerPlayer player, BlockPos pos, HermeticArea hermeticArea) {
        ServerShip ship = (ServerShip) VSGameUtilsKt.getShipManagingPos(player.level(), pos);
        if (ship == null){
            return false;
        }
        Vec3 eyePosition = player.getEyePosition();
        Vector3d shipPos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePosition.x, eyePosition.y, eyePosition.z)
        );
        BlockPos shipBlockPos = new BlockPos(
                (int) Math.floor(shipPos.x),
                (int) Math.floor(shipPos.y),
                (int) Math.floor(shipPos.z)
        );
        if (hermeticArea.getArea().contains(shipBlockPos)) {
            player.addEffect(new MobEffectInstance(
                    NEOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false
            ));
            player.setAirSupply(player.getMaxAirSupply());// Prevent drowning
            updateSealedStatus(player, true);
            player.setAirSupply(player.getMaxAirSupply());
        }
        return false;
    }
    public static void updateSealedStatus(ServerPlayer player, boolean isSealed) {
        if (isSealed) {
            playersInSealedShips.put(player, 5);
        }
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            playersInSealedShips.entrySet().removeIf(entry -> {
                int ticksLeft = entry.getValue() - 1;
                if (ticksLeft <= 0) {
                    return true; // Remove player
                }
                entry.setValue(ticksLeft);
                return false;
            });
        }
    }
}