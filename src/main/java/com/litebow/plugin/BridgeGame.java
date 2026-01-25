package com.litebow.plugin;

import com.hypixel.hytale.server.core.entity.entities.Player;

public class BridgeGame {
    public GameModel gameModel;

    public BridgeGame(MapModel map){
        this.gameModel = new GameModel(map);
    }

    public boolean canPlaceBlock(){
        return false;
    }

    public boolean canBreakBlock(){
        return false;
    }

    public boolean canTakeDamage(){
        return false;
    }

    public boolean canDropItem(){
        return false;
    }

    public void startGame(){
        gameModel.transferAllPlayersToGame();
        gameModel.setGameState(GameModel.GameState.ACTIVE);

        for(Player player : gameModel.getPlayersInGameSet()){
            //teleport players to their team spawn points
            GameModel.Team team = gameModel.getPlayerTeams().get(player);
            var entityRef = player.getReference();
            Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " to team " + team + " spawn point.");
            if(team == GameModel.Team.RED){
                HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getRedTeamSpawn());
            } else if(team == GameModel.Team.BLUE){
                HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getBlueTeamSpawn());
            }
        }
    }

    //BridgeGame handles all logic, rules, and state transitions for a single game instance.
}
