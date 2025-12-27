    package com.sierravanguard.beyond_oxygen.compat.curios;

    import com.sierravanguard.beyond_oxygen.BeyondOxygen;
    import com.sierravanguard.beyond_oxygen.registry.BOOxygenSources;
    import com.sierravanguard.beyond_oxygen.tags.BOItemTags;
    import net.minecraft.world.entity.LivingEntity;
    import net.minecraft.world.item.ItemStack;
    import net.minecraftforge.common.ForgeConfigSpec;
    import net.minecraftforge.common.util.LazyOptional;
    import net.minecraftforge.eventbus.api.SubscribeEvent;
    import net.minecraftforge.fml.common.Mod;
    import net.minecraftforge.fml.event.config.ModConfigEvent;
    import top.theillusivec4.curios.api.CuriosApi;
    import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
    import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
    import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

    import java.util.*;
    import java.util.stream.IntStream;
    import java.util.stream.Stream;

    //TODO manually register the config load event?
    @Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class BOCuriosCompatServerConfig {
        private static final ForgeConfigSpec.BooleanValue IS_BLACKLIST;
        private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_SLOTS;
        private static final ForgeConfigSpec.ConfigValue<Integer> CURIOS_OXYGEN_PRIORITY;

        static final ForgeConfigSpec SPEC;

        //we instantiate all config in the class constructor here so that the builder can be released from memory after creating the config spec.
        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            IS_BLACKLIST = builder
                    .comment("Whether to treat the list of slots as a blacklist instead of a whitelist")
                    .define("isBlacklist", false);
            ALLOWED_SLOTS = builder
                    .comment("What slots to allow (or disallow) the consumption of oxygen from. Keep in mind the items will need to be in the " + BOItemTags.BREATHABLES.location().toString() + " tag.")
                    .defineList("allowedSlots", List.of("back"), t -> t instanceof String);
            CURIOS_OXYGEN_PRIORITY = builder
                    .comment("Priority for consuming oxygen from curios. Use " + Integer.MIN_VALUE + " to disable.")
                    .define("curiosOxygenPriority", BOCuriosOxygenSources.DEFAULT_PRIORITY_CURIOS);

            SPEC = builder.build();
        }

        @SubscribeEvent
        static void onLoad(final ModConfigEvent event) {
            if (event.getConfig().getSpec() == SPEC && SPEC.isLoaded()) loadConfig();
        }

        private static boolean isBlacklist;
        private static Set<String> allowedSlots;

        //we load all the config values into more friendly versions here, and can process custom syntax issues.
        public static void loadConfig() {
            isBlacklist = IS_BLACKLIST.get();
            allowedSlots = new HashSet<>(ALLOWED_SLOTS.get());
            BOCuriosOxygenSources.CURIOS.get().setPriority(CURIOS_OXYGEN_PRIORITY.get());
        }

        public static Stream<ItemStack> getCurios(LivingEntity entity) {
            return CuriosApi
                    .getCuriosInventory(entity)
                    .lazyMap(curios -> {
                        if (isBlacklist) {
                            return curios
                                    .getCurios()
                                    .entrySet()
                                    .stream()
                                    .filter(e -> !allowedSlots.contains(e.getKey()))
                                    .flatMap(e -> getStacks(e.getValue()));
                        } else {
                            return allowedSlots
                                    .stream()
                                    .map(curios::getStacksHandler)
                                    .filter(Optional::isPresent)
                                    .flatMap(curioStacksHandler -> getStacks(curioStacksHandler.get()));
                        }
                    }).orElse(Stream.empty());
        }

        private static Stream<ItemStack> getStacks(ICurioStacksHandler curioStacksHandler) {
            IDynamicStackHandler dynamicStackHandler = curioStacksHandler.getStacks();
            return IntStream
                    .range(0, dynamicStackHandler.getSlots())
                    .mapToObj(dynamicStackHandler::getStackInSlot)
                    .filter(stack -> !stack.isEmpty());
        }
    }
