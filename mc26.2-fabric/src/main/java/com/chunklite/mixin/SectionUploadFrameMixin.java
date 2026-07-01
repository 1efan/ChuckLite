package com.chunklite.mixin;

import com.chunklite.optimizer.AdaptiveThrottle;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// per-frame pulse for the adaptive throttle. uploadGlobalGeomBuffersToGPU runs once per frame, and
// works in the mixin-only neoforge build where there's no render-tick hook. observe only.
@Mixin(SectionRenderDispatcher.class)
public abstract class SectionUploadFrameMixin {

    @Inject(method = "uploadGlobalGeomBuffersToGPU", at = @At("HEAD"))
    private void chunklite$feedFrame(CallbackInfo ci) {
        AdaptiveThrottle.onFrame(System.nanoTime());
    }
}
