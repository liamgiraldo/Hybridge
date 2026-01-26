package com.litebow.plugin.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.Transform;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.collision.BlockData;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.BridgeGame;
import com.litebow.plugin.BridgeService;
import com.litebow.plugin.Hybridge;
import com.litebow.plugin.HybridgeConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private BridgeService bridgeService;

    public BlockBreakEvent(BridgeService bridgeService) {
        super(BreakBlockEvent.class);
        this.bridgeService = bridgeService;
    }

    @Override
    public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent breakBlockEvent) {
        BlockType blockType = breakBlockEvent.getBlockType();

        //I think this is the position of the block being broken?
        var blockPosition = breakBlockEvent.getTargetBlock();
        Hybridge.LOGGER.atInfo().log("Block break at position: " + blockPosition.getX() + ", " + blockPosition.getY() + ", " + blockPosition.getZ());

        var ref = archetypeChunk.getReferenceTo(i);
        var playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        var player = ref.getStore().getComponent(ref, Player.getComponentType());

        Hybridge.LOGGER.atInfo().log("BlockType: " + blockType.getId());

        BridgeGame game = bridgeService.getPlayerGame(playerRef);
        if (game != null && !game.canBreakBlock(blockType, blockPosition)) {
            breakBlockEvent.setCancelled(true);
            Hybridge.LOGGER.atInfo().log("Cancelled block break event for player " + playerRef.getUsername() + " in game.");
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
