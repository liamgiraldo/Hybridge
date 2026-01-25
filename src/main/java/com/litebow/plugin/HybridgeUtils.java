package com.litebow.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HybridgeUtils {
    private HybridgeUtils() {
        // Private constructor to prevent instantiation
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
        if (world == null) return;

        world.execute(() -> {
            var transform = store.getComponent(ref, TransformComponent.getComponentType());
            Vector3f rot = (transform != null) ? transform.getRotation() : new Vector3f(0, 0, 0);

            Teleport tp = Teleport.createForPlayer(world, pos, rot);

            //WHAT IS THIS TELEPORT COMPONENT AHHHH I DONT GET ITTTT
            store.addComponent(ref, Teleport.getComponentType(), tp);
        });
    }
}
