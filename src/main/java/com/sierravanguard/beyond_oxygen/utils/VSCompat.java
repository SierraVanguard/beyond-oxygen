package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.blocks.entity.BubbleGeneratorBlockEntity;
import com.sierravanguard.beyond_oxygen.blocks.entity.VentBlockEntity;
import com.sierravanguard.beyond_oxygen.compat.ColdSweatCompat;
import com.sierravanguard.beyond_oxygen.compat.CompatLoader;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.registry.BOEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.ShipData;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.Ship;


import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VSCompat {
    public static final Map<Player, Integer> playersInSealedShips = new HashMap<>();

    public static boolean applySealedEffects(ServerPlayer player, BlockPos pos, HermeticArea hermeticArea, VentBlockEntity entity) {
        ServerShip ship = (ServerShip) VSGameUtilsKt.getShipManagingPos(player.level(), pos);
        if (ship == null) {
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
                    BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false
            ));
            player.setAirSupply(player.getMaxAirSupply());
            if (entity.temperatureRegulatorApplied) CompatLoader.setComfortableTemperature(player);
            updateSealedStatus(player, true);
            player.setAirSupply(player.getMaxAirSupply());
        }
        return false;
    }

    public static void updateSealedStatus(ServerPlayer player, boolean isSealed) {
        boolean currentlySealed = playersInSealedShips.containsKey(player);

        if (isSealed) {
            if (!currentlySealed) {
                NetworkHandler.sendSealedAreaStatusToClient(player, true);
            }
            playersInSealedShips.put(player, 5);
        } else {
            if (currentlySealed) {
                playersInSealedShips.remove(player);
                NetworkHandler.sendSealedAreaStatusToClient(player, false);
            }
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

    public static boolean applyBubbleEffects(ServerPlayer player, BlockPos origin, float radius, BubbleGeneratorBlockEntity entity) {
        Level level = player.level();
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, origin);
        if (ship == null) {
            updateSealedStatus(player, false);
            return false;
        }
        Vec3 eyePos = player.getEyePosition();
        Vector3d shipEyePos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePos.x, eyePos.y, eyePos.z));
        Vector3d shipBubbleCenter = new Vector3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);

        double distanceSquared = shipEyePos.distanceSquared(shipBubbleCenter);
        if (distanceSquared <= radius * radius * 2) {
            player.addEffect(new MobEffectInstance(BOEffects.OXYGEN_SATURATION.get(), 5, 0, false, false));
            player.setAirSupply(player.getMaxAirSupply());
            if (entity.temperatureRegulatorApplied) CompatLoader.setComfortableTemperature(player);
            updateSealedStatus(player, true);
            return true;
        } else {
            updateSealedStatus(player, false);
            return false;
        }
    }

    public static boolean isWithinShipRadius(Level level, Player player, BlockPos origin, double radius) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, origin);
        if (ship == null) return false;

        Vec3 eyePos = player.getEyePosition();
        Vector3d shipPos = ship.getTransform().getWorldToShip().transformPosition(
                new Vector3d(eyePos.x, eyePos.y, eyePos.z)
        );

        Vector3d localOrigin = new Vector3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);

        return shipPos.distanceSquared(localOrigin) <= radius * radius;
    }

    public static ServerShip getShipAtPosition(ServerLevel level, BlockPos pos) {
        return (ServerShip) VSGameUtilsKt.getShipManagingPos(level, pos);
    }


    public static ServerShip getShipById(ServerLevel targetLevel, long shipId) {
        QueryableShipData<Ship> shipData = VSGameUtilsKt.getAllShips(targetLevel);
        if (shipData == null) {
            return null;
        }
        Ship ship = shipData.getById(shipId);
        if (ship instanceof ServerShip serverShip) {
            return serverShip;
        }
        return null;
    }
}