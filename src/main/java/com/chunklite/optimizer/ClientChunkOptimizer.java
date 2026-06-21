package com.chunklite.optimizer;

import com.chunklite.ChuckLiteConfig;
import com.chunklite.mixin.ChunkStorageAccessor;
import com.chunklite.mixin.ClientChunkCacheMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central coordinator for client-side chunk optimizations.
 */
public final class ClientChunkOptimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

    public final ChunkLoadThrottle throttle = new ChunkLoadThrottle();

    private int tickCount = 0;
    private boolean joining = true;
    private int joinTicks = 0;

    /** Cooldown between forced GC calls, in ticks (30 seconds at 20 tps). */
    private int gcCooldown = 0;

    // ── Public API ─────────────────────────────────────────────

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

    public void onDisconnect() {
        throttle.clear();
        joining = true;
        joinTicks = 0;
        gcCooldown = 0;
        LOGGER.debug("ChuckLite state reset (disconnect).");
    }

    public boolean isJoining() {
        return joining;
    }

    // ── Helpers ────────────────────────────────────────────────

    /** Null-safe storage accessor. Returns null during world transitions. */
    private static ChunkStorageAccessor storage(ClientLevel level) {
        ClientChunkCache cache = level.getChunkSource();
        if (cache == null) return null;
        Object raw = ClientChunkCacheMixin.getStorage(cache);
        return raw != null ? (ChunkStorageAccessor) raw : null;
    }

    /** Count loaded chunks by scanning the array. Cheap -- only called for stats. */
    private static int countLoaded(ChunkStorageAccessor s) {
        int r = s.chunklite$getViewRange();
        int cx = s.chunklite$getViewCenterX();
        int cz = s.chunklite$getViewCenterZ();
        int count = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (s.chunklite$getChunk(cx + dx, cz + dz) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    // ── Optimization routines ──────────────────────────────────

    private void directionalUnload(ClientLevel level, LocalPlayer player) {
        ChunkStorageAccessor s = storage(level);
        if (s == null) return;
        int r = s.chunklite$getViewRange();

        double lookX = player.getLookAngle().x;
        double lookZ = player.getLookAngle().z;
        double lookLen = Math.sqrt(lookX * lookX + lookZ * lookZ);
        if (lookLen < 0.001) return;

        double lookNormX = lookX / lookLen;
        double lookNormZ = lookZ / lookLen;
        double retentionCos = Math.cos(Math.toRadians(
                ChuckLiteConfig.forwardRetentionAngle() / 2.0));

        // Use the storage center, not player position, so the iteration
        // exactly covers the loaded chunk array. The center tracks the
        // player closely (updated by vanilla every tick).
        int centerCX = s.chunklite$getViewCenterX();
        int centerCZ = s.chunklite$getViewCenterZ();

        ClientChunkCache cache = level.getChunkSource();
        int unloaded = 0;

        // Only touch the outer ring (distance >= r - 1).
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist < r - 1) continue;

                int cx = centerCX + dx;
                int cz = centerCZ + dz;

                LevelChunk chunk = s.chunklite$getChunk(cx, cz);
                if (chunk == null) continue;

                // Direction to chunk from storage center (proxy for player).
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

        ChunkStorageAccessor s = storage(level);
        if (s == null) return;
        int r = s.chunklite$getViewRange();
        int keepRadius = Math.max(2, r / 2);
        int playerCX = ((int) player.getX()) >> 4;
        int playerCZ = ((int) player.getZ()) >> 4;

        ClientChunkCache cache = level.getChunkSource();
        int unloaded = 0;
        int limit = ChuckLiteConfig.aggressiveUnloadCount();

        for (int dx = -r; dx <= r && unloaded < limit; dx++) {
            for (int dz = -r; dz <= r && unloaded < limit; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist <= keepRadius) continue;

                int cx = playerCX + dx;
                int cz = playerCZ + dz;
                if (s.chunklite$getChunk(cx, cz) != null) {
                    cache.drop(new ChunkPos(cx, cz));
                    unloaded++;
                }
            }
        }

        if (unloaded > 0) {
            LOGGER.warn("Memory pressure ({}%): unloaded {} chunks",
                    Math.round(usedPct), unloaded);
            // Only call System.gc() if the cooldown has expired to avoid
            // spamming full GCs every 20 ticks while memory is high.
            if (gcCooldown <= 0) {
                System.gc();
                gcCooldown = 600; // 30 seconds at 20 tps
            }
        }
    }

    // ── Stats / Commands ───────────────────────────────────────

    public String getStats() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return "No world loaded.";

        ChunkStorageAccessor s = storage(mc.level);
        if (s == null) return "Chunk storage not available.";

        int loaded = countLoaded(s);
        int capacity = s.chunklite$getCapacity();

        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();

        return String.format(
                "ChuckLite Stats%n" +
                "  View radius : %d%n" +
                "  Loaded chunks: %d / %d%n" +
                "  Throttle pending: %d%n" +
                "  Heap : %.0f / %.0f MB (%.0f%%)%n" +
                "  Joining: %s",
                s.chunklite$getViewRange(),
                loaded, capacity,
                throttle.pendingCount(),
                used / 1_048_576.0, max / 1_048_576.0,
                100.0 * used / max,
                joining ? "yes" : "no"
        );
    }

    public int forceUnload(int keepRadius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return 0;

        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        ChunkStorageAccessor s = storage(level);
        if (s == null) return 0;

        int r = s.chunklite$getViewRange();
        int playerCX = ((int) player.getX()) >> 4;
        int playerCZ = ((int) player.getZ()) >> 4;

        ClientChunkCache cache = level.getChunkSource();
        int unloaded = 0;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist <= keepRadius) continue;

                int cx = playerCX + dx;
                int cz = playerCZ + dz;
                if (s.chunklite$getChunk(cx, cz) != null) {
                    cache.drop(new ChunkPos(cx, cz));
                    unloaded++;
                }
            }
        }
        return unloaded;
    }
}
