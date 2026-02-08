package com.litebow.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.litebow.plugin.commands.BridgeCommand;
import com.litebow.plugin.commands.CopyPasteCommand;
import com.litebow.plugin.commands.PlayerItemCommand;
import com.litebow.plugin.commands.RotationCommand;
import com.litebow.plugin.events.*;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class Hybridge extends JavaPlugin {

    private BridgeService bridgeService = new BridgeService();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Hybridge(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.getCommandRegistry().registerCommand(new BridgeCommand(bridgeService));
        this.getCommandRegistry().registerCommand(new RotationCommand());
        this.getCommandRegistry().registerCommand(new CopyPasteCommand());
        this.getCommandRegistry().registerCommand(new CopyPasteCommand.PasteCommand());
        this.getCommandRegistry().registerCommand(new PlayerItemCommand());

        this.getEntityStoreRegistry().registerSystem(new BlockPlaceEvent(bridgeService));
        this.getEntityStoreRegistry().registerSystem(new DamageEvent(bridgeService));
        this.getEntityStoreRegistry().registerSystem(new BlockBreakEvent(bridgeService));
        this.getEntityStoreRegistry().registerSystem(new PlayerDropItemEvent(bridgeService));

        this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent.class, new PlayerDisconnectEvent(bridgeService)::onPlayerDisconnect);

//        bridgeService.createGame(PlaceholderMap.map, Universe.get().getWorld("default")); //creates a default game on startup
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down plugin " + this.getName());
        bridgeService.removeAllGames();
        super.shutdown();
    }
}