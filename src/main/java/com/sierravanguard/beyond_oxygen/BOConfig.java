    package com.sierravanguard.beyond_oxygen;

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
        public static final ForgeConfigSpec.ConfigValue<Integer> VENT_RANGE = BUILDER.comment("Max range of vent, high value CAN AND WILL cause lag.").define("ventRange", 2048);
        public static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_TANK_CAPACITY = BUILDER.comment("Max amount of oxygen that oxygen tank can contain (in mb)").define("oxygenTankCapacity", 1200);
        public static final ForgeConfigSpec.ConfigValue<Integer> OXYGEN_CONSUMPTION = BUILDER.comment("How many oxygen units in 1 mb").define("oxygenConsumption", 10);
        public static final ForgeConfigSpec.ConfigValue<Integer> VENT_CONSUMPTION = BUILDER.comment("How many blocks vent can fill with 1 oxygen unit").define("ventConsumption", 20);

        public static final ForgeConfigSpec.ConfigValue<Integer> BUBBLE_MAX_RADIUS = BUILDER
                .comment("Maximum radius of bubble generators")
                .defineInRange("bubbleMaxRadius", 5, 5, 20);
        public static final ForgeConfigSpec.ConfigValue<Integer> TIME_TO_IMPLODE = BUILDER.comment("How many ticks (1 second ~ 20 ticks) for someone to IMPLODE from lack of air? Do not set lower than 15, or you may experience flicker.").define("timeToImplode", 10);

        static final ForgeConfigSpec SPEC = BUILDER.build();
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
            return VENT_CONSUMPTION.get();
        }

        public static int getBubbleMaxRadius(){return BUBBLE_MAX_RADIUS.get();}
    }
