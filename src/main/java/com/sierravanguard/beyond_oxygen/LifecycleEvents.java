package com.sierravanguard.beyond_oxygen;

import com.sierravanguard.beyond_oxygen.data.BOFluidTagsProvider;
import com.sierravanguard.beyond_oxygen.data.BOItemTagsProvider;
import com.sierravanguard.beyond_oxygen.data.BORecipesProvider;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondOxygen.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LifecycleEvents {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.getGenerator().addProvider(true, (DataProvider.Factory<BOItemTagsProvider>) output -> new BOItemTagsProvider(
                    output,
                    event.getLookupProvider(),
                    event.getExistingFileHelper()
            ));
            event.getGenerator().addProvider(true, (DataProvider.Factory<BOFluidTagsProvider>) output -> new BOFluidTagsProvider(
                    output,
                    event.getLookupProvider(),
                    event.getExistingFileHelper()
            ));
            event.getGenerator().addProvider(true, (DataProvider.Factory<BORecipesProvider>) BORecipesProvider::new);
        }
    }
}
