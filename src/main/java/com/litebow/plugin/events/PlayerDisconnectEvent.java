package com.litebow.plugin.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerRefEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.BridgeService;
import com.litebow.plugin.HybridgeConstants;
import com.litebow.plugin.HybridgeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDisconnectEvent{
    public BridgeService bridgeService;
    public PlayerDisconnectEvent(BridgeService bridgeService){
        this.bridgeService = bridgeService;
    }
    public void onPlayerDisconnect(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent event){
        //handle player disconnect logic here
        var playerRef = event.getPlayerRef();
        HybridgeUtils.sendMessageToCollectionOfPlayerRefs(Universe.get().getPlayers(), Message.raw(playerRef.getUsername() + " has disconnected from the server."));

        var game = bridgeService.getPlayerGame(playerRef);
        if(game == null){
            //teleport them to the lobby
            HybridgeUtils.teleportPlayer(playerRef.getReference(), HybridgeConstants.SPAWN_LOCATION);
        }
    }
}
