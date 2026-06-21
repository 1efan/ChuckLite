package com.chunklite.mixin;

import com.chunklite.ChuckLite;
import com.chunklite.ChuckLiteConfig;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hooks into {@link ClientChunkCache}.
 */
@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {

    @Shadow
    public Object storage;  // ClientChunkCache.Storage — shadowed as Object to avoid package-private type

    /** Public accessor for the storage field. */
    public static Object getStorage(ClientChunkCache cache) {
        return ((ClientChunkCacheMixin) (Object) cache).storage;
    }

    /**
     * Clamp the view radius to the configured [min, max] range.
     */
    @ModifyVariable(
            method = "updateViewRadius",
            at = @At("HEAD"),
            argsOnly = true,
            require = 1
    )
    private int chunklite$clampViewRadius(int rawDistance) {
        if (!ChuckLiteConfig.overrideRenderDistance()) {
            return rawDistance;
        }
        int min = ChuckLiteConfig.minRenderDistance();
        int max = ChuckLiteConfig.maxRenderDistance();
        int clamped = Mth.clamp(rawDistance, min, max);
        if (clamped != rawDistance) {
            ChuckLite.LOGGER.debug("Clamped render distance {} → {}", rawDistance, clamped);
        }
        return clamped;
    }
}
