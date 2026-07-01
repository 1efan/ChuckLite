package com.chunklite.fabric;

import com.chunklite.ChuckLiteConfig;
import com.chunklite.optimizer.ClientChunkOptimizer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric 26.1 entrypoint for ChuckLite.
 */
public class ChuckLiteFabric implements ClientModInitializer {

    public static final String MOD_ID = "chunklite";
    public static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

    public static ClientChunkOptimizer optimizer;

    @Override
    public void onInitializeClient() {
        ChuckLiteConfig.reload();
        optimizer = new ClientChunkOptimizer();
        LOGGER.info("ChuckLite v1.03 (Fabric 26.1) optimizer ready (Sodium present: {}).",
                com.chunklite.compat.ModCompat.sodiumPresent());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (optimizer != null) optimizer.tick();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, c) -> {
            com.chunklite.optimizer.AdaptiveThrottle.reset();
            if (optimizer != null) {
                optimizer.onDisconnect();
                LOGGER.debug("ChuckLite state reset (disconnect).");
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommands.literal("chunk-lite")
                            .executes(ctx -> status(ctx.getSource()))
                            .then(ClientCommands.literal("stats")
                                    .executes(ctx -> stats(ctx.getSource())))
                            .then(ClientCommands.literal("unload")
                                    .executes(ctx -> unload(ctx.getSource(), 4)))
                            .then(ClientCommands.literal("reload")
                                    .executes(ctx -> reload(ctx.getSource())))
            );
        });
    }

    private static int status(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        if (optimizer == null) { send(src, "Optimizer not initialized."); return 0; }
        send(src, "ChuckLite v1.03 by 1efan (Fabric 26.1)");
        send(src, "  Throttling : " + b(ChuckLiteConfig.throttleEnabled())
                + (ChuckLiteConfig.adaptiveThrottle() ? " (adaptive)" : ""));
        send(src, "  Directional: " + b(ChuckLiteConfig.directionalUnload()));
        send(src, "  Memory-aware: " + b(ChuckLiteConfig.memoryAware()));
        send(src, "  RD override: " + b(ChuckLiteConfig.overrideRenderDistance()));
        return 1;
    }

    private static int stats(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        if (optimizer == null) { send(src, "Optimizer not ready."); return 0; }
        for (String line : optimizer.getStats().split("\n")) send(src, line);
        return 1;
    }

    private static int unload(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, int r) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) { send(src, "Not connected."); return 0; }
        if (optimizer == null) { send(src, "Optimizer not ready."); return 0; }
        int d = optimizer.forceUnload(r);
        send(src, "Unloaded " + d + " chunk(s) beyond radius " + r + ".");
        System.gc();
        return d;
    }

    private static int reload(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        ChuckLiteConfig.reload();
        send(src, "Config reloaded from disk.");
        return 1;
    }

    private static void send(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, String msg) {
        src.sendFeedback(Component.literal(msg));
    }

    private static String b(boolean v) { return v ? "ON" : "OFF"; }
}
