package com.sierravanguard.beyond_oxygen.registry;

import com.sierravanguard.beyond_oxygen.client.renderer.AtmosphericTankCurioRenderer;
import com.sierravanguard.beyond_oxygen.client.renderer.LargeTankCurioRenderer;
import com.sierravanguard.beyond_oxygen.client.renderer.MediumTankCurioRenderer;
import com.sierravanguard.beyond_oxygen.client.renderer.SmallTankCurioRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class BOCurioRenderers {

    public static void register(EntityModelSet modelSet) {
        CuriosRendererRegistry.register(BOItems.MEDIUM_OXYGEN_TANK.get(), () -> new MediumTankCurioRenderer(modelSet));
        CuriosRendererRegistry.register(BOItems.SMALL_OXYGEN_TANK.get(), () -> new SmallTankCurioRenderer(modelSet));
        CuriosRendererRegistry.register(BOItems.LARGE_OXYGEN_TANK.get(), () -> new LargeTankCurioRenderer(modelSet));
        CuriosRendererRegistry.register(BOItems.ATMOSPHERIC_OXYGEN_TANK.get(), () -> new AtmosphericTankCurioRenderer(modelSet));
    }
}
