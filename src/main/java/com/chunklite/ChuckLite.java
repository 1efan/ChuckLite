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

@Mod(ChuckLite.MOD_ID)
public class ChuckLite {

    public static final String MOD_ID = "chunk-lite";
    public static final Logger LOGGER = LoggerFactory.getLogger("ChuckLite");

    public static ClientChunkOptimizer optimizer;

    public ChuckLite() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            LOGGER.warn("ChuckLite is a client-only mod, skipping server-side init.");
            return;
        }

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
        ChuckLiteConfig.reload();
        optimizer = new ClientChunkOptimizer();
        LOGGER.info("ChuckLite v{} optimizer ready.", "1.02");
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

    @SubscribeEvent
    public void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (optimizer != null) {
            optimizer.onDisconnect();
            LOGGER.debug("ChuckLite state reset (logout).");
        }
    }
}
