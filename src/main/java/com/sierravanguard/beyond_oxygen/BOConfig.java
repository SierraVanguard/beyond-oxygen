    package com.sierravanguard.beyond_oxygen;

    import net.minecraftforge.common.ForgeConfigSpec;
    import net.minecraftforge.eventbus.api.SubscribeEvent;
    import net.minecraftforge.fml.common.Mod;
    import net.minecraftforge.fml.event.config.ModConfigEvent;

    @Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class BOConfig {
        private static final ForgeConfigSpec.ConfigValue<Integer> VENT_RANGE;
        private static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_CONSUMPTION;

        private static final ForgeConfigSpec.ConfigValue<Integer> BUBBLE_MAX_RADIUS;
        private static final ForgeConfigSpec.ConfigValue<Integer> TIME_TO_IMPLODE;

        private static final ForgeConfigSpec.BooleanValue UNBREATHABLE_DIMENSIONS;
        private static final ForgeConfigSpec.BooleanValue COLD_DIMENSIONS;
        private static final ForgeConfigSpec.BooleanValue HOT_DIMENSIONS;
        private static final ForgeConfigSpec.BooleanValue ENABLE_BABY_MODE;
        static final ForgeConfigSpec SPEC;


        //we instantiate all config in the class constructor here so that the builder can be released from memory after creating the config spec.
        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            VENT_RANGE = builder
                    .comment("Max range of vent, high value CAN AND WILL cause lag.")
                    .define("ventRange", 2048);
            ENABLE_BABY_MODE = builder
                    .comment("Enables baby mode (Also known as ZAVODCHANIN mode, turns off oxygen requirements for vents)")
                    .define("enableBabyMode", false);
            OXYGEN_CONSUMPTION = builder
                    .comment("How many oxygen units in 1 mb")
                    .define("oxygenConsumption", 10);

            BUBBLE_MAX_RADIUS = builder
                    .comment("Maximum radius of bubble generators")
                    .defineInRange("bubbleMaxRadius", 5, 5, 20);
            TIME_TO_IMPLODE = builder
                    .comment("How many ticks (1 second ~ 20 ticks) for someone to IMPLODE from lack of air? Do not set lower than 15, or you may experience flicker.")
                    .define("timeToImplode", 10);

            UNBREATHABLE_DIMENSIONS = builder
                    .comment("Enable Beyond Oxygen unbreathable dimensions")
                    .define("unbreathable_dimensions", true);
            COLD_DIMENSIONS = builder
                    .comment("Enable Beyond Oxygen cold dimensions")
                    .define("cold_dimensions", true);
            HOT_DIMENSIONS = builder
                    .comment("Enable Beyond Oxygen hot dimensions")
                    .define("hot_dimensions", true);

            SPEC = builder.build();
        }

        @SubscribeEvent
        static void onLoad(final ModConfigEvent event) {
            if (event.getConfig().getSpec() == SPEC && SPEC.isLoaded()) loadConfig();
        }

        private static int ventRange;
        private static int oxygenConsumption;

        private static int bubbleMaxRadius;
        private static int timeToImplode;

        private static boolean enableUnbreathableDimensions;
        private static boolean enableColdDimensions;
        private static boolean enableHotDimensions;
        private static boolean babyMode;
        //we load all the config values into more friendly versions here, and can process custom syntax issues.
        public static void loadConfig() {
            babyMode = ENABLE_BABY_MODE.get();
            ventRange = VENT_RANGE.get();
            oxygenConsumption = OXYGEN_CONSUMPTION.get();

            bubbleMaxRadius = BUBBLE_MAX_RADIUS.get();
            timeToImplode = TIME_TO_IMPLODE.get();

            enableUnbreathableDimensions = UNBREATHABLE_DIMENSIONS.get();
            enableColdDimensions = COLD_DIMENSIONS.get();
            enableHotDimensions = HOT_DIMENSIONS.get();
        }
        public static boolean getBabyMode(){ return babyMode;}

        public static int getVentRange() {
            return ventRange;
        }

        public static int getOxygenConsumption() {
            return oxygenConsumption;
        }

        public static int getTimeToImplode() {
            return timeToImplode;
        }

        public static int getBubbleMaxRadius() {
            return bubbleMaxRadius;
        }

        public static boolean enableUnbreathableDimensions() {
            return enableUnbreathableDimensions;
        }

        public static boolean enableColdDimensions() {
            return enableColdDimensions;
        }

        public static boolean enableHotDimensions() {
            return enableHotDimensions;
        }
    }
