package com.sierravanguard.beyond_oxygen.client.renderer;

import net.minecraft.client.renderer.RenderStateShard;

import java.lang.reflect.Field;

public class RenderStateShardReflection {
    public static final RenderStateShard.ShaderStateShard WATER_MASK_SHADER;
    public static final RenderStateShard.WriteMaskStateShard DEPTH_WRITE;
    public static final RenderStateShard.EmptyTextureStateShard NO_TEXTURE;
    public static final RenderStateShard.CullStateShard NO_CULL;

    // Debug states
    public static final RenderStateShard.ShaderStateShard POSITION_COLOR_SHADER;
    public static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE;
    public static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST;

    static {
        try {
            Class<RenderStateShard> clazz = RenderStateShard.class;

            WATER_MASK_SHADER = getStaticField(clazz, "RENDERTYPE_WATER_MASK_SHADER");
            DEPTH_WRITE = getStaticField(clazz, "DEPTH_WRITE");
            NO_TEXTURE = getStaticField(clazz, "NO_TEXTURE");
            NO_CULL = getStaticField(clazz, "NO_CULL");
            LEQUAL_DEPTH_TEST = getStaticField(clazz, "LEQUAL_DEPTH_TEST");
            POSITION_COLOR_SHADER = getStaticField(clazz, "POSITION_COLOR_SHADER");
            COLOR_DEPTH_WRITE = getStaticField(clazz, "COLOR_DEPTH_WRITE");

        } catch (Exception e) {
            throw new RuntimeException("Failed to reflect RenderStateShard constants", e);
        }
    }
    private static <T> T getStaticField(Class<?> clazz, String name) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(null);
    }
}
