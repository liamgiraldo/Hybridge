package com.litebow.plugin.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.*;

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
        if (playerRef != null && bridgeService.getPlayerGame(playerRef) != null) {
            BridgeGame game = bridgeService.getPlayerGame(playerRef);
            Hybridge.LOGGER.atInfo().log("Player " + playerRef.getUsername() + " took damage: " + damage.getAmount());
//            damage.setCancelled(true);
            if (damage.getCause().equals(DamageCause.FALL)) {
                Hybridge.LOGGER.atInfo().log("Cancelled fall damage for player " + playerRef.getUsername() + " in game.");
                damage.setCancelled(true);
                return;
            }
            //if the damager and the damagee are on the same team, cancel the damage
            
            EntityStatMap playerStats = store.getComponent(playerRef.getReference(), EntityStatMap.getComponentType());
            var playerHealth = playerStats.get(DefaultEntityStatTypes.getHealth());
            if (playerHealth.get() - damage.getAmount() <= 0) {
                damage.setCancelled(true);
                HybridgeUtils.playSoundEffectToPlayer(playerRef, HybridgeConstants.SFX_DIE);
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
