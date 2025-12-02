package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.BeyondOxygen;
import com.sierravanguard.beyond_oxygen.items.armor.OxygenStorageArmorItem;
import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
import com.sierravanguard.beyond_oxygen.utils.OxygenSource;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BOOxygenSources {
    public static final int DEFAULT_PRIORITY_ARMOR = 1000;
    public static final int DEFAULT_PRIORITY_PLAYER_INVENTORY = 2000;
    public static final int DEFAULT_PRIORITY_HELD_ITEMS = 3000;

    public static final ResourceKey<Registry<OxygenSource<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(BeyondOxygen.MODID, "oxygen_sources"));
    private static DeferredRegister<OxygenSource<?>> registry = DeferredRegister.create(REGISTRY_KEY, BeyondOxygen.MODID);
    public static final Supplier<IForgeRegistry<OxygenSource<?>>> REGISTRY = registry.makeRegistry(() -> RegistryBuilder.of(REGISTRY_KEY.location()));

    public static final RegistryObject<OxygenSource<ItemStack>> ARMOR = registry.register("armor", () -> OxygenSource.forItems(
            DEFAULT_PRIORITY_ARMOR,
            entity -> Arrays.stream(EquipmentSlot.values())
                    .filter(EquipmentSlot::isArmor)
                    .map(entity::getItemBySlot)
                    .filter(stack -> stack.getItem() instanceof OxygenStorageArmorItem)));
    public static final RegistryObject<OxygenSource<ItemStack>> HELD_ITEMS = registry.register("held_items", () -> OxygenSource.forItems(
            DEFAULT_PRIORITY_HELD_ITEMS,
            entity -> Arrays.stream(EquipmentSlot.values())
                    .filter(Predicate.not(EquipmentSlot::isArmor))
                    .map(entity::getItemBySlot)
                    .filter(stack -> stack.is(BOItemTags.BREATHABLES))));
    public static final RegistryObject<OxygenSource<ItemStack>> INVENTORY = registry.register("player_inventory", () -> OxygenSource.forItems(
            DEFAULT_PRIORITY_PLAYER_INVENTORY,
            entity -> entity instanceof Player player ?
                    player.getInventory().items
                            .stream()
                            .filter(stack -> stack.is(BOItemTags.BREATHABLES)):
                    Stream.empty()));

    public static void register(IEventBus eventBus) {
        registry.register(eventBus);
        registry = null;
    }

    private static OxygenSource<?>[] orderedSources = {};
    private static boolean dirty = true;

    public static void markDirty() {
        dirty = true;
    }

    public static OxygenSource<?>[] getSources() {
        if (dirty) {
            StringJoiner logJoiner = new StringJoiner(", ", "Loaded oxygen sources in order: ", "");
            orderedSources = REGISTRY
                    .get()
                    .getEntries()
                    .stream()
                    .filter(e -> e.getValue().enabled())
                    .sorted(Map.Entry.comparingByValue())
                    .peek(e -> logJoiner.add(e.getKey().location().toString()))
                    .map(Map.Entry::getValue)
                    .toArray(OxygenSource[]::new);
            BeyondOxygen.LOGGER.info(logJoiner.toString());
            dirty = false;
        }
        return orderedSources;
    }
}
