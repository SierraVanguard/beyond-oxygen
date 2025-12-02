package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.registry.BOOxygenSources;
import com.sierravanguard.beyond_oxygen.utils.OxygenManager;
import com.sierravanguard.beyond_oxygen.utils.OxygenSource;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;

//config values that should be automatically synchronized to the client.
@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BOServerConfig {
    private static final ForgeConfigSpec.IntValue HELD_ITEMS_OXYGEN_PRIORITY;
    private static final ForgeConfigSpec.IntValue INVENTORY_OXYGEN_PRIORITY;
    private static final ForgeConfigSpec.IntValue ARMOR_OXYGEN_PRIORITY;
    static final ForgeConfigSpec SPEC;

    //we instantiate all config in the class constructor here so that the builder can be released from memory after creating the config spec.
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        HELD_ITEMS_OXYGEN_PRIORITY = builder
                .comment("Priority for consuming oxygen from held items. Use " + Integer.MIN_VALUE + " to disable.")
                .defineInRange("heldItemsOxygenPriority", BOOxygenSources.DEFAULT_PRIORITY_HELD_ITEMS, Integer.MIN_VALUE, Integer.MAX_VALUE);
        INVENTORY_OXYGEN_PRIORITY = builder
                .comment("Priority for consuming oxygen from items in the player inventory. Use " + Integer.MIN_VALUE + " to disable.")
                .defineInRange("playerInventoryOxygenPriority", BOOxygenSources.DEFAULT_PRIORITY_PLAYER_INVENTORY, Integer.MIN_VALUE, Integer.MAX_VALUE);
        ARMOR_OXYGEN_PRIORITY = builder
                .comment("Priority for consuming oxygen from armor items. Use " + Integer.MIN_VALUE + " to disable.")
                .defineInRange("armorOxygenPriority", BOOxygenSources.DEFAULT_PRIORITY_ARMOR, Integer.MIN_VALUE, Integer.MAX_VALUE);
        SPEC = builder.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC && SPEC.isLoaded()) loadConfig();
    }

    //we load all of the config values into more friendly versions here, and can process custom syntax issues.
    public static void loadConfig() {
        BOOxygenSources.HELD_ITEMS.get().setPriority(HELD_ITEMS_OXYGEN_PRIORITY.get());
        BOOxygenSources.INVENTORY.get().setPriority(INVENTORY_OXYGEN_PRIORITY.get());
        BOOxygenSources.ARMOR.get().setPriority(ARMOR_OXYGEN_PRIORITY.get());
    }
}
