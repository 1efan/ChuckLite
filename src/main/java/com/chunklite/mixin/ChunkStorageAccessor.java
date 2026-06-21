package com.chunklite.mixin;

import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Mixin into {@code ClientChunkCache.Storage} that exposes the
 * internal chunk array and view parameters for the optimizer.
 *
 * <p>In vanilla 1.20.1, the chunk store is an {@code AtomicReferenceArray},
 * indexed by a computed offset — not a hash map. This mixin provides
 * public accessor methods that hide that detail from the optimizer.</p>
 */
@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public abstract class ChunkStorageAccessor {

    @Shadow
    private AtomicReferenceArray<LevelChunk> chunks;

    /** The radius in chunks — this is the "chunk load distance," not viewRange. */
    @Shadow
    int chunkRadius;

    @Shadow
    volatile int viewCenterX;

    @Shadow
    volatile int viewCenterZ;

    /** Compute the array index for chunk coords (matches vanilla logic). */
    @Shadow
    protected abstract int getIndex(int x, int z);

    // ── Public API ────────────────────────────────────────────

    /** Retrieve a chunk at world chunk coords, or null if not loaded. */
    public LevelChunk chunklite$getChunk(int x, int z) {
        int idx = getIndex(x, z);
        if (idx < 0 || idx >= chunks.length()) return null;
        return chunks.get(idx);
    }

    /** The current chunk radius (what vanilla calls chunkRadius / load distance). */
    public int chunklite$getViewRange() {
        return chunkRadius;
    }

    /** The center X chunk coord. */
    public int chunklite$getViewCenterX() {
        return viewCenterX;
    }

    /** The center Z chunk coord. */
    public int chunklite$getViewCenterZ() {
        return viewCenterZ;
    }

    /** Total slots in the chunk array (used for stats). */
    public int chunklite$getCapacity() {
        return chunks.length();
    }
}
