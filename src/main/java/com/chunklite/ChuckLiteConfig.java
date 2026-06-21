package com.chunklite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Simple {@code .properties}-based configuration for ChuckLite.
 *
 * <p>The config file lives at {@code .minecraft/config/chuck-lite.properties}.
 * If the file is missing, defaults are written on first access. Values are
 * re-read on every access so changes take effect without a restart.</p>
 *
 * <p>This uses plain Java {@link Properties} — no loader-specific APIs.
 * Works on both Forge and Fabric without recompilation.</p>
 */
public final class ChuckLiteConfig {

    private static final Logger LOG = LoggerFactory.getLogger("ChuckLite/Config");

    private static final String FILE_NAME = "chuck-lite.properties";

    /**
     * Resolve the config path at runtime, trying loader-specific APIs
     * before falling back to {@code ./config/}. Cached after first call.
     */
    private static Path CONFIG_PATH = null;

    /** Cached properties; re-read from disk when the file timestamp changes. */
    private static final Properties PROPS = new Properties();
    private static long lastModified = 0;

    /** Resolve the Minecraft config directory at runtime. Result is cached. */
    private static synchronized Path resolveConfigPath() {
        if (CONFIG_PATH != null) return CONFIG_PATH;

        // Try Forge FMLPaths first.
        try {
            Class<?> fmlPaths = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
            // FMLPaths.CONFIGDIR is a public static Path field in Forge 1.20+
            Path configDir = (Path) fmlPaths.getField("CONFIGDIR").get(null);
            CONFIG_PATH = configDir.resolve(FILE_NAME);
            LOG.debug("Resolved config via Forge FMLPaths: {}", CONFIG_PATH);
            return CONFIG_PATH;
        } catch (Exception ignored) { /* not Forge */ }

        // Try Fabric Loader.
        try {
            Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoader.getMethod("getInstance").invoke(null);
            Path configDir = (Path) instance.getClass()
                    .getMethod("getConfigDir").invoke(instance);
            CONFIG_PATH = configDir.resolve(FILE_NAME);
            LOG.debug("Resolved config via FabricLoader: {}", CONFIG_PATH);
            return CONFIG_PATH;
        } catch (Exception ignored) { /* not Fabric */ }

        // Fallback: running directory / config (works in dev and most launchers).
        CONFIG_PATH = Paths.get("config", FILE_NAME).toAbsolutePath();
        LOG.debug("Resolved config via fallback: {}", CONFIG_PATH);
        return CONFIG_PATH;
    }

    // ── Defaults ──────────────────────────────────────────────

    private static void setDefaults(Properties p) {
        // Throttling
        p.putIfAbsent("throttle.enabled", "true");
        p.putIfAbsent("throttle.maxPerTick", "12");

        // Directional unloading
        p.putIfAbsent("directional.enabled", "true");
        p.putIfAbsent("directional.retentionAngle", "120");

        // Memory pressure
        p.putIfAbsent("memory.enabled", "true");
        p.putIfAbsent("memory.thresholdPercent", "75");
        p.putIfAbsent("memory.aggressiveUnloadCount", "8");

        // Render distance override
        p.putIfAbsent("renderDistance.enabled", "false");
        p.putIfAbsent("renderDistance.min", "2");
        p.putIfAbsent("renderDistance.max", "16");
    }

    /** Load (or reload) properties from disk. Called lazily on every access. */
    private static synchronized void ensureLoaded() {
        Path configPath = resolveConfigPath();
        if (!Files.exists(configPath)) {
            setDefaults(PROPS);
            save();
            try {
                lastModified = Files.getLastModifiedTime(configPath).toMillis();
            } catch (IOException e) { lastModified = System.currentTimeMillis(); }
            LOG.info("Wrote default config to {}", configPath);
            return;
        }

        try {
            long modTime = Files.getLastModifiedTime(configPath).toMillis();
            if (modTime > lastModified) {
                try (InputStream in = Files.newInputStream(configPath)) {
                    PROPS.clear();
                    PROPS.load(in);
                }
                setDefaults(PROPS); // fill in any missing keys
                lastModified = modTime;
            }
        } catch (IOException e) {
            LOG.warn("Failed to read config: {}", e.getMessage());
        }
    }

    /** Persist current properties to disk. */
    private static synchronized void save() {
        Path configPath = resolveConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException ignored) { /* config dir exists */ }
        try (OutputStream out = Files.newOutputStream(configPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            PROPS.store(out, "ChuckLite Configuration — changes take effect immediately");
        } catch (IOException e) {
            LOG.error("Failed to save config: {}", e.getMessage());
        }
    }

    /** Force re-read from disk (called by /chunk-lite reload). */
    public static void reload() {
        lastModified = 0;
        ensureLoaded();
        LOG.info("Config reloaded from disk.");
    }

    // ── Accessors ─────────────────────────────────────────────

    private static boolean getBool(String key, boolean def) {
        ensureLoaded();
        return Boolean.parseBoolean(PROPS.getProperty(key, Boolean.toString(def)));
    }

    private static int getInt(String key, int def, int min, int max) {
        ensureLoaded();
        try {
            int val = Integer.parseInt(PROPS.getProperty(key, Integer.toString(def)));
            return Math.max(min, Math.min(max, val));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // ---- Throttling ----
    public static boolean throttleEnabled()       { return getBool("throttle.enabled", true); }
    public static int maxChunkLoadsPerTick()      { return getInt("throttle.maxPerTick", 12, 1, 50); }

    // ---- Directional ----
    public static boolean directionalUnload()     { return getBool("directional.enabled", true); }
    public static int forwardRetentionAngle()     { return getInt("directional.retentionAngle", 120, 60, 180); }

    // ---- Memory ----
    public static boolean memoryAware()           { return getBool("memory.enabled", true); }
    public static int memoryThresholdPct()        { return getInt("memory.thresholdPercent", 75, 50, 95); }
    public static int aggressiveUnloadCount()     { return getInt("memory.aggressiveUnloadCount", 8, 1, 30); }

    // ---- Render Distance ----
    public static boolean overrideRenderDistance(){ return getBool("renderDistance.enabled", false); }
    public static int minRenderDistance()         { return getInt("renderDistance.min", 2, 2, 32); }
    public static int maxRenderDistance()         { return getInt("renderDistance.max", 16, 4, 64); }
}
