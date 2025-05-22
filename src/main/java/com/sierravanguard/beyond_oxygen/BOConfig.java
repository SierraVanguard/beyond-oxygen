package com.sierravanguard.beyond_oxygen;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BOConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<Integer> VENT_RANGE = BUILDER.comment("Max range of vent, high value can cause lags").define("ventRange", 2048);

    private static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_TANK_CAPACITY = BUILDER.comment("Max amount of oxygen that oxygen tank can contain (in mb)").define("oxygenTankCapacity",1200);
    private static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_CONSUMPTION = BUILDER.comment("How many oxygen units in 1 mb").define("oxygenConsumption", 10);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNBREATHABLE_DIMENSIONS = BUILDER.comment("List of unbreathable dimensions").defineListAllowEmpty("unbreathableDimensions",List.of("minecraft:the_end"),s->s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<Integer> VENT_CONSUMPTION = BUILDER.comment("How many blocks vent can fill with 1 oxygen unit").define("ventConsumption", 20);

    static final ForgeConfigSpec SPEC = BUILDER.build();


    public static int ventRange;
    public static int oxygenConsumption;
    public static int oxygenTankCapacity;
    public static int ventConsumption;
    public static List<ResourceLocation> unbreathableDimensions;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ventRange = VENT_RANGE.get();
        oxygenTankCapacity = OXYGEN_TANK_CAPACITY.get();
        List<? extends String> unbreathableDimConf = UNBREATHABLE_DIMENSIONS.get();
        unbreathableDimensions = new ArrayList<>();
        for (String dim : unbreathableDimConf){
            String[] pair = dim.split(":");
            unbreathableDimensions.add(new ResourceLocation(pair[0],pair[1]));
        }
        oxygenConsumption = OXYGEN_CONSUMPTION.get();
        ventConsumption = VENT_CONSUMPTION.get();
    }
}
