package com.litebow.plugin.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.Hybridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPlaceEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public BlockPlaceEvent() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent placeBlockEvent) {
        var ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
//        placeBlockEvent.setCancelled(true);
        Hybridge.LOGGER.atInfo().log("Cancelled block place event for player " + playerRef.getUsername());
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
