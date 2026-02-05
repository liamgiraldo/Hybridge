package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.litebow.plugin.maps.PlaceholderMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BridgeService {
    private ArrayList <BridgeGame> games = new ArrayList<>();

    private HashMap<PlayerRef, BridgeGame> playerGameMap = new HashMap<>();

    public BridgeService(){
    }

    public void createGame(MapModel map, World world){
        var mapOrigin = map.getMapOrigin();
        //so this might be a littel confusing, but we're going to copy the map to create a new game instance
        var zWidth = map.getMapBound2().getZ() - map.getMapBound1().getZ();

        int zOffset = (int)(zWidth * games.size() + 1);
        Vector3i pastePosition = new Vector3i(
                (int)mapOrigin.getX(),
                (int)mapOrigin.getY(),
                (int)(mapOrigin.getZ() + zOffset)
        );

        //copy the map to the new position
        var copiedBlocks = HybridgeUtils.getBlocksInArea(world,
                map.getMapBound1(),
                map.getMapBound2(), map.getMapOrigin()
        );
        HybridgeUtils.setBlocksInArea(world, pastePosition, copiedBlocks);

        BridgeGame newGame = new BridgeGame(map, this::stopGame, zOffset);
        newGame.gameModel.setGameState(GameModel.GameState.QUEUEING);
        games.add(newGame);

        Hybridge.LOGGER.atInfo().log("Created new Bridge game with " + map.getMapName() + " at offset " + zOffset);
    }

    public boolean joinQueue(Player player){
        PlayerRef ref = player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
        if(ref == null){
            return false;
        }
        if(playerGameMap.containsKey(ref)){
            //player is already in a game
            ref.sendMessage(HybridgeMessages.ALREADY_IN_GAME);
            return false;
        }
        BridgeGame gameToJoin = null;
        //find queue with most players
        for(BridgeGame game : games){
            if(game.gameModel.getCurrentState() == GameModel.GameState.QUEUEING){
                if(gameToJoin == null || game.gameModel.getPlayersInGame() > gameToJoin.gameModel.getPlayersInGame()){
                    gameToJoin = game;
                }
            }
        }
        if(gameToJoin != null){
            gameToJoin.gameModel.addPlayerToQueue(player);
            return true;
        }
        else{
            ref.sendMessage(HybridgeMessages.NO_AVAILABLE_GAMES);
        }
        return false;
    }

    public void startGame(BridgeGame game){
        game.startGame();
        game.gameModel.getPlayerRefsInGameSet().forEach(player -> playerGameMap.put(player, game));
    }

    public void stopGame(BridgeGame game){
        game.gameModel.getPlayerRefsInGameSet().forEach(playerGameMap::remove);
        game.performStopActions();
    }

    public void leaveQueue(PlayerRef player){
        //remove player from game queue
    }

    /**
     * I shouldn't need this later, just for testing purposes
     * */
    public ArrayList<BridgeGame> getGames(){
        return this.games;
    }

    public BridgeGame getPlayerGame(PlayerRef player){
        return playerGameMap.get(player);
    }

    public void removeAllGames(){
        for(BridgeGame game : games){
            stopGame(game);
            //by this point the maps should already have been reset but we need to literally DELETE them
            HybridgeUtils.setBlocksInAreaAir(game.getWorld(), game.gameModel.map.getMapBound1WithOffset(game.getzOffset()), game.gameModel.map.getMapBound2WithOffset(game.getzOffset()));
        }
        games.clear();
        playerGameMap.clear();
    }
}
