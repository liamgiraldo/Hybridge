package com.litebow.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.xml.crypto.dsig.Transform;

public class RotationCommand extends AbstractPlayerCommand {
    OptionalArg<Vector3f> rotationArg;

    public RotationCommand() {
        super("rotation", "gets the player's rotation");

        this.rotationArg = this.withOptionalArg("rotation", "sets rotation", ArgTypes.ROTATION);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = (Player) commandContext.sender();

        // Show current head rotation (safe to read from player's reference store)
        HeadRotation headRotation = player.getReference().getStore().getComponent(player.getReference(), HeadRotation.getComponentType());
        if (headRotation != null) {
            player.sendMessage(Message.raw("Your head rotation is: " + headRotation.getRotation().toString()));
            player.sendMessage(Message.raw("Your axis direction is: " + headRotation.getAxisDirection().toString()));
            player.sendMessage(Message.raw("Your axis is: " + headRotation.getAxis().toString()));
            player.sendMessage(Message.raw("Your Horizontal axis direction is: " + headRotation.getHorizontalAxisDirection()));
        }

        if (this.rotationArg.get(commandContext) != null) {
            Vector3f newRotation = this.rotationArg.get(commandContext);

            // Must modify components on the world's thread — use world.execute
            world.execute(() -> {
                var storeRef = ref; // the Ref<EntityStore> passed into execute
                var storeLocal = storeRef.getStore();

                // Update the entity's TransformComponent (body rotation)
                TransformComponent transform = storeLocal.getComponent(storeRef, TransformComponent.getComponentType());
                if (transform != null) {
                    transform.setRotation(newRotation);
                }

                // Update head rotation component as well
                HeadRotation hr = storeLocal.getComponent(storeRef, HeadRotation.getComponentType());
                if (hr != null) {
                    hr.setRotation(newRotation);
                }

                // Add a Teleport component to force an immediate client update (position + rotation)
                Vector3d pos = (transform != null) ? transform.getPosition() : new Vector3d(0, 0, 0);
                Teleport tp = Teleport.createForPlayer(world, pos, newRotation);
                storeLocal.addComponent(storeRef, Teleport.getComponentType(), tp);
            });

            player.sendMessage(Message.raw("Your head rotation has been set to: " + newRotation.toString()));
        }

        //4.7 for blue on y
        //7.85 for red on y
    }
}
