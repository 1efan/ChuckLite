package com.chunklite.mixin;

import com.chunklite.ChuckLiteConfig;
import com.chunklite.optimizer.AdaptiveThrottle;
import net.minecraft.client.multiplayer.ChunkBatchSizeCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBatchSizeCalculator.class)
public class ChunkBatchThrottleMixin {

    @Inject(method = "getDesiredChunksPerTick", at = @At("RETURN"), cancellable = true)
    private void chunklite$capChunkRate(CallbackInfoReturnable<Float> cir) {
        if (!ChuckLiteConfig.throttleEnabled()) {
            return;
        }
        // When adaptive throttling is on, the cap tracks frame time (fed by SectionUploadFrameMixin);
        // otherwise fall back to the fixed per-tick cap.
        float cap = ChuckLiteConfig.adaptiveThrottle()
                ? AdaptiveThrottle.chunkPerTickCap()
                : ChuckLiteConfig.maxChunkLoadsPerTick();
        if (cir.getReturnValueF() > cap) {
            cir.setReturnValue(cap);
        }
    }
}
