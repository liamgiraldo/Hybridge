package com.litebow.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Objects;

public class HybridgeUtils {
    private HybridgeUtils() {

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

    public static void providePlayerWithBridgeItems(Player player, GameModel.Team team) {
        //give player items based on their team
        ItemContainer hotbar = player.getInventory().getHotbar();
        if(team == GameModel.Team.RED){
            //give red team items
            hotbar.setItemStackForSlot((short) 1, HybridgeConstants.RED_BLOCK);
            hotbar.setItemStackForSlot((short) 2, HybridgeConstants.RED_BLOCK);
        }
        else if(team == GameModel.Team.BLUE){
            //give blue team items
            hotbar.setItemStackForSlot((short) 1, HybridgeConstants.BLUE_BLOCK);
            hotbar.setItemStackForSlot((short) 2, HybridgeConstants.BLUE_BLOCK);
        }
        hotbar.setItemStackForSlot((short) 0, HybridgeConstants.SWORD);
        hotbar.setItemStackForSlot((short) 3, HybridgeConstants.PICK);
    }

    public static void clearPlayerInventory(Player player){
        Inventory inventory = player.getInventory();
        inventory.clear();
    }

    public static boolean isPositionWithinArea(Vector3d pos, Vector3d min, Vector3d max){
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
        Hybridge.LOGGER.atInfo().log("Checking position " + pos + " within area min " + realMin + " and max " + realMax);
        return (pos.x >= realMin.x && pos.x <= realMax.x) &&
               (pos.y >= realMin.y && pos.y <= realMax.y) &&
               (pos.z >= realMin.z && pos.z <= realMax.z);
    }

    public static boolean isBlockValidBridgeBlock(BlockType blockType){
        String blockName = blockType.getId();
        Hybridge.LOGGER.atInfo().log("Checking if block " + blockName + " is a valid bridge block.");
        Hybridge.LOGGER.atInfo().log("Red Block ID: " + HybridgeConstants.RED_BLOCK.getItemId());
        Hybridge.LOGGER.atInfo().log("Blue Block ID: " + HybridgeConstants.BLUE_BLOCK.getItemId());
        Hybridge.LOGGER.atInfo().log("White Block ID: " + HybridgeConstants.WHITE_BLOCK.getItemId());
        if(Objects.equals(blockName, HybridgeConstants.RED_BLOCK.getItemId()) ||
                Objects.equals(blockName, HybridgeConstants.BLUE_BLOCK.getItemId()) || Objects.equals(blockName, HybridgeConstants.WHITE_BLOCK.getItemId())){
            return true;
        }
        return false;
    }
}
