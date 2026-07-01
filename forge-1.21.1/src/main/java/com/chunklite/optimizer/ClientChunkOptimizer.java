package com.chunklite.optimizer;

import com.chunklite.ChuckLiteConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientChunkOptimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

    // live optimizer handle so loader-agnostic mixins reach it without touching the entrypoint class
    public static ClientChunkOptimizer active;

    public final ChunkLoadThrottle throttle = new ChunkLoadThrottle();
    public final FrameTimeTracker frameTime = new FrameTimeTracker();
    public final AdaptiveBudget buildBudget = new AdaptiveBudget(32);

    private int tickCount = 0;
    private boolean joining = true;
    private int joinTicks = 0;
    private int gcCooldown = 0;

    public ClientChunkOptimizer() {
        active = this;
    }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        tickCount++;
        throttle.tick();

        if (gcCooldown > 0) gcCooldown--;

        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        if (level == null || player == null) {
            joining = true;
            joinTicks = 0;
            return;
        }

        if (joining) {
            joinTicks++;
            if (joinTicks > 200) {
                joining = false;
                LOGGER.info("Join-flood window ended.");
            }
        }

        if (ChuckLiteConfig.directionalUnload() && tickCount % 4 == 0 && !joining) {
            directionalUnload(level, player);
        }

        if (ChuckLiteConfig.memoryAware() && tickCount % 20 == 0) {
            memoryPressureCheck(level, player);
        }
    }

    // feed one frame to the budget. runs on render thread before the upload gate reads it
    public void onFrame(long nowNanos) {
        frameTime.onFrame(nowNanos);
        if (ChuckLiteConfig.adaptiveThrottle()) {
            double targetMillis = 1000.0 / Math.max(1, ChuckLiteConfig.throttleTargetFps());
            buildBudget.update(
                frameTime.averageMillis(),
                targetMillis,
                ChuckLiteConfig.throttleMinPerFrame(),
                ChuckLiteConfig.throttleMaxPerFrame()
            );
        }
    }

    public void onDisconnect() {
        throttle.clear();
        frameTime.reset();
        buildBudget.snap(32);
        joining = true;
        joinTicks = 0;
        gcCooldown = 0;
        LOGGER.debug("ChuckLite state reset (disconnect).");
    }

    public boolean isJoining() {
        return joining;
    }

    // view radius via the public client option, not the chunk cache internals
    private static int viewRadius() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance();
    }

    private void directionalUnload(ClientLevel level, LocalPlayer player) {
        ClientChunkCache cache = level.getChunkSource();
        if (cache == null) return;
        int r = viewRadius();

        double lookX = player.getLookAngle().x;
        double lookZ = player.getLookAngle().z;
        double lookLen = Math.sqrt(lookX * lookX + lookZ * lookZ);
        if (lookLen < 0.001) return;

        double lookNormX = lookX / lookLen;
        double lookNormZ = lookZ / lookLen;
        double retentionCos = Math.cos(Math.toRadians(
                ChuckLiteConfig.forwardRetentionAngle() / 2.0));

        int centerCX = player.blockPosition().getX() >> 4;
        int centerCZ = player.blockPosition().getZ() >> 4;
        int unloaded = 0;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist < r - 1) continue;

                int cx = centerCX + dx;
                int cz = centerCZ + dz;
                if (!cache.hasChunk(cx, cz)) continue;

                double toCX = dx * 16.0 + 8.0;
                double toCZ = dz * 16.0 + 8.0;
                double toLen = Math.sqrt(toCX * toCX + toCZ * toCZ);
                if (toLen < 0.5) continue;

                double dot = (lookNormX * toCX / toLen) + (lookNormZ * toCZ / toLen);
                if (dot < retentionCos) {
                    cache.drop(new ChunkPos(cx, cz));
                    unloaded++;
                }
            }
        }

        if (unloaded > 0) {
            LOGGER.debug("Directional unload: dropped {} chunks", unloaded);
        }
    }

    private void memoryPressureCheck(ClientLevel level, LocalPlayer player) {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();
        double usedPct = 100.0 * used / max;

        if (usedPct < ChuckLiteConfig.memoryThresholdPct()) return;

        ClientChunkCache cache = level.getChunkSource();
        if (cache == null) return;
        int r = viewRadius();
        int keepRadius = Math.max(2, r / 2);
        int playerCX = player.blockPosition().getX() >> 4;
        int playerCZ = player.blockPosition().getZ() >> 4;

        int unloaded = 0;
        int limit = ChuckLiteConfig.aggressiveUnloadCount();

        for (int dx = -r; dx <= r && unloaded < limit; dx++) {
            for (int dz = -r; dz <= r && unloaded < limit; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist <= keepRadius) continue;

                int cx = playerCX + dx;
                int cz = playerCZ + dz;
                if (cache.hasChunk(cx, cz)) {
                    cache.drop(new ChunkPos(cx, cz));
                    unloaded++;
                }
            }
        }

        if (unloaded > 0) {
            LOGGER.warn("Memory pressure ({}%): unloaded {} chunks",
                    Math.round(usedPct), unloaded);
            if (gcCooldown <= 0) {
                System.gc();
                gcCooldown = 600;
            }
        }
    }

    public String getStats() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return "No world loaded.";

        ClientChunkCache cache = mc.level.getChunkSource();
        if (cache == null) return "Chunk source not available.";

        int r = viewRadius();
        int loaded = cache.getLoadedChunksCount();
        int capacity = (2 * r + 1) * (2 * r + 1);

        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();

        return String.format(
                "ChuckLite Stats%n" +
                "  View radius : %d%n" +
                "  Loaded chunks: %d / %d%n" +
                "  Frame time : %.1f ms (%.0f fps)%n" +
                "  Build budget: %d / frame%n" +
                "  Throttle pending: %d%n" +
                "  Heap : %.0f / %.0f MB (%.0f%%)%n" +
                "  Joining: %s",
                r,
                loaded, capacity,
                frameTime.averageMillis(), frameTime.averageFps(),
                buildBudget.perFrame(),
                throttle.pendingCount(),
                used / 1_048_576.0, max / 1_048_576.0,
                100.0 * used / max,
                joining ? "yes" : "no"
        );
    }

    public int forceUnload(int keepRadius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return 0;

        ClientChunkCache cache = mc.level.getChunkSource();
        if (cache == null) return 0;

        int r = viewRadius();
        int playerCX = mc.player.blockPosition().getX() >> 4;
        int playerCZ = mc.player.blockPosition().getZ() >> 4;
        int unloaded = 0;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist <= keepRadius) continue;

                int cx = playerCX + dx;
                int cz = playerCZ + dz;
                if (cache.hasChunk(cx, cz)) {
                    cache.drop(new ChunkPos(cx, cz));
                    unloaded++;
                }
            }
        }
        return unloaded;
    }
}
