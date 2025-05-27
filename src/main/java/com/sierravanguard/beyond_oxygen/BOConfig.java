package com.sierravanguard.beyond_oxygen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
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
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> OXYGEN_FLUIDS = BUILDER.comment("List of fluids to accept as oxygen").defineListAllowEmpty("oxygenFluids",List.of("mekanism:oxygen"),s->s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_TANK_CAPACITY = BUILDER.comment("Max amount of oxygen that oxygen tank can contain (in mb)").define("oxygenTankCapacity",1200);
    private static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_CONSUMPTION = BUILDER.comment("How many oxygen units in 1 mb").define("oxygenConsumption", 10);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNBREATHABLE_DIMENSIONS = BUILDER.comment("List of unbreathable dimensions").defineListAllowEmpty("unbreathableDimensions",List.of("minecraft:the_end"),s->s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<Integer> VENT_CONSUMPTION = BUILDER.comment("How many blocks vent can fill with 1 oxygen unit").define("ventConsumption", 20);
    private static final ForgeConfigSpec.ConfigValue<String> SPACE_REPAIR_MATERIAL = BUILDER.comment("The item ID of the material to repair the spacesuit with").define("space_repair_material", "mekanism:ingot_steel");
    private static final ForgeConfigSpec.ConfigValue<String> CRYO_REPAIR_MATERIAL = BUILDER.comment("The item ID of the material to repair the cryo suit with").define("cryo_repair_material", "mekanism:ingot_steel");
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_HELMETS = BUILDER.comment("List of item IDs that count as spacesuit helmets").defineListAllowEmpty("space_helmets", List.of("beyond_oxygen:spacesuit_helmet"), s -> s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_CHESTPLATES = BUILDER.comment("List of item IDs that count as spacesuit chestplates").defineListAllowEmpty("space_chestplates", List.of("beyond_oxygen:spacesuit_chestplate"), s -> s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_LEGGINGS = BUILDER.comment("List of item IDs that count as spacesuit leggings").defineListAllowEmpty("space_leggings", List.of("beyond_oxygen:spacesuit_leggings"), s -> s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_BOOTS = BUILDER.comment("List of item IDs that count as spacesuit boots").defineListAllowEmpty("space_boots", List.of("beyond_oxygen:spacesuit_boots"), s -> s instanceof String);
    private static final ForgeConfigSpec.ConfigValue<Integer> BUBBLE_MAX_RADIUS = BUILDER
            .comment("Maximum radius of bubble generators")
            .defineInRange("bubbleMaxRadius", 5, 5, 20); // Default 5, min 1, max 20
    static final ForgeConfigSpec SPEC = BUILDER.build();


    public static int ventRange;
    public static int oxygenConsumption;
    public static int oxygenTankCapacity;
    public static int ventConsumption;
    public static List<ResourceLocation> oxygenFluids;
    public static List<ResourceLocation> unbreathableDimensions;
    public static ResourceLocation spaceRepairMaterial;
    public static ResourceLocation cryoRepairMaterial;
    public static List<String> spaceHelmets;
    public static List<String> spaceChestplates;
    public static List<String> spaceLeggings;
    public static List<String> spaceBoots;
    public static float bubbleMaxRadius;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ventRange = VENT_RANGE.get();
        oxygenTankCapacity = OXYGEN_TANK_CAPACITY.get();
        List<? extends String> oxygenFluidConf = OXYGEN_FLUIDS.get();
        oxygenFluids = new ArrayList<>();
        for (String fluid : oxygenFluidConf){
            String[] pair = fluid.split(":");
            oxygenFluids.add(ResourceLocation.fromNamespaceAndPath(pair[0],pair[1]));
        }
        List<? extends String> unbreathableDimConf = UNBREATHABLE_DIMENSIONS.get();
        unbreathableDimensions = new ArrayList<>();
        for (String dim : unbreathableDimConf){
            String[] pair = dim.split(":");
            unbreathableDimensions.add(ResourceLocation.fromNamespaceAndPath(pair[0],pair[1]));
        }
        oxygenConsumption = OXYGEN_CONSUMPTION.get();
        ventConsumption = VENT_CONSUMPTION.get();
        String[] space_repair_material_pair = SPACE_REPAIR_MATERIAL.get().split(":");
        spaceRepairMaterial = ResourceLocation.fromNamespaceAndPath(space_repair_material_pair[0], space_repair_material_pair[1]);
        String[] cryo_repair_material_pair = CRYO_REPAIR_MATERIAL.get().split(":");
        cryoRepairMaterial = ResourceLocation.fromNamespaceAndPath(cryo_repair_material_pair[0], cryo_repair_material_pair[1]);
        spaceHelmets = new ArrayList<>(SPACE_HELMETS.get());
        spaceChestplates = new ArrayList<>(SPACE_CHESTPLATES.get());
        spaceLeggings = new ArrayList<>(SPACE_LEGGINGS.get());
        spaceBoots = new ArrayList<>(SPACE_BOOTS.get());
        bubbleMaxRadius = BUBBLE_MAX_RADIUS.get();

    }
    public static boolean isItemValidForSlot(ResourceLocation itemId, EquipmentSlot slot) {
        String id = itemId.toString();
        return switch (slot) {
            case HEAD -> spaceHelmets.contains(id);
            case CHEST -> spaceChestplates.contains(id);
            case LEGS -> spaceLeggings.contains(id);
            case FEET -> spaceBoots.contains(id);
            default -> false;
        };
    }

}
