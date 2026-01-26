package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.HytaleServer;

import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BridgeGame {
    public GameModel gameModel;

    private volatile ScheduledFuture<?> gameTimerTask;
    private long remainingMs = HybridgeConstants.GAME_DURATION_MILLISECONDS;

    public BridgeGame(MapModel map){
        this.gameModel = new GameModel(map);
    }

    public boolean canPlaceBlock(Vector3i blockPosition){
        //you also can't place blocks during the starting / stopping phases but that's not implemented yet
        //TODO: implement that
        return HybridgeUtils.isPositionWithinArea(blockPosition.toVector3d(), gameModel.map.getBuildAreaMin(), gameModel.map.getBuildAreaMax());
    }

    public boolean canBreakBlock(BlockType blockType, Vector3i blockPosition){
        if(HybridgeUtils.isBlockValidBridgeBlock(blockType)){
            return true;
        }
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
            teleportPlayerToTeamSpawn(player);
            HybridgeUtils.providePlayerWithBridgeItems(player, team);
        }

        // To whoever reads this code: There is NO WAY THIS IS SAFE.
        // Please help me find a better, safer way to do this! :)
        gameTimerTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(()->{
            update();
            remainingMs -= HybridgeConstants.GAME_TICK_MILLISECONDS;
        },0, HybridgeConstants.GAME_TICK_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    public void stopGame() {
        gameModel.setGameState(GameModel.GameState.QUEUEING);
        for (Player player : gameModel.getPlayersInGameSet()) {
            //teleport players back to lobby or some safe location
            var entityRef = player.getReference();
            Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " back to lobby.");
            HybridgeUtils.teleportPlayer(entityRef, HybridgeConstants.SPAWN_LOCATION);
            HybridgeUtils.clearPlayerInventory(player);
        }
        gameModel.resetGameProperties();
        cancelTimer();
    }

    private void cancelTimer() {
        ScheduledFuture<?> f = gameTimerTask;
        if (f != null) {
            f.cancel(false);
            gameTimerTask = null;
        }
        remainingMs = HybridgeConstants.GAME_DURATION_MILLISECONDS;
    }

    private void update(){
        if(gameModel.getCurrentState() != GameModel.GameState.ACTIVE){
            return;
        }

//        Hybridge.LOGGER.atInfo().log("Game time remaining: " + (remainingMs / 1000) + " seconds.");

        for(Player player : gameModel.getPlayersInGameSet()){
            var entityRef = player.getReference();
            HybridgeUtils.withEntityPosition(entityRef, (pos) -> {
                if(pos.getY() < gameModel.map.getKillPlaneY()){
                    Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " fell below kill plane. Respawning.");
                    teleportPlayerToTeamSpawn(player);
                }
                //are we within the red goal area?
                if(HybridgeUtils.isPositionWithinArea(pos, gameModel.map.getRedGoalPos1(), gameModel.map.getRedGoalPos2())){
                    GameModel.Team playerTeam = gameModel.getPlayerTeams().get(player);
                    switch(playerTeam){
                        case RED:
                            Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered RED goal area but is on RED team. No score.");
                            teleportPlayerToTeamSpawn(player);
                            break;
                        case BLUE:
                            Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered BLUE goal area. RED team scores!");
                            gameModel.incrementTeamScore(GameModel.Team.RED);
                            teleportAllInGamePlayersToTeamSpawn();
                            break;
                    }
                }
                //are we within the blue goal area?
                if(HybridgeUtils.isPositionWithinArea(pos, gameModel.map.getBlueGoalPos1(), gameModel.map.getBlueGoalPos2())){
                    GameModel.Team playerTeam = gameModel.getPlayerTeams().get(player);
                    switch(playerTeam){
                        case BLUE:
                            Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered BLUE goal area but is on BLUE team. No score.");
                            teleportPlayerToTeamSpawn(player);
                            break;
                        case RED:
                            Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered RED goal area. BLUE team scores!");
                            gameModel.incrementTeamScore(GameModel.Team.BLUE);
                            teleportAllInGamePlayersToTeamSpawn();
                            break;
                    }
                }
            });
        }

        if(remainingMs <= 0){
            Hybridge.LOGGER.atInfo().log("Game time over. Stopping game.");
            stopGame();
        }
    }

    private void teleportPlayerToTeamSpawn(Player player){
        GameModel.Team team = gameModel.getPlayerTeams().get(player);
        var entityRef = player.getReference();
        Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " to team " + team + " spawn point.");
        if(team == GameModel.Team.RED){
            HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getRedTeamSpawn());
        } else if(team == GameModel.Team.BLUE){
            HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getBlueTeamSpawn());
        }
    }

    private void teleportAllInGamePlayersToTeamSpawn(){
        for(Player player : gameModel.getPlayersInGameSet()){
            teleportPlayerToTeamSpawn(player);
        }
    }

    //BridgeGame handles all logic, rules, and state transitions for a single game instance.
}
