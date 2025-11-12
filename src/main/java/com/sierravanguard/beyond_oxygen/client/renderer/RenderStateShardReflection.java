package com.sierravanguard.beyond_oxygen.client.renderer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class RenderStateShardReflection {
    private static final Logger LOGGER = LoggerFactory.getLogger("BeyondOxygen/RenderStateShardReflection");
    private static final Map<String, Object> CACHE = new HashMap<>();
    private static boolean initialized = false;

    private RenderStateShardReflection() {}

    public static synchronized void initializeSafe() {
        if (initialized) return;
        try {
            Class<RenderStateShard> clazz = RenderStateShard.class;
            cache(clazz, "f_173076_", "RENDERTYPE_WATER_MASK_SHADER"); 
            cache(clazz, "f_110116_", "DEPTH_WRITE"); 
            cache(clazz, "f_110147_", "NO_TEXTURE"); 
            cache(clazz, "f_110110_", "NO_CULL"); 
            cache(clazz, "f_110113_", "LEQUAL_DEPTH_TEST"); 
            cache(clazz, "f_173104_", "POSITION_COLOR_SHADER"); 
            cache(clazz, "f_110114_", "COLOR_DEPTH_WRITE"); 

            initialized = true;
            LOGGER.debug("RenderStateShardReflection initialized successfully.");
        } catch (Throwable t) {
            LOGGER.warn("RenderStateShardReflection failed during initialization: {}", t.toString());
        }
    }

    private static <T> T get(String name, Class<T> type) {
        if (!initialized) initializeSafe();
        Object value = CACHE.get(name);
        if (value == null) {
            LOGGER.warn("RenderStateShard constant {} is missing or unavailable.", name);
        }
        return type.cast(value);
    }

    public static RenderStateShard.ShaderStateShard waterMaskShader() {
        return get("RENDERTYPE_WATER_MASK_SHADER", RenderStateShard.ShaderStateShard.class);
    }

    public static RenderStateShard.WriteMaskStateShard depthWrite() {
        return get("DEPTH_WRITE", RenderStateShard.WriteMaskStateShard.class);
    }

    public static RenderStateShard.EmptyTextureStateShard noTexture() {
        return get("NO_TEXTURE", RenderStateShard.EmptyTextureStateShard.class);
    }

    public static RenderStateShard.CullStateShard noCull() {
        return get("NO_CULL", RenderStateShard.CullStateShard.class);
    }

    public static RenderStateShard.DepthTestStateShard lequalDepthTest() {
        return get("LEQUAL_DEPTH_TEST", RenderStateShard.DepthTestStateShard.class);
    }

    public static RenderStateShard.ShaderStateShard positionColorShader() {
        return get("POSITION_COLOR_SHADER", RenderStateShard.ShaderStateShard.class);
    }

    public static RenderStateShard.WriteMaskStateShard colorDepthWrite() {
        return get("COLOR_DEPTH_WRITE", RenderStateShard.WriteMaskStateShard.class);
    }

    private static void cache(Class<?> clazz, String obfName, String readableKey) {
        try {
            Field f = ObfuscationReflectionHelper.findField(clazz, obfName);
            Object value = f.get(null);
            CACHE.put(readableKey, value);
        } catch (Exception e) {
            LOGGER.warn("Failed to reflect RenderStateShard field {} ({}): {}", readableKey, obfName, e.toString());
        }
    }
}
