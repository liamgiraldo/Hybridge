package com.litebow.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class was written mostly by AI,
 * It was just for testing inventory related functions
 * <p>
 * We can remove this command later if we want but it might be useful for testing.
 */
public class PlayerItemCommand extends AbstractPlayerCommand {
    private RequiredArg<String> giveOrCheckArg;
    private RequiredArg<String> itemIdArg;
    private OptionalArg<Integer> quantityArg;
    private OptionalArg<String> targetPlayerArg;

    public PlayerItemCommand() {
        super("item", "basic item utils");
        this.targetPlayerArg = this.withOptionalArg("target", "the player to give the item to (defaults to self)", ArgTypes.STRING);
        this.giveOrCheckArg = this.withRequiredArg("action", "whether to give the item or check for it", ArgTypes.STRING);
        this.itemIdArg = this.withRequiredArg("itemId", "the ID of the item to give or check for", ArgTypes.STRING);
        this.quantityArg = this.withOptionalArg("quantity", "the quantity of the item to give (or check for)", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player sender = (Player) commandContext.sender();
        PlayerRef targetRef = resolveTargetRef(commandContext, world, playerRef);
        String action = this.giveOrCheckArg.get(commandContext).trim();

        if ("give".equalsIgnoreCase(action)) {
            handleGive(commandContext, sender, targetRef, world);
        } else if ("check".equalsIgnoreCase(action)) {
            handleCheck(commandContext, sender, targetRef);
        } else {
            sender.sendMessage(Message.raw("Invalid action: " + action + ". Use 'give' or 'check'."));
        }
    }

    private PlayerRef resolveTargetRef(@Nonnull CommandContext ctx, @Nonnull World world, @Nonnull PlayerRef defaultRef) {
        String targetName = this.targetPlayerArg.get(ctx);
        if (targetName == null) {
            return defaultRef;
        }
        for (PlayerRef pr : world.getPlayerRefs()) {
            if (pr.getUsername().equalsIgnoreCase(targetName)) {
                return pr;
            }
        }
        return defaultRef;
    }

    private Integer getOptionalQuantity(@Nonnull CommandContext ctx) {
        return this.quantityArg.get(ctx); // may be null
    }

    private int getQuantityOrDefault(@Nonnull CommandContext ctx) {
        Integer q = getOptionalQuantity(ctx);
        return q != null && q > 0 ? q : 1;
    }

    private Player getPlayerFromRef(@Nonnull PlayerRef ref) {
        try {
            return ref.getReference().getStore().getComponent(ref.getReference(), Player.getComponentType());
        } catch (Exception e) {
            return null;
        }
    }

    private void handleGive(@Nonnull CommandContext ctx, @Nonnull Player sender, @Nonnull PlayerRef targetRef, @Nonnull World world) {
        Player targetPlayer = getPlayerFromRef(targetRef);
        if (targetPlayer == null) {
            sender.sendMessage(Message.raw("Could not find player: " + targetRef.getUsername()));
            return;
        }

        int quantity = getQuantityOrDefault(ctx);
        String itemId = this.itemIdArg.get(ctx);

        world.execute(() -> {
            ItemStack itemStack = new ItemStack(itemId, quantity);
            targetPlayer.getInventory().getStorage().addItemStack(itemStack);
            sender.sendMessage(Message.raw("Gave " + quantity + " of " + itemId + " to " + targetPlayer.getDisplayName()));
        });
    }

    private void handleCheck(@Nonnull CommandContext ctx, @Nonnull Player sender, @Nonnull PlayerRef targetRef) {
        Player targetPlayer = getPlayerFromRef(targetRef);
        if (targetPlayer == null) {
            sender.sendMessage(Message.raw("Could not find player: " + targetRef.getUsername()));
            return;
        }

        Integer quantity = getOptionalQuantity(ctx);
        String itemId = this.itemIdArg.get(ctx);
        boolean hasItem = InventoryHelper.containsItem(targetPlayer.getInventory(), itemId);

        if (quantity == null) {
            // No quantity specified: just report presence/absence
            sender.sendMessage(Message.raw(targetPlayer.getDisplayName() + (hasItem ? " has " : " does not have ") + itemId));
        } else {
            // Quantity specified: keep previous messaging (presence-based)
            int amountInInventory = InventoryHelper.countItems(targetPlayer.getInventory().getCombinedEverything(), List.of(itemId));
            sender.sendMessage(Message.raw(targetPlayer.getDisplayName() + " has " + amountInInventory + " of " + itemId + (amountInInventory >= quantity ? " (enough)" : " (not enough, needs " + quantity + ")")));
        }
    }
}