package com.litebow.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class HybridgeUtils {
    private HybridgeUtils() {

    }

    /** Relative positions for a hollow 5x5x5 cube (shell only). Each component in [-2, 2]; on shell if any component is ±2. */
    private static final List<Vector3i> CAGE_SHELL_RELATIVE = new ArrayList<>();
    static {
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2) {
                        CAGE_SHELL_RELATIVE.add(new Vector3i(x, y, z));
                    }
                }
            }
        }
    }

    // Potentially could use built in hytale method here... Axiom?
    /** Place a hollow 5x5x5 cage at the given block position (center of the cube). Call from world thread only. */
    public static void placeCageSync(World world, Vector3i center) {
        for (Vector3i rel : CAGE_SHELL_RELATIVE) {
            int x = center.x + rel.x;
            int y = center.y + rel.y;
            int z = center.z + rel.z;
            world.setBlock(x, y, z, HybridgeConstants.CAGE_BLOCK_ID);
        }
    }

    /** Remove a hollow 5x5x5 cage at the given block position (same center as placeCage). Call from world thread only. */
    public static void removeCageSync(World world, Vector3i center) {
        for (Vector3i rel : CAGE_SHELL_RELATIVE) {
            int x = center.x + rel.x;
            int y = center.y + rel.y;
            int z = center.z + rel.z;
            world.setBlock(x, y, z, "Empty");
        }
    }


    public static void withEntityPosition(
            Ref<EntityStore> entityRef,
            java.util.function.Consumer<Vector3d> callback
    ) {
        if (entityRef == null || !entityRef.isValid()) return;

        var store = entityRef.getStore();
        var world = store.getExternalData().getWorld();

        world.execute(() -> {
            var tc = store.getComponent(entityRef, TransformComponent.getComponentType());
            if (tc == null) return;

            callback.accept(tc.getPosition());
        });
    }

    public static void teleportPlayer(Ref<EntityStore> ref, Vector3d pos) {
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            var transform = store.getComponent(ref, TransformComponent.getComponentType());
            Vector3f rot = (transform != null) ? transform.getRotation() : new Vector3f(0, 0, 0);

            Teleport tp = Teleport.createForPlayer(world, pos, rot);

            //WHAT IS THIS TELEPORT COMPONENT AHHHH I DONT GET ITTTT
            store.addComponent(ref, Teleport.getComponentType(), tp);
        });
    }

    public static void teleportPlayer(Ref<EntityStore> ref, Vector3d pos, Vector3f rot) {
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            var transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform != null) {
                transform.setRotation(rot);
            }

            HeadRotation hr = store.getComponent(ref, HeadRotation.getComponentType());
            if (hr != null) {
                hr.setRotation(rot);
            }

            Teleport tp = Teleport.createForPlayer(world, pos, rot);

            //WHAT IS THIS TELEPORT COMPONENT AHHHH I DONT GET ITTTT
            store.addComponent(ref, Teleport.getComponentType(), tp);
        });
    }

    public static void setPlayerRotation(PlayerRef player, Vector3f rotation) {
        var ref = player.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        var world = store.getExternalData().getWorld();

        world.execute(() -> {
            var headRotation = store.getComponent(ref, HeadRotation.getComponentType());
            if (headRotation != null) {
                headRotation.setRotation(rotation);
            }

            var transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform != null) {
                transform.setRotation(rotation);
            }
        });
    }

    public static void providePlayerWithBridgeItems(Player player, GameModel.Team team) {
        //give player items based on their team
        ItemContainer hotbar = player.getInventory().getHotbar();
        if (team == GameModel.Team.RED) {
            //give red team items
            hotbar.setItemStackForSlot((short) 1, HybridgeConstants.RED_BLOCK);
            hotbar.setItemStackForSlot((short) 2, HybridgeConstants.RED_BLOCK);
        } else if (team == GameModel.Team.BLUE) {
            //give blue team items
            hotbar.setItemStackForSlot((short) 1, HybridgeConstants.BLUE_BLOCK);
            hotbar.setItemStackForSlot((short) 2, HybridgeConstants.BLUE_BLOCK);
        }
        hotbar.setItemStackForSlot((short) 0, HybridgeConstants.SWORD);
        hotbar.setItemStackForSlot((short) 3, HybridgeConstants.PICK);
        hotbar.setItemStackForSlot((short) 4, HybridgeConstants.HEAL);
        hotbar.setItemStackForSlot((short) 5, HybridgeConstants.BOW);
    }

    public static void clearPlayerInventory(Player player) {
        Inventory inventory = player.getInventory();
        inventory.clear();
    }

    public static boolean isPositionWithinArea(Vector3d pos, Vector3d min, Vector3d max) {
        //sort min and max just in case
        Vector3d realMin = new Vector3d(
                Math.min(min.x, max.x),
                Math.min(min.y, max.y),
                Math.min(min.z, max.z)
        );
        Vector3d realMax = new Vector3d(
                Math.max(min.x, max.x),
                Math.max(min.y, max.y),
                Math.max(min.z, max.z)
        );

        return (pos.x >= realMin.x && pos.x <= realMax.x) &&
                (pos.y >= realMin.y && pos.y <= realMax.y) &&
                (pos.z >= realMin.z && pos.z <= realMax.z);
    }

    public static boolean isBlockValidBridgeBlock(BlockType blockType) {
        String blockName = blockType.getId();

        if (Objects.equals(blockName, HybridgeConstants.RED_BLOCK.getItemId()) ||
                Objects.equals(blockName, HybridgeConstants.BLUE_BLOCK.getItemId()) || Objects.equals(blockName, HybridgeConstants.WHITE_BLOCK.getItemId())) {
            return true;
        }
        return false;
    }

    public static void sendMessageToCollectionOfPlayers(java.util.Collection<Player> players, Message message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public static void sendMessageToCollectionOfPlayerRefs(java.util.Collection<PlayerRef> players, Message message) {
        for (PlayerRef player : players) {
            player.sendMessage(message);
        }
    }

    public static void setPlayerHealthFull(Player player) {
        var ref = player.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            var statMap = (EntityStatMap) store.getComponent(ref, EntityStatMap.getComponentType());
            if (statMap != null) {
                statMap.maximizeStatValue(DefaultEntityStatTypes.getHealth());
            }
        });
    }

    public static void showTitleToCollectionOfPlayers(java.util.Collection<PlayerRef> players, Message title, Message subtitle, int fadeInMs, int stayMs, int fadeOutMs) {
        for (PlayerRef player : players) {
            EventTitleUtil.showEventTitleToPlayer(player, title, subtitle, true, null, stayMs, fadeInMs, fadeOutMs);
        }
    }


    private static class CopiedBlock {
        public Vector3i relativePos;
        public Vector3i globalPos;
        public BlockType blockType;

        public CopiedBlock(Vector3i relativePos, Vector3i globalPos, BlockType blockType) {
            this.relativePos = relativePos;
            this.globalPos = globalPos;
            this.blockType = blockType;
        }
    }

    public static ArrayList<CopiedBlock> getBlocksInArea(World world, Vector3i pos1, Vector3i pos2, Vector3i relativePos) {
        ArrayList<CopiedBlock> blocks = new ArrayList<>();

        Vector3i min = new Vector3i(
                Math.min(pos1.x, pos2.x),
                Math.min(pos1.y, pos2.y),
                Math.min(pos1.z, pos2.z)
        );
        Vector3i max = new Vector3i(
                Math.max(pos1.x, pos2.x),
                Math.max(pos1.y, pos2.y),
                Math.max(pos1.z, pos2.z)
        );
        world.execute(() -> {
            for (int x = min.x + 1; x < max.x; x++) {
                for (int y = min.y; y < max.y; y++) {
                    for (int z = min.z + 1; z < max.z; z++) {
                        Vector3i currentPos = new Vector3i(x, y, z);
                        BlockType blockType = world.getBlockType(currentPos);

                        Vector3i relativeToCopy = new Vector3i(
                                currentPos.x - relativePos.x,
                                currentPos.y - relativePos.y,
                                currentPos.z - relativePos.z
                        );
                        blocks.add(new CopiedBlock(relativeToCopy, currentPos, blockType));
                    }
                }
            }
        });
        return blocks;
    }

    public static void setBlocksInArea(World world, Vector3i basePos, ArrayList<CopiedBlock> blocks) {
        world.execute(() -> {
            for (CopiedBlock copiedBlock : blocks) {
                Vector3i targetPos = new Vector3i(
                        basePos.x + copiedBlock.relativePos.x,
                        basePos.y + copiedBlock.relativePos.y,
                        basePos.z + copiedBlock.relativePos.z
                );
                if (copiedBlock.blockType == null) continue;
                var key = copiedBlock.blockType.getId();
                world.setBlock(targetPos.x, targetPos.y, targetPos.z, key);
            }
        });
    }

    public static void setBlocksInAreaAir(World world, Vector3i pos1, Vector3i pos2) {
        Vector3i min = new Vector3i(
                Math.min(pos1.x, pos2.x),
                Math.min(pos1.y, pos2.y),
                Math.min(pos1.z, pos2.z)
        );
        Vector3i max = new Vector3i(
                Math.max(pos1.x, pos2.x),
                Math.max(pos1.y, pos2.y),
                Math.max(pos1.z, pos2.z)
        );
        world.execute(() -> {
            for (int x = min.x + 1; x < max.x; x++) {
                for (int y = min.y + 1; y < max.y; y++) {
                    for (int z = min.z + 1; z < max.z; z++) {
                        Vector3i currentPos = new Vector3i(x, y, z);
                        world.setBlock(currentPos.x, currentPos.y, currentPos.z, "Empty");
                    }
                }
            }
        });

    }

    public static void playSoundEffectToPlayer(PlayerRef player, String key) {
        var ref = player.getReference();
        if (ref == null || !ref.isValid()) return;

        int index = SoundEvent.getAssetMap().getIndex(key);

        World world = ref.getStore().getExternalData().getWorld();
        world.execute(() -> {
            TransformComponent transformComponent = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
            SoundUtil.playSoundEvent3dToPlayer(ref, index, SoundCategory.SFX, transformComponent.getPosition(), ref.getStore());
        });
    }

    public static void playSoundEffectToPlayers(Collection<PlayerRef> players, String key) {
        for (PlayerRef player : players) {
            playSoundEffectToPlayer(player, key);
        }
    }

    /**
     * Play a sound to multiple players. Must be called from the world thread (e.g. inside world.execute)
     * so the sound plays immediately without nesting world.execute.
     */
    public static void playSoundEffectToPlayersOnWorldThread(Collection<PlayerRef> players, String key) {
        int index;
        try {
            index = SoundEvent.getAssetMap().getIndex(key);
        } catch (Exception e) {
            return;
        }
        for (PlayerRef player : players) {
            var ref = player.getReference();
            if (ref == null || !ref.isValid()) continue;
            TransformComponent transformComponent = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent != null) {
                SoundUtil.playSoundEvent3dToPlayer(ref, index, SoundCategory.SFX, transformComponent.getPosition(), ref.getStore());
            }
        }
    }
}
