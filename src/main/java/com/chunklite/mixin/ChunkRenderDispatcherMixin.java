package com.chunklite.mixin;

import com.chunklite.ChuckLiteConfig;
import com.chunklite.compat.ModCompat;
import com.chunklite.optimizer.ClientChunkOptimizer;
import java.util.Queue;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Caps how many finished chunk meshes are uploaded to the GPU in a single frame. Vanilla's
 * {@code uploadAllPendingUploads} drains the entire queue every frame, so when a flood of sections
 * finishes building at once (joining a server, flying, teleporting) all of those uploads land on one
 * frame and spike it. We instead upload only up to the adaptive budget and let the rest wait for the
 * next frames, which spreads the cost and keeps the frame rate smooth. Nothing is dropped, just
 * deferred.
 *
 * <p>This does nothing when Sodium (or a port) is present, since Sodium replaces this whole pipeline
 * and manages uploads itself. It is a no-op when adaptive throttling is turned off in the config.</p>
 */
@Mixin(ChunkRenderDispatcher.class)
public abstract class ChunkRenderDispatcherMixin {

    @Shadow
    @Final
    private Queue<Runnable> toUpload;

    @Inject(method = "uploadAllPendingUploads", at = @At("HEAD"), cancellable = true)
    private void chunklite$cappedUpload(CallbackInfo ci) {
        if (ModCompat.sodiumPresent()) {
            return;
        }
        if (!ChuckLiteConfig.throttleEnabled() || !ChuckLiteConfig.adaptiveThrottle()) {
            return;
        }
        ClientChunkOptimizer optimizer = ClientChunkOptimizer.active;
        if (optimizer == null) {
            return;
        }

        int budget = optimizer.buildBudget.perFrame();
        if (budget <= 0) {
            return;
        }

        Runnable task;
        int done = 0;
        while (done < budget && (task = this.toUpload.poll()) != null) {
            task.run();
            done++;
        }
        // We handled the (capped) drain, so skip vanilla's drain-everything pass.
        ci.cancel();
    }
}
