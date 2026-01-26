package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BridgeService {
    private ArrayList <BridgeGame> games = new ArrayList<>();

    private HashMap<PlayerRef, BridgeGame> playerGameMap = new HashMap<>();

    public BridgeService(){

    }

    public void createGame(MapModel map){
        //I know this passes in a MapModel, but i'm going to ignore it for now and create a default map
        Vector3d redSpawn = new Vector3d(715,172,-65);
        Vector3d blueSpawn = new Vector3d(661,172,-65);
        Vector3d mapOrigin = new Vector3d(688,168,-65);
        Vector3d redGoalPos1 = new Vector3d(715,166,-66);
        Vector3d redGoalPos2 = new Vector3d(717,166,-64);
        Vector3d blueGoalPos1 = new Vector3d(659,166,-66);
        Vector3d blueGoalPos2 = new Vector3d(661,166,-64);
        Vector3d buildAreaMin = new Vector3d(713,159,-56);
        Vector3d buildAreaMax = new Vector3d(663,173,-74);
        double killPlaneY = 155;
        MapModel defaultMap = new MapModel(
                "Default Bridge Map",
                "Litebow",
                "A simple default map for Bridge games.",
                mapOrigin,
                redSpawn,
                blueSpawn,
                redGoalPos1,
                redGoalPos2,
                blueGoalPos1,
                blueGoalPos2,
                buildAreaMin,
                buildAreaMax,
                killPlaneY
        );

        BridgeGame newGame = new BridgeGame(defaultMap);
        newGame.gameModel.setGameState(GameModel.GameState.QUEUEING);
        games.add(newGame);

        Hybridge.LOGGER.atInfo().log("Created new Bridge game with default map.");
    }

    public boolean joinQueue(Player player){
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
        return false;
    }

    public void startGame(BridgeGame game){
        game.startGame();
        game.gameModel.getPlayerRefsInGameSet().forEach(player -> playerGameMap.put(player, game));
    }

    public void stopGame(BridgeGame game){
        game.stopGame();
        game.gameModel.getPlayersInGameSet().forEach(player -> playerGameMap.remove(player));
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
}
