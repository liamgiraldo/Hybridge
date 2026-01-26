package com.litebow.plugin.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.Hybridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageEvent extends EntityEventSystem<EntityStore, Damage> {
    public DamageEvent() {
        super(Damage.class);
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
        PlayerRef playerRef = store.getComponent(archetypeChunk.getReferenceTo(i), PlayerRef.getComponentType());
        if(playerRef != null){
            Hybridge.LOGGER.atInfo().log("Player " + playerRef.getUsername() + " took damage: " + damage.getAmount());
//            damage.setCancelled(true);
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
