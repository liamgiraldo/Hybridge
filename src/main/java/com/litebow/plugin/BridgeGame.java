package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.HytaleServer;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.litebow.plugin.pages.ScorePage;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BridgeGame {
    public interface GameLifecycleListener{
        void onGameEnd(BridgeGame game);
    }

    public GameModel gameModel;

    private World world;

    private ScorePage scoreboard;

    private volatile ScheduledFuture<?> gameTimerTask;
    private long remainingMs = HybridgeConstants.GAME_DURATION_MILLISECONDS;

    private final GameLifecycleListener lifecycleListener;

    public BridgeGame(MapModel map, GameLifecycleListener lifecycleListener){
        this.lifecycleListener = lifecycleListener;
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

    public boolean canTakeDamage(Player damager, Player damageReceiver){
        //There will be other conditions later, but for now just prevent team damage
        //TODO: implement other conditions later
        if(gameModel.getPlayerTeams().get(damager) != gameModel.getPlayerTeams().get(damageReceiver)){
            return true;
        }
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

            PlayerRef playerRef = player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
        }

        //this is so astronomically fucked but I don't know a better way to do it right now
        gameModel.getPlayersInGameSet().stream().findFirst().ifPresent(player -> {
            World world = player.getReference().getStore().getExternalData().getWorld();
            this.world = world;
        });


        scoreboard = new ScorePage(gameModel.getPlayerRefsInGameSet(), gameModel);

        // To whoever reads this code: There is NO WAY THIS IS SAFE.
        // Please help me find a better, safer way to do this! :)
        gameTimerTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(()->{
            update();
            remainingMs -= HybridgeConstants.GAME_TICK_MILLISECONDS;
            gameModel.setTimeRemaining(remainingMs);
        },0, HybridgeConstants.GAME_TICK_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    public void performStopActions() {
        gameModel.setGameState(GameModel.GameState.QUEUEING);
        for (Player player : gameModel.getPlayersInGameSet()) {
            //teleport players back to lobby or some safe location
            var entityRef = player.getReference();
            Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " back to lobby.");
            HybridgeUtils.teleportPlayer(entityRef, HybridgeConstants.SPAWN_LOCATION);
            HybridgeUtils.clearPlayerInventory(player);

            PlayerRef playerRef = player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
            EventTitleUtil.showEventTitleToPlayer(playerRef, HybridgeMessages.TITLE_GAME_ENDED, HybridgeMessages.SUBTITLE_GAME_ENDED, true, null, 3, 1, 1);
        }

        cancelTimer();

        //TODO: This isn't removing the player from the BridgeService's playerGameMap, fix that

        gameModel.resetGameProperties();

        scoreboard.removeAll();
        scoreboard = null;
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
        //THIS IS SO SCUFFFFEEEEDDDDD
        world.execute(() -> {


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
                    HybridgeUtils.setPlayerHealthFull(player);
                }
                //are we within the red goal area?
                if(HybridgeUtils.isPositionWithinArea(pos, gameModel.map.getRedGoalPos1(), gameModel.map.getRedGoalPos2())){
                    GameModel.Team playerTeam = gameModel.getPlayerTeams().get(player);
                    switch(playerTeam){
                        case RED:
                            teleportPlayerToTeamSpawn(player);
                            HybridgeUtils.setPlayerHealthFull(player);
                            break;
                        case BLUE:
                            Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered RED goal area. BLUE team scores!");
                            gameModel.incrementTeamScore(GameModel.Team.BLUE);
                            teleportAllInGamePlayersToTeamSpawn();
                            HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), HybridgeMessages.BLUE_TEAM_SCORED);
                            HybridgeUtils.showTitleToCollectionOfPlayers(gameModel.getPlayerRefsInGameSet(),
                                    HybridgeMessages.TITLE_BLUE_SCORED,
                                    Message.empty(),
                                    1,
                                    1,
                                    1);
                            healAllInGamePlayers();
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
                            HybridgeUtils.setPlayerHealthFull(player);
                            break;
                        case RED:
                            gameModel.incrementTeamScore(GameModel.Team.RED);
                            teleportAllInGamePlayersToTeamSpawn();
                            HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), HybridgeMessages.RED_TEAM_SCORED);
                            HybridgeUtils.showTitleToCollectionOfPlayers(gameModel.getPlayerRefsInGameSet(),
                                    HybridgeMessages.TITLE_RED_SCORED,
                                    Message.empty(),
                                    1,
                                    1,
                                    1);
                            healAllInGamePlayers();
                            break;
                    }
                }
            });

        }

        if(isGameWon() != null){
            GameModel.Team winningTeam = isGameWon();
            Hybridge.LOGGER.atInfo().log("Team " + winningTeam + " has won the game! Stopping game.");
            HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(),
                    winningTeam == GameModel.Team.RED ? HybridgeMessages.RED_TEAM_WINS : HybridgeMessages.BLUE_TEAM_WINS);
            requestStopFromService();
            return;
        }

        if(remainingMs <= 0){
            Hybridge.LOGGER.atInfo().log("Game time over. Stopping game.");
            HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), HybridgeMessages.TIME_OUT);
            requestStopFromService();
        }

        });
    }

    public void teleportPlayerToTeamSpawn(Player player){
        GameModel.Team team = gameModel.getPlayerTeams().get(player);
        var entityRef = player.getReference();
        Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " to team " + team + " spawn point.");
        if(team == GameModel.Team.RED){
            HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getRedTeamSpawn(), gameModel.map.getRedTeamSpawnRotation());
        } else if(team == GameModel.Team.BLUE){
            HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getBlueTeamSpawn(), gameModel.map.getBlueTeamSpawnRotation());
        }

        //TODO: separate this logic later
        HybridgeUtils.clearPlayerInventory(player);
        HybridgeUtils.providePlayerWithBridgeItems(player, team);
    }

    private void teleportAllInGamePlayersToTeamSpawn(){
        for(Player player : gameModel.getPlayersInGameSet()){
            teleportPlayerToTeamSpawn(player);
        }
    }

    private void healAllInGamePlayers(){
        for(Player player : gameModel.getPlayersInGameSet()){
            HybridgeUtils.setPlayerHealthFull(player);
        }
    }

    private GameModel.Team isGameWon(){
        if(gameModel.getTeamScore(GameModel.Team.RED) >= HybridgeConstants.GOALS_TO_WIN){
            return GameModel.Team.RED;
        }
        else if(gameModel.getTeamScore(GameModel.Team.BLUE) >= HybridgeConstants.GOALS_TO_WIN){
            return GameModel.Team.BLUE;
        }
        return null;
    }

    private void requestStopFromService() {
        if (lifecycleListener != null) {
            lifecycleListener.onGameEnd(this);
        }
    }


    //BridgeGame handles all logic, rules, and state transitions for a single game instance.
}
