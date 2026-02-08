package com.litebow.plugin.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.BridgeGame;
import com.litebow.plugin.BridgeService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerDropItemEvent extends EntityEventSystem<EntityStore, DropItemEvent.Drop> {
    BridgeService bridgeService;

    public PlayerDropItemEvent(BridgeService bridgeService) {
        super(DropItemEvent.Drop.class);
        this.bridgeService = bridgeService;
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull DropItemEvent.Drop dropItemEvent) {
        PlayerRef playerRef = store.getComponent(archetypeChunk.getReferenceTo(i), PlayerRef.getComponentType());
        String itemId = dropItemEvent.getItemStack().getItemId();
        if (playerRef != null) {
            BridgeGame game = bridgeService.getPlayerGame(playerRef);
            if (game != null && !game.canDropItem(playerRef, itemId)) {
                dropItemEvent.setCancelled(true);
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
