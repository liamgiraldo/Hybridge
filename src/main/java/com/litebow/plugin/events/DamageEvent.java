package com.litebow.plugin.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.BridgeGame;
import com.litebow.plugin.BridgeService;
import com.litebow.plugin.Hybridge;
import com.litebow.plugin.HybridgeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageEvent extends EntityEventSystem<EntityStore, Damage> {
    private BridgeService bridgeService;
    public DamageEvent(BridgeService bridgeService) {
        super(Damage.class);
        this.bridgeService = bridgeService;
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
        PlayerRef playerRef = store.getComponent(archetypeChunk.getReferenceTo(i), PlayerRef.getComponentType());
        if(playerRef != null && bridgeService.getPlayerGame(playerRef) != null){
            BridgeGame game = bridgeService.getPlayerGame(playerRef);
            Hybridge.LOGGER.atInfo().log("Player " + playerRef.getUsername() + " took damage: " + damage.getAmount());
//            damage.setCancelled(true);
            EntityStatMap playerStats = store.getComponent(playerRef.getReference(), EntityStatMap.getComponentType());
            var playerHealth = playerStats.get(DefaultEntityStatTypes.getHealth());
            if(playerHealth.get() - damage.getAmount() <= 0){
                damage.setCancelled(true);
                game.teleportPlayerToTeamSpawn(playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType()));
                HybridgeUtils.setPlayerHealthFull(store.getComponent(playerRef.getReference(), Player.getComponentType()));
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
