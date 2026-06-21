package com.chunklite.fabric;

import com.chunklite.ChuckLiteConfig;
import com.chunklite.optimizer.ClientChunkOptimizer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric entrypoint for ChuckLite.
 *
 * <p>Registers client-tick and disconnect handlers, plus the
 * {@code /chunk-lite} command. The core optimization engine is
 * shared with the Forge variant via the {@code com.chunklite}
 * package.</p>
 */
public class ChuckLiteFabric implements ClientModInitializer {

    public static final String MOD_ID = "chunk-lite";
    public static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

    public static ClientChunkOptimizer optimizer;

    @Override
    public void onInitializeClient() {
        ChuckLiteConfig.reload();
        optimizer = new ClientChunkOptimizer();
        LOGGER.info("ChuckLite v1.01 (Fabric) optimizer ready.");

        // Tick handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (optimizer != null) {
                optimizer.tick();
            }
        });

        // Disconnect handler
        ClientPlayConnectionEvents.DISCONNECT.register((handler, c) -> {
            if (optimizer != null) {
                optimizer.onDisconnect();
                LOGGER.debug("ChuckLite state reset (disconnect).");
            }
        });

        // Commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("chunk-lite")
                            .executes(ctx -> status(ctx.getSource()))
                            .then(ClientCommandManager.literal("stats")
                                    .executes(ctx -> stats(ctx.getSource())))
                            .then(ClientCommandManager.literal("unload")
                                    .executes(ctx -> unload(ctx.getSource(), 4)))
                            .then(ClientCommandManager.literal("reload")
                                    .executes(ctx -> reload(ctx.getSource())))
            );
        });
    }

    // ── Command implementations ────────────────────────────────

    private static int status(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        if (optimizer == null) {
            send(src, "§cChuckLite optimizer not initialized.");
            return 0;
        }
        send(src, "§6ChuckLite §av1.01§r — by §b1efan§r (Fabric)");
        send(src, "  Throttling : " + b(ChuckLiteConfig.throttleEnabled()));
        send(src, "  Directional: " + b(ChuckLiteConfig.directionalUnload()));
        send(src, "  Memory-aware: " + b(ChuckLiteConfig.memoryAware()));
        send(src, "  RD override: " + b(ChuckLiteConfig.overrideRenderDistance()));
        send(src, "§7Use §f/chunk-lite stats§7 for live numbers.§r");
        return 1;
    }

    private static int stats(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        if (optimizer == null) {
            send(src, "§cOptimizer not ready.");
            return 0;
        }
        String s = optimizer.getStats();
        for (String line : s.split("\n")) send(src, line);
        return 1;
    }

    private static int unload(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, int r) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            send(src, "§cNot connected to a world.");
            return 0;
        }
        if (optimizer == null) { send(src, "§cOptimizer not ready."); return 0; }
        int d = optimizer.forceUnload(r);
        send(src, "§aUnloaded §b" + d + "§a chunk(s) beyond radius §b" + r + "§a.");
        System.gc();
        return d;
    }

    private static int reload(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        ChuckLiteConfig.reload();
        send(src, "§aChuckLite config reloaded from disk.§r");
        return 1;
    }

    private static void send(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, String msg) {
        src.sendFeedback(Component.literal(msg));
    }

    private static String b(boolean v) { return v ? "§aON§r" : "§cOFF§r"; }
}
