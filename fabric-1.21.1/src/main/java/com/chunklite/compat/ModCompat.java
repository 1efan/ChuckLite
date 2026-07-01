package com.chunklite.compat;

/**
 * Loader-agnostic checks for which other mods are present, so ChunkLite can step aside when a
 * specialist is already doing the same job. Uses reflection (the same approach as the config path
 * resolver) so this one shared source compiles and runs on Forge, NeoForge, and Fabric without
 * importing any loader's classes directly.
 */
public final class ModCompat {

    private ModCompat() {
    }

    private static Boolean sodiumLike;

    /** True if Sodium or one of its ports (Embeddium, Rubidium) is installed and owns chunk rendering. */
    public static boolean sodiumPresent() {
        if (sodiumLike == null) {
            sodiumLike = isLoaded("sodium") || isLoaded("embeddium") || isLoaded("rubidium");
        }
        return sodiumLike;
    }

    public static boolean isLoaded(String modId) {
        return forgeLoaded("net.minecraftforge.fml.ModList", modId)
            || forgeLoaded("net.neoforged.fml.ModList", modId)
            || fabricLoaded(modId);
    }

    private static boolean forgeLoaded(String modListClass, String modId) {
        try {
            Class<?> modList = Class.forName(modListClass);
            Object instance = modList.getMethod("get").invoke(null);
            if (instance != null) {
                return (boolean) modList.getMethod("isLoaded", String.class).invoke(instance, modId);
            }
        } catch (ReflectiveOperationException loaderNotPresent) {
            // Not this loader; fall through to the next check.
        }
        return false;
    }

    private static boolean fabricLoaded(String modId) {
        try {
            Class<?> loader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = loader.getMethod("getInstance").invoke(null);
            if (instance != null) {
                return (boolean) loader.getMethod("isModLoaded", String.class).invoke(instance, modId);
            }
        } catch (ReflectiveOperationException loaderNotPresent) {
            // Not Fabric.
        }
        return false;
    }
}
