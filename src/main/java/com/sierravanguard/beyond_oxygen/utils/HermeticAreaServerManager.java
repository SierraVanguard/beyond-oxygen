package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import com.sierravanguard.beyond_oxygen.utils.ship.BuoyancyForceInducer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.apigame.world.VSPipeline;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HermeticAreaServerManager {

    public static HermeticArea getArea(ServerLevel level, BlockPos pos, long id) {
        return HermeticAreaData.get(level).getOrCreate(level, pos, id);
    }

    public static void removeArea(ServerLevel level, long id) {
        HermeticAreaData.get(level).remove(id);
    }

    public static void markDirty(ServerLevel level) {
        HermeticAreaData.get(level).setDirty();
    }

    private static final Set<Long> dirtyAreas = ConcurrentHashMap.newKeySet();

    public static void onBlockChanged(ServerLevel level, BlockPos pos) {
        HermeticAreaData data = HermeticAreaData.get(level);
        for (HermeticArea area : data.getAreasAffecting(pos)) {
            if (area.maybeContains(pos)) {
                dirtyAreas.add(area.getId());
            }
        }
    }

    private static final Set<Long> toRemove = ConcurrentHashMap.newKeySet();

    public static void removeAreaDeferred(ServerLevel level, long id) {
        toRemove.add(id);
    }


    public static void tick(ServerLevel level) {
        HermeticAreaData data = HermeticAreaData.get(level);
        Map<Long, Double> volumePerShip = new HashMap<>();

        for (Long id : dirtyAreas) {
            HermeticArea area = data.getAreas().get(id);
            if (area != null) {
                area.markDirty();
                area.bake();
                data.setDirty();
            }
        }
        dirtyAreas.clear();
        for (HermeticArea area : data.getAreas().values()) {
            volumePerShip.merge(area.getShipId(), area.lastComputedVolume, Double::sum);
        }
        if (!volumePerShip.isEmpty()) {
            QueryableShipData<LoadedShip> shipData = VSGameUtilsKt.getShipWorldNullable(level).getLoadedShips();
            for (Map.Entry<Long, Double> entry : volumePerShip.entrySet()) {
                long shipId = entry.getKey();
                double totalVolume = entry.getValue();

                Ship ship = shipData.getById(entry.getKey());
                if (ship instanceof LoadedServerShip serverShip) {
                    System.out.printf("Applying buoyant force to ship %d; Volume: %f\n", shipId, totalVolume);
                    BuoyancyForceInducer.tickOnShip(serverShip, totalVolume);
                }
            }
        }
        for (Long id : toRemove) {
            NetworkHandler.sendInvalidateHermeticAreas(id, false);
            data.remove(id);
        }
        toRemove.clear();

        Iterator<HermeticArea> it = data.getAreas().values().iterator();
        while (it.hasNext()) {
            it.next().tickDormant();
        }
    }


    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            HermeticAreaServerManager.tick((ServerLevel) event.level);
        }
    }
}
