package com.sierravanguard.beyond_oxygen.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

public final class BOKeybindings {
    public static final BOKeybindings INSTANCE = new BOKeybindings();
    private static final String CATEGORY = "key.categories.beyond_oxygen";
    public static final KeyMapping TOGGLE_HELMET = new KeyMapping(
            "key.beyond_oxygen.toggle_helmet",
            KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_H, -1),
            CATEGORY
    );
    private BOKeybindings() {}
}

