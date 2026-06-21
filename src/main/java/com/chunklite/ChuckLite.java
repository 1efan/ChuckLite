package com.chunklite;

import com.chunklite.command.ChuckLiteCommands;
import com.chunklite.optimizer.ClientChunkOptimizer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChuckLite — lightweight client-side chunk optimization mod.
 *
 * <p>Reduces memory overhead and improves frame times by intelligently
 * managing chunk load cycles. All optimizations are strictly client-side;
 * nothing needs to be installed on a server.</p>
 *
 * <h3>Key features</h3>
 * <ul>
 *   <li>Smart chunk preloading based on player movement patterns.</li>
 *   <li>Reduced memory footprint through aggressive cache cleanup.</li>
 *   <li>Compatible with Sodium, Lithium, and other optimization mods.</li>
 *   <li>{@code /chunk-lite} command for stats and manual control.</li>
 * </ul>
 *
 * <h3>Configuration</h3>
 * <p>Edit {@code .minecraft/config/chuck-lite.properties}. Changes take
 * effect immediately — no restart needed.</p>
 *
 * @author 1efan
 * @version 1.01
 */
@Mod(ChuckLite.MOD_ID)
public class ChuckLite {

    public static final String MOD_ID = "chunk-lite";
    public static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

    /** Shared optimizer instance — created during client setup. */
    public static ClientChunkOptimizer optimizer;

    public ChuckLite() {
        // This mod is client-only. If someone loads it on a server, bail early.
        if (FMLEnvironment.dist != Dist.CLIENT) {
            LOGGER.warn("ChuckLite is a client-only mod — skipping server-side init.");
            return;
        }

        // Tell Forge this mod doesn't need to be present on the server.
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remote, isServer) -> true
                )
        );

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        // Touch config to ensure defaults are written.
        ChuckLiteConfig.reload();
        optimizer = new ClientChunkOptimizer();
        LOGGER.info("ChuckLite v{} optimizer ready.", "1.01");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterClientCommandsEvent event) {
        ChuckLiteCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (optimizer != null) {
            optimizer.tick();
        }
    }

    /**
     * Reset optimizer state when the player disconnects from a server
     * or leaves a single-player world.
     */
    @SubscribeEvent
    public void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (optimizer != null) {
            optimizer.onDisconnect();
            LOGGER.debug("ChuckLite state reset (logout).");
        }
    }
}
