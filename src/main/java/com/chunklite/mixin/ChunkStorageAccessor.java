package com.chunklite.mixin;

import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public abstract class ChunkStorageAccessor {

    @Shadow
    private AtomicReferenceArray<LevelChunk> chunks;

    @Shadow
    int chunkRadius;

    @Shadow
    volatile int viewCenterX;

    @Shadow
    volatile int viewCenterZ;

    @Shadow
    protected abstract int getIndex(int x, int z);

    public LevelChunk chunklite$getChunk(int x, int z) {
        int idx = getIndex(x, z);
        if (idx < 0 || idx >= chunks.length()) return null;
        return chunks.get(idx);
    }

    public int chunklite$getViewRange() {
        return chunkRadius;
    }

    public int chunklite$getViewCenterX() {
        return viewCenterX;
    }

    public int chunklite$getViewCenterZ() {
        return viewCenterZ;
    }

    public int chunklite$getCapacity() {
        return chunks.length();
    }
}
