package com.chunklite.mixin;

import com.chunklite.ChuckLiteConfig;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

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
            LOGGER.debug("Clamped render distance {} to {}", rawDistance, clamped);
        }
        return clamped;
    }
}
