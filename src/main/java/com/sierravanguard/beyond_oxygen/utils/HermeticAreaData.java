package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.network.InvalidateHermeticAreasPacket;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class HermeticAreaData extends SavedData {
    private final Map<Long, HermeticArea> areas = new HashMap<>();
    private static final String KEY = "beyond_oxygen_areas";

    public static HermeticAreaData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> HermeticAreaData.load(level, tag),
                HermeticAreaData::new,
                KEY
        );
    }

    private HermeticAreaData() {}

    private static HermeticAreaData load(ServerLevel level, CompoundTag tag) {
        HermeticAreaData data = new HermeticAreaData();
        ListTag list = tag.getList("Areas", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            HermeticArea area = HermeticArea.load(level, list.getCompound(i));
            data.areas.put(area.getId(), area);
            data.indexArea(area);
        }
        System.out.printf("[BO-DEBUG] Loaded %d areas\n", data.getAreas().size());
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (HermeticArea area : areas.values()) list.add(area.save());
        tag.put("Areas", list);
        System.out.printf("[BO-DEBUG] Saving %d areas\n", list.size());
        return tag;
    }

    public HermeticArea getOrCreate(ServerLevel level, BlockPos pos, long id) {
        if (id == Long.MIN_VALUE) {
            long newId = generateUniqueId();
            HermeticArea area = new HermeticArea(level, pos, newId);
            areas.put(newId, area);
            return area;
        }
        return areas.computeIfAbsent(id, k -> {
            HermeticArea area = new HermeticArea(level, pos, id);
            setDirty();
            return area;
        });
    }
    private long generateUniqueId() {
        long id;
        Random random = new Random();
        do {
            id = random.nextLong();
        } while (id == Long.MIN_VALUE || areas.containsKey(id));
        return id;
    }


    public void remove(long id) {
        areas.remove(id);
        setDirty();
    }

    public Map<Long, HermeticArea> getAreas() {
        return areas;
    }
    private final Map<Long, Set<Long>> chunkToAreas = new HashMap<>();

    void indexArea(HermeticArea area) {
        area.recalcBounds();

        ChunkPos min = new ChunkPos((int) area.getBounds().minX >> 4, (int) area.getBounds().minZ >> 4);
        ChunkPos max = new ChunkPos((int) area.getBounds().maxX >> 4, (int) area.getBounds().maxZ >> 4);

        for (int x = min.x; x <= max.x; x++) {
            for (int z = min.z; z <= max.z; z++) {
                long key = ChunkPos.asLong(x, z);
                chunkToAreas.computeIfAbsent(key, k -> new HashSet<>()).add(area.getId());
            }
        }
    }


    public List<HermeticArea> getAreasAffecting(BlockPos pos) {
        long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Set<Long> ids = chunkToAreas.get(key);
        if (ids == null) return List.of();
        return ids.stream()
                .map(areas::get)
                .filter(Objects::nonNull)
                .toList();
    }

}
