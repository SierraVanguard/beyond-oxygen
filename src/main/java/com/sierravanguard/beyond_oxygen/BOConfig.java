    package com.sierravanguard.beyond_oxygen;

    import com.sierravanguard.beyond_oxygen.compat.AdAstraConfigHelper;
    import net.minecraft.resources.ResourceLocation;
    import net.minecraftforge.common.ForgeConfigSpec;
    import net.minecraftforge.eventbus.api.SubscribeEvent;
    import net.minecraftforge.fml.common.Mod;
    import net.minecraftforge.fml.event.config.ModConfigEvent;

    import java.util.ArrayList;
    import java.util.List;

    @Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class BOConfig {
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec.ConfigValue<Integer> VENT_RANGE = BUILDER.comment("Max range of vent, high value can cause lags").define("ventRange", 2048);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> OXYGEN_FLUIDS = BUILDER.comment("List of fluids to accept as oxygen").defineListAllowEmpty("oxygenFluids", List.of("mekanism:oxygen"), s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_TANK_CAPACITY = BUILDER.comment("Max amount of oxygen that oxygen tank can contain (in mb)").define("oxygenTankCapacity", 1200);
        public static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_CONSUMPTION = BUILDER.comment("How many oxygen units in 1 mb").define("oxygenConsumption", 10);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNBREATHABLE_DIMENSIONS = BUILDER.comment("List of unbreathable dimensions").defineListAllowEmpty("unbreathableDimensions", List.of("minecraft:the_end"), s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<Integer> VENT_CONSUMPTION = BUILDER.comment("How many blocks vent can fill with 1 oxygen unit").define("ventConsumption", 20);
        public static final ForgeConfigSpec.ConfigValue<String> SPACE_REPAIR_MATERIAL = BUILDER.comment("The item ID of the material to repair the spacesuit with").define("space_repair_material", "mekanism:ingot_steel");
        public static final ForgeConfigSpec.ConfigValue<String> CRYO_REPAIR_MATERIAL = BUILDER.comment("The item ID of the material to repair the cryo suit with").define("cryo_repair_material", "mekanism:ingot_refined_obsidian");
        public static final ForgeConfigSpec.ConfigValue<String> THERMAL_REPAIR_MATERIAL = BUILDER.comment("The item ID of the material to repair the thermal suit with").define("thermal_repair_material", "mekanism:ingot_refined_glowstone");
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_HELMETS = BUILDER.comment("List of item IDs that count as spacesuit helmets").defineListAllowEmpty("space_helmets", List.of("beyond_oxygen:spacesuit_helmet", "beyond_oxygen:cryo_suit_helmet", "beyond_oxygen:thermal_suit_helmet"), s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_CHESTPLATES = BUILDER.comment("List of item IDs that count as spacesuit chestplates").defineListAllowEmpty("space_chestplates", List.of("beyond_oxygen:spacesuit_chestplate", "beyond_oxygen:cryo_suit_chestplate", "beyond_oxygen:thermal_suit_chestplate"), s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_LEGGINGS = BUILDER.comment("List of item IDs that count as spacesuit leggings").defineListAllowEmpty("space_leggings", List.of("beyond_oxygen:spacesuit_leggings", "beyond_oxygen:cryo_suit_leggings", "beyond_oxygen:thermal_suit_leggings"), s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPACE_BOOTS = BUILDER.comment("List of item IDs that count as spacesuit boots").defineListAllowEmpty("space_boots", List.of("beyond_oxygen:spacesuit_boots", "beyond_oxygen:cryo_suit_boots", "beyond_oxygen:thermal_suit_boots"), s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CRYO_HELMETS =
                BUILDER.comment("List of item IDs that count as cryo suit helmets")
                        .defineListAllowEmpty("cryo_helmets",
                                List.of("beyond_oxygen:cryo_suit_helmet"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CRYO_CHESTPLATES =
                BUILDER.comment("List of item IDs that count as cryo suit chestplates")
                        .defineListAllowEmpty("cryo_chestplates",
                                List.of("beyond_oxygen:cryo_suit_chestplate"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CRYO_LEGGINGS =
                BUILDER.comment("List of item IDs that count as cryo suit leggings")
                        .defineListAllowEmpty("cryo_leggings",
                                List.of("beyond_oxygen:cryo_suit_leggings"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CRYO_BOOTS =
                BUILDER.comment("List of item IDs that count as cryo suit boots")
                        .defineListAllowEmpty("cryo_boots",
                                List.of("beyond_oxygen:cryo_suit_boots"),
                                s -> s instanceof String);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THERMAL_HELMETS =
                BUILDER.comment("List of item IDs that count as thermal suit helmets")
                        .defineListAllowEmpty("thermal_helmets",
                                List.of("beyond_oxygen:thermal_suit_helmet"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THERMAL_CHESTPLATES =
                BUILDER.comment("List of item IDs that count as thermal suit chestplates")
                        .defineListAllowEmpty("thermal_chestplates",
                                List.of("beyond_oxygen:thermal_suit_chestplate"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THERMAL_LEGGINGS =
                BUILDER.comment("List of item IDs that count as thermal suit leggings")
                        .defineListAllowEmpty("thermal_leggings",
                                List.of("beyond_oxygen:thermal_suit_leggings"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THERMAL_BOOTS =
                BUILDER.comment("List of item IDs that count as thermal suit boots")
                        .defineListAllowEmpty("thermal_boots",
                                List.of("beyond_oxygen:thermal_suit_boots"),
                                s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<Integer> BUBBLE_MAX_RADIUS = BUILDER
                .comment("Maximum radius of bubble generators")
                .defineInRange("bubbleMaxRadius", 5, 5, 20);
        public static final ForgeConfigSpec.ConfigValue<Integer> TIME_TO_IMPLODE = BUILDER.comment("How many ticks (1 second = 20 ticks) for someone to IMPLODE from lack of air?").define("timeToImplode", 10);
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HOT_DIMENSIONS =
                BUILDER.comment("Dimensions that deal heat damage")
                        .defineListAllowEmpty("hotDimensions", List.of("minecraft:the_nether"), s -> s instanceof String);

        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> COLD_DIMENSIONS =
                BUILDER.comment("Dimensions that deal cold damage")
                        .defineListAllowEmpty("coldDimensions", List.of("minecraft:the_end"), s -> s instanceof String);


        public static int ventRange;
        public static int oxygenConsumption;
        public static int oxygenTankCapacity;
        public static int ventConsumption;
        public static List<ResourceLocation> oxygenFluids = new ArrayList<>();
        public static ResourceLocation spaceRepairMaterial;
        public static ResourceLocation cryoRepairMaterial;
        public static ResourceLocation thermalRepairMaterial;
        public static List<String> spaceHelmets;
        public static List<String> spaceChestplates;
        public static List<String> spaceLeggings;
        public static List<String> spaceBoots;
        public static float bubbleMaxRadius;
        public static int timeToImplode;
        public static List<ResourceLocation> unbreathableDimensions;
        public static List<ResourceLocation> hotDimensions;
        public static List<ResourceLocation> coldDimensions;
        public static List<String> coldSuit;
        public static List<String> hotSuit;
        static final ForgeConfigSpec SPEC = BUILDER.build();

        @SubscribeEvent
        static void onLoad(final ModConfigEvent event) {
            if (!event.getConfig().getSpec().equals(SPEC)) return;
            ventRange = VENT_RANGE.get();
            oxygenTankCapacity = OXYGEN_TANK_CAPACITY.get();
            oxygenConsumption = OXYGEN_CONSUMPTION.get();
            ventConsumption = VENT_CONSUMPTION.get();
            bubbleMaxRadius = BUBBLE_MAX_RADIUS.get();
            timeToImplode = TIME_TO_IMPLODE.get();
            oxygenFluids = toResourceLocationList(OXYGEN_FLUIDS.get());
            unbreathableDimensions = toResourceLocationList(UNBREATHABLE_DIMENSIONS.get());
            hotDimensions = toResourceLocationList(HOT_DIMENSIONS.get());
            coldDimensions = toResourceLocationList(COLD_DIMENSIONS.get());
            spaceRepairMaterial = toResourceLocation(SPACE_REPAIR_MATERIAL.get());
            cryoRepairMaterial = toResourceLocation(CRYO_REPAIR_MATERIAL.get());
            thermalRepairMaterial = toResourceLocation(THERMAL_REPAIR_MATERIAL.get());


            spaceHelmets = new ArrayList<>(SPACE_HELMETS.get());
            spaceChestplates = new ArrayList<>(SPACE_CHESTPLATES.get());
            spaceLeggings = new ArrayList<>(SPACE_LEGGINGS.get());
            spaceBoots = new ArrayList<>(SPACE_BOOTS.get());
            coldSuit = new ArrayList<>();
            coldSuit.addAll(CRYO_HELMETS.get());
            coldSuit.addAll(CRYO_CHESTPLATES.get());
            coldSuit.addAll(CRYO_LEGGINGS.get());
            coldSuit.addAll(CRYO_BOOTS.get());


            hotSuit = new ArrayList<>();
            hotSuit.addAll(THERMAL_HELMETS.get());
            hotSuit.addAll(THERMAL_CHESTPLATES.get());
            hotSuit.addAll(THERMAL_LEGGINGS.get());
            hotSuit.addAll(THERMAL_BOOTS.get());


            AdAstraConfigHelper.injectAdAstraDimensions();
        }

        private static ResourceLocation toResourceLocation(String id) {
            String[] parts = id.split(":");
            return new ResourceLocation(parts[0], parts[1]);
        }

        private static List<ResourceLocation> toResourceLocationList(List<? extends String> list) {
            List<ResourceLocation> result = new ArrayList<>();
            for (String s : list) result.add(toResourceLocation(s));
            return result;
        }


        public static int getOxygenTankCapacity() {
            return OXYGEN_TANK_CAPACITY.get();
        }

        public static int getVentRange() {
            return VENT_RANGE.get();
        }

        public static int getOxygenConsumption() {
            return OXYGEN_CONSUMPTION.get();
        }

        public static int getTimeToImplode() {
            return TIME_TO_IMPLODE.get();
        }

        public static int getVentConsumption() {
            return ventConsumption;
        }


        public static List<ResourceLocation> getOxygenFluids() {
            if (OXYGEN_FLUIDS == null) return List.of();
            List<ResourceLocation> fluids = new ArrayList<>();
            for (String id : OXYGEN_FLUIDS.get()) fluids.add(toResourceLocation(id));
            return fluids;
        }

        public static List<ResourceLocation> getUnbreathableDimensions() {
            if (UNBREATHABLE_DIMENSIONS == null) return List.of();
            List<ResourceLocation> result = new ArrayList<>();
            for (String id : UNBREATHABLE_DIMENSIONS.get()) result.add(toResourceLocation(id));
            return result;
        }

        public static List<ResourceLocation> getHotDimensions() {
            if (HOT_DIMENSIONS == null) return List.of();
            List<ResourceLocation> result = new ArrayList<>();
            for (String id : HOT_DIMENSIONS.get()) result.add(toResourceLocation(id));
            return result;
        }

        public static List<ResourceLocation> getColdDimensions() {
            if (COLD_DIMENSIONS == null) return List.of();
            List<ResourceLocation> result = new ArrayList<>();
            for (String id : COLD_DIMENSIONS.get()) result.add(toResourceLocation(id));
            return result;
        }

        public static ResourceLocation getSpaceRepairMaterial() {
            return toResourceLocation(SPACE_REPAIR_MATERIAL.get());
        }

        public static ResourceLocation getCryoRepairMaterial() {
            return toResourceLocation(CRYO_REPAIR_MATERIAL.get());
        }

        public static ResourceLocation getThermalRepairMaterial() {
            return toResourceLocation(THERMAL_REPAIR_MATERIAL.get());
        }
        public static List<ResourceLocation> getSpaceHelmets() {
            return toResourceLocationList(SPACE_HELMETS.get());
        }

        public static List<ResourceLocation> getSpaceChestplates() {
            return toResourceLocationList(SPACE_CHESTPLATES.get());
        }

        public static List<ResourceLocation> getSpaceLeggings() {
            return toResourceLocationList(SPACE_LEGGINGS.get());
        }

        public static List<ResourceLocation> getSpaceBoots() {
            return toResourceLocationList(SPACE_BOOTS.get());
        }


        public static List<ResourceLocation> getCryoHelmets() {
            return toResourceLocationList(CRYO_HELMETS.get());
        }

        public static List<ResourceLocation> getCryoChestplates() {
            return toResourceLocationList(CRYO_CHESTPLATES.get());
        }

        public static List<ResourceLocation> getCryoLeggings() {
            return toResourceLocationList(CRYO_LEGGINGS.get());
        }

        public static List<ResourceLocation> getCryoBoots() {
            return toResourceLocationList(CRYO_BOOTS.get());
        }


        public static List<ResourceLocation> getThermalHelmets() {
            return toResourceLocationList(THERMAL_HELMETS.get());
        }

        public static List<ResourceLocation> getThermalChestplates() {
            return toResourceLocationList(THERMAL_CHESTPLATES.get());
        }

        public static List<ResourceLocation> getThermalLeggings() {
            return toResourceLocationList(THERMAL_LEGGINGS.get());
        }

        public static List<ResourceLocation> getThermalBoots() {
            return toResourceLocationList(THERMAL_BOOTS.get());
        }


        public static List<ResourceLocation> getColdSuit() {
            List<ResourceLocation> result = new ArrayList<>();
            result.addAll(getCryoHelmets());
            result.addAll(getCryoChestplates());
            result.addAll(getCryoLeggings());
            result.addAll(getCryoBoots());
            return result;
        }
        public static List<ResourceLocation> getHotSuit() {
            List<ResourceLocation> result = new ArrayList<>();
            result.addAll(getThermalHelmets());
            result.addAll(getThermalChestplates());
            result.addAll(getThermalLeggings());
            result.addAll(getThermalBoots());
            return result;
        }
    }
