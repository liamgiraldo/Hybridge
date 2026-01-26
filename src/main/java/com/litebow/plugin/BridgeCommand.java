package com.litebow.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class BridgeCommand extends AbstractPlayerCommand {
    private OptionalArg<String> startOrStopArg;

    private BridgeService bridgeService;

    Message joinMessage = Message.raw("You have joined the Bridge queue!").bold(true).color(Color.BLUE);
    Message unauthorizedMessage = Message.raw("You do not have permission to use this command.").bold(true).color(Color.RED);

    public BridgeCommand(BridgeService bridgeService) {
        super("bridge", "Queues the player for bridge");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP

        this.bridgeService = bridgeService;

        this.startOrStopArg = this.withOptionalArg("startOrStop", "starts or stops the bridge game", ArgTypes.STRING);

    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = (Player) commandContext.sender();
        if(this.startOrStopArg.get(commandContext) != null){
            if(!player.hasPermission("bridge.admin")){
                player.sendMessage(unauthorizedMessage);
                return;
            }
            if(this.startOrStopArg.get(commandContext).equalsIgnoreCase("start")){
                player.sendMessage(Message.raw("Force starting the bridge game...").color(Color.GREEN).bold(true));
                bridgeService.startGame(bridgeService.getGames().getFirst()); //forcibly starts the first game. In the future, we can add more logic to select which game to start
                return;
            }
            else if(this.startOrStopArg.get(commandContext).equalsIgnoreCase("stop")){
                //not implemented yet
                player.sendMessage(Message.raw("Stopping the bridge game...").color(Color.RED).bold(true));
                bridgeService.stopGame(bridgeService.getGames().getFirst()); //forcibly stops the first game. In the future, we can add more logic to select which game to stop
                return;
            }
        }
        else {
            bridgeService.joinQueue(player);
            player.sendMessage(joinMessage);
        }
    }
}