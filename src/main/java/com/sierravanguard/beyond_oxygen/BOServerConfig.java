package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.registry.BOOxygenSources;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

//config values that should be automatically synchronized to the client.
@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BOServerConfig {
    private static final ForgeConfigSpec.ConfigValue<Integer> HELD_ITEMS_OXYGEN_PRIORITY;
    private static final ForgeConfigSpec.ConfigValue<Integer> INVENTORY_OXYGEN_PRIORITY;
    private static final ForgeConfigSpec.ConfigValue<Integer> ARMOR_OXYGEN_PRIORITY;

    private static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_TANK_CAPACITY;
    private static final ForgeConfigSpec.ConfigValue<Integer> VENT_CONSUMPTION;

    static final ForgeConfigSpec SPEC;

    //we instantiate all config in the class constructor here so that the builder can be released from memory after creating the config spec.
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        HELD_ITEMS_OXYGEN_PRIORITY = builder
                .comment("Priority for consuming oxygen from held items. Use " + Integer.MIN_VALUE + " to disable.")
                .define("heldItemsOxygenPriority", BOOxygenSources.DEFAULT_PRIORITY_HELD_ITEMS);
        INVENTORY_OXYGEN_PRIORITY = builder
                .comment("Priority for consuming oxygen from items in the player inventory. Use " + Integer.MIN_VALUE + " to disable.")
                .define("playerInventoryOxygenPriority", BOOxygenSources.DEFAULT_PRIORITY_PLAYER_INVENTORY);
        ARMOR_OXYGEN_PRIORITY = builder
                .comment("Priority for consuming oxygen from armor items. Use " + Integer.MIN_VALUE + " to disable.")
                .define("armorOxygenPriority", BOOxygenSources.DEFAULT_PRIORITY_ARMOR);

        OXYGEN_TANK_CAPACITY = builder
                .comment("Max amount of oxygen that oxygen tank can contain (in mb)")
                .define("oxygenTankCapacity", 1200);
        VENT_CONSUMPTION = builder
                .comment("How many blocks vent can fill with 1 oxygen unit")
                .define("ventConsumption", 20);

        SPEC = builder.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC && SPEC.isLoaded()) loadConfig();
    }

    private static int oxygenTankCapacity;
    private static int ventConsumption;

    //we load all the config values into more friendly versions here, and can process custom syntax issues.
    public static void loadConfig() {
        BOOxygenSources.HELD_ITEMS.get().setPriority(HELD_ITEMS_OXYGEN_PRIORITY.get());
        BOOxygenSources.INVENTORY.get().setPriority(INVENTORY_OXYGEN_PRIORITY.get());
        BOOxygenSources.ARMOR.get().setPriority(ARMOR_OXYGEN_PRIORITY.get());

        oxygenTankCapacity = OXYGEN_TANK_CAPACITY.get();
        ventConsumption = VENT_CONSUMPTION.get();
    }

    public static int getOxygenTankCapacity() {
        return oxygenTankCapacity;
    }

    public static int getVentConsumption() {
        return ventConsumption;
    }
}
