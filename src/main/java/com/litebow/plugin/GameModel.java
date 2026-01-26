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
    }

    public void incrementTeamScore(Team team){
        if(team == Team.RED){
            redGoals++;
        }
        else if(team == Team.BLUE){
            blueGoals++;
        }
    }
}
