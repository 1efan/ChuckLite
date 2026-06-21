package com.chunklite.command;

import com.chunklite.ChuckLite;
import com.chunklite.ChuckLiteConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side commands for ChuckLite.
 *
 * <ul>
 *   <li>{@code /chunk-lite} — show current status.</li>
 *   <li>{@code /chunk-lite stats} — dump detailed statistics.</li>
 *   <li>{@code /chunk-lite unload [radius]} — force-unload chunks
 *       outside the given radius (default 4).</li>
 *   <li>{@code /chunk-lite reload} — re-read config from disk.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public final class ChuckLiteCommands {

    private ChuckLiteCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("chunk-lite")
                        .executes(ctx -> status(ctx.getSource()))
                        .then(Commands.literal("stats")
                                .executes(ctx -> stats(ctx.getSource())))
                        .then(Commands.literal("unload")
                                .executes(ctx -> unload(ctx.getSource(), 4))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> unload(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "radius")))))
                        .then(Commands.literal("reload")
                                .executes(ctx -> reload(ctx.getSource())))
        );
    }

    // ── Subcommand implementations ─────────────────────────────

    private static int status(CommandSourceStack source) {
        if (ChuckLite.optimizer == null) {
            send(source, "§cChuckLite optimizer not initialized.");
            return 0;
        }

        send(source, "§6ChuckLite §av1.01§r — by §b1efan§r");
        send(source, "  Throttling : " + boolStr(ChuckLiteConfig.throttleEnabled())
                + " §7(" + ChuckLiteConfig.maxChunkLoadsPerTick() + "/tick)§r");
        send(source, "  Directional: " + boolStr(ChuckLiteConfig.directionalUnload())
                + " §7(arc: " + ChuckLiteConfig.forwardRetentionAngle() + "°)§r");
        send(source, "  Memory-aware: " + boolStr(ChuckLiteConfig.memoryAware())
                + " §7(threshold: " + ChuckLiteConfig.memoryThresholdPct() + "%)§r");
        send(source, "  RD override: " + boolStr(ChuckLiteConfig.overrideRenderDistance())
                + " §7(" + ChuckLiteConfig.minRenderDistance() + "–" + ChuckLiteConfig.maxRenderDistance() + ")§r");
        send(source, "§7Use §f/chunk-lite stats§7 for live numbers.§r");
        return 1;
    }

    private static int stats(CommandSourceStack source) {
        if (ChuckLite.optimizer == null) {
            send(source, "§cOptimizer not ready.");
            return 0;
        }
        String stats = ChuckLite.optimizer.getStats();
        for (String line : stats.split("\n")) {
            send(source, line);
        }
        return 1;
    }

    private static int unload(CommandSourceStack source, int keepRadius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            send(source, "§cNot connected to a world.");
            return 0;
        }
        if (ChuckLite.optimizer == null) {
            send(source, "§cOptimizer not ready.");
            return 0;
        }

        int dropped = ChuckLite.optimizer.forceUnload(keepRadius);
        send(source, "§aUnloaded §b" + dropped + "§a chunk(s) beyond radius §b" + keepRadius + "§a.");
        System.gc();
        return dropped;
    }

    private static int reload(CommandSourceStack source) {
        ChuckLiteConfig.reload();
        send(source, "§aChuckLite config reloaded from disk.§r");
        return 1;
    }

    // ── Helpers ────────────────────────────────────────────────

    private static void send(CommandSourceStack source, String msg) {
        source.sendSystemMessage(Component.literal(msg));
    }

    private static String boolStr(boolean v) {
        return v ? "§aON§r" : "§cOFF§r";
    }
}
