package com.litebow.plugin;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.HashSet;

public class GameModel {
    public enum Team{
        RED,
        BLUE
    }

    private HashSet<Player> gameQueue = new HashSet<>();
    private HashSet<Player> gameInGame = new HashSet<>();

    private HashMap<Player, Team> playerTeams = new HashMap<>();

    private int redGoals = 0;
    private int blueGoals = 0;

    private float timeRemaining = HybridgeConstants.GAME_DURATION_MILLISECONDS;

    public enum GameState{
        QUEUEING,
        ACTIVE,
        DISABLED
    }

    private GameState currentState = GameState.DISABLED;

    //the map used to create this game. Info from the map will be used during the game;
    public MapModel map;

    public GameModel(MapModel map){
        this.map = map;
    }

    public void setGameState(GameState newState){
        this.currentState = newState;
    }

    public int getPlayersInGame() {
        return gameInGame.size();
    }

    public GameState getCurrentState(){
        return this.currentState;
    }

    public boolean addPlayerToQueue(Player player){
        Hybridge.LOGGER.atInfo().log("Adding player " + player.getDisplayName() + " to game queue.");
        return gameQueue.add(player);
    }

    public boolean removePlayerFromQueue(Player player){
        return gameQueue.remove(player);
    }

    public boolean transferAllPlayersToGame(){
        if(gameQueue.isEmpty()){
            return false;
        }
        gameInGame.addAll(gameQueue);

        //Assign teams alternately
        Team teamToAssign = Team.RED;
        for(Player player : gameQueue){
            playerTeams.put(player, teamToAssign);
            if(teamToAssign == Team.RED){
                teamToAssign = Team.BLUE;
            } else {
                teamToAssign = Team.RED;
            }
        }

        gameQueue.clear();
        return true;
    }

    public HashSet<Player> getPlayersInGameSet(){
        return this.gameInGame;
    }

    //hack fix so I don't have to refactor again
    public HashSet<PlayerRef> getPlayerRefsInGameSet(){
        HashSet<PlayerRef> playerRefs = new HashSet<>();
        for(Player player : this.gameInGame){
            playerRefs.add(player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType()));
        }
        return playerRefs;
    }

    public HashMap<Player, Team> getPlayerTeams() {
        return playerTeams;
    }

    public boolean isPlayerInGame(Player player){
        return gameInGame.contains(player);
    }

    public void resetGameProperties(){
        this.redGoals = 0;
        this.blueGoals = 0;
        this.playerTeams.clear();
        this.gameInGame.clear();
        this.timeRemaining = HybridgeConstants.GAME_DURATION_MILLISECONDS;
    }

    public void incrementTeamScore(Team team){
        if(team == Team.RED){
            redGoals++;
        }
        else if(team == Team.BLUE){
            blueGoals++;
        }
    }

    public int getTeamScore(Team team){
        if(team == Team.RED){
            return redGoals;
        }
        else if(team == Team.BLUE){
            return blueGoals;
        }
        return 0;
    }

    //I know this is weird, sorry
    public void setTimeRemaining(float timeMs){
        this.timeRemaining = timeMs;
    }

    //I should change this later, time should probably just always be stored here instead of BridgeGame
    public float getTimeRemaining(){
        return this.timeRemaining;
    }
}
