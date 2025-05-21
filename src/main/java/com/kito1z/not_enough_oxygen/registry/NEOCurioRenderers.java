package com.kito1z.not_enough_oxygen.registry;

import com.kito1z.not_enough_oxygen.client.renderer.AtmosphericTankCurioRenderer;
import com.kito1z.not_enough_oxygen.client.renderer.LargeTankCurioRenderer;
import com.kito1z.not_enough_oxygen.client.renderer.MediumTankCurioRenderer;
import com.kito1z.not_enough_oxygen.client.renderer.SmallTankCurioRenderer;
import com.kito1z.not_enough_oxygen.registry.NEOItems;
import net.minecraft.client.model.geom.EntityModelSet;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class NEOCurioRenderers {

    public static void register(EntityModelSet modelSet) {
        CuriosRendererRegistry.register(NEOItems.MEDIUM_OXYGEN_TANK.get(), () -> new MediumTankCurioRenderer(modelSet));
        CuriosRendererRegistry.register(NEOItems.SMALL_OXYGEN_TANK.get(), () -> new SmallTankCurioRenderer(modelSet));
        CuriosRendererRegistry.register(NEOItems.LARGE_OXYGEN_TANK.get(), () -> new LargeTankCurioRenderer(modelSet));
        CuriosRendererRegistry.register(NEOItems.ATMOSPHERIC_OXYGEN_TANK.get(), () -> new AtmosphericTankCurioRenderer(modelSet));
    }
}
