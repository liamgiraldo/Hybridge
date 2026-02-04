package com.litebow.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CopyPasteCommand extends AbstractPlayerCommand {
    RequiredArg<Vector3i> blockPos1Arg;
    RequiredArg<Vector3i> blockPos2Arg;

    public class CopiedBlock{
        public Vector3i relativePos;
        public Vector3i globalPos;
        public BlockType blockType;
        public CopiedBlock(Vector3i relativePos, Vector3i globalPos, BlockType blockType){
            this.relativePos = relativePos;
            this.globalPos = globalPos;
            this.blockType = blockType;
        }
    }

    public class CopyPasteSelection {
        public Vector3i pos1;
        public Vector3i pos2;

        private List<CopiedBlock> blockData = new ArrayList<>();

        public CopyPasteSelection(Vector3i pos1, Vector3i pos2, List<CopiedBlock> blockData) {
            this.blockData = blockData;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public List<CopiedBlock> getBlockData() {
            return blockData;
        }
    }

    private static HashMap<PlayerRef, CopyPasteSelection> selections = new HashMap<>();

    public CopyPasteCommand() {
        super("selectarea", "selects an area for copying and pasting");
        this.blockPos1Arg = this.withRequiredArg("blockPos1", "first corner of the area", ArgTypes.VECTOR3I);
        this.blockPos2Arg = this.withRequiredArg("blockPos2", "second corner of the area", ArgTypes.VECTOR3I);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = (Player) commandContext.sender();
        PlayerRef playerReference = player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
        World playerWorld = playerReference.getReference().getStore().getExternalData().getWorld();

        Vector3i pos1 = this.blockPos1Arg.get(commandContext);
        Vector3i pos2 = this.blockPos2Arg.get(commandContext);
        Vector3i min = new Vector3i(
                Math.min(pos1.x, pos2.x),
                Math.min(pos1.y, pos2.y),
                Math.min(pos1.z, pos2.z)
        );
        Vector3i max = new Vector3i(
                Math.max(pos1.x, pos2.x),
                Math.max(pos1.y, pos2.y),
                Math.max(pos1.z, pos2.z));

        world.execute(()-> {
            List<CopiedBlock> blockData = new ArrayList<>();
            for (int x = min.x; x <= max.x; x++) {
                for (int y = min.y; y <= max.y; y++) {
                    for (int z = min.z; z <= max.z; z++) {
                        Vector3i currentPos = new Vector3i(x, y, z);
                        BlockType blockType = world.getBlockType(currentPos);
                        CopiedBlock copiedBlock = new CopiedBlock(
                                new Vector3i(x - min.x, y - min.y, z - min.z),
                                currentPos,
                                blockType
                        );
                        blockData.add(copiedBlock);
                    }
                }
            }
            CopyPasteSelection selection = new CopyPasteSelection(pos1, pos2, blockData);
            selections.put(playerRef, selection);

        });
    }

    public static class PasteCommand extends AbstractPlayerCommand {
        public PasteCommand() {
            super("pastearea", "pastes the previously selected area at the player's current position");
        }

        @Override
        protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            Player player = (Player) commandContext.sender();
            CopyPasteSelection selection = selections.get(playerRef);
            if (selection == null) {
                player.sendMessage(Message.raw("No area selected to paste. Use /selectarea first."));
                return;
            }

            Vector3d playerPos = player.getReference().getStore().getComponent(player.getReference(), TransformComponent.getComponentType()).getPosition();
            Vector3i basePos = new Vector3i((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);

            world.execute(() -> {
                for (CopiedBlock copiedBlock : selection.getBlockData()) {
                    Vector3i targetPos = new Vector3i(
                            basePos.x + copiedBlock.relativePos.x,
                            basePos.y + copiedBlock.relativePos.y,
                            basePos.z + copiedBlock.relativePos.z
                    );
                    if(copiedBlock.blockType == null || copiedBlock.blockType.getGroup().equals("Air")) continue;
                    var key = copiedBlock.blockType.getId();
                    world.setBlock(targetPos.x, targetPos.y, targetPos.z, key);
                }
            });
        }
    }
}
