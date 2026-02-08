package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.litebow.plugin.pages.ScorePage;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BridgeGame {
    public interface GameLifecycleListener {
        void onGameEnd(BridgeGame game);
    }

    public GameModel gameModel;

    private World world;

    private ScorePage scoreboard;

    private volatile ScheduledFuture<?> gameTimerTask;
    private volatile ScheduledFuture<?> cageCountdownTask;
    private final AtomicInteger cageCountdownRemaining = new AtomicInteger(0);
    private long remainingMs = HybridgeConstants.GAME_DURATION_MILLISECONDS;

    private HashMap<PlayerRef, AtomicLong> playerArrowTimers = new HashMap<>();
    private HashMap<PlayerRef, AtomicInteger> playerLastArrowCount = new HashMap<>();

    private final GameLifecycleListener lifecycleListener;

    private int zOffset = 0;

    public BridgeGame(MapModel map, GameLifecycleListener lifecycleListener, int zOffset, World world) {
        this.lifecycleListener = lifecycleListener;
        this.gameModel = new GameModel(map);
        this.zOffset = zOffset;
        this.world = world;
    }

    private Player getPlayerFromRef(PlayerRef playerRef) {
        return playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
    }


    public boolean canPlaceBlock(Vector3i blockPosition, PlayerRef playerRef) {
        //if the player isn't in the game, they CAN place blocks (probably in the lobby)
        //TODO: Make sure people can't place the health potions
        if (!gameModel.getPlayerRefsInGameSet().contains(playerRef)) {
            return true;
        }
        if (gameModel.getCurrentState() == GameModel.GameState.CAGE) {
            return false;
        }
        return HybridgeUtils.isPositionWithinArea(blockPosition.toVector3d(), gameModel.map.getBuildAreaMinWithOffset(zOffset), gameModel.map.getBuildAreaMaxWithOffset(zOffset));
    }

    public boolean canBreakBlock(BlockType blockType, Vector3i blockPosition, PlayerRef playerRef) {
        //if the player isn't in the game, they CAN break blocks (probably in the lobby)
        if (!gameModel.getPlayerRefsInGameSet().contains(playerRef)) {
            return true;
        }
        if (gameModel.getCurrentState() == GameModel.GameState.CAGE) {
            return false;
        }
        if (HybridgeUtils.isBlockValidBridgeBlock(blockType)) {
            return true;
        }
        return false;
    }

    public boolean canTakeDamage(Player damager, Player damageReceiver) {
        //There will be other conditions later, but for now just prevent team damage
        //TODO: implement other conditions later
        if (gameModel.getPlayerTeams().get(damager) != gameModel.getPlayerTeams().get(damageReceiver)) {
            return true;
        }
        return false;
    }

    public boolean canDropItem(PlayerRef playerRef, String itemId) {
        Player player = getPlayerFromRef(playerRef);
        if (itemId.equals(HybridgeConstants.ARROW.getItemId())) {
            //prevent dropping arrows
            return false;
        }
        return true;
    }

    public void startGame() {
        gameModel.transferAllPlayersToGame();
        gameModel.setGameState(GameModel.GameState.CAGE);

        //this is so astronomically fucked but I don't know a better way to do it right now
        gameModel.getPlayersInGameSet().stream().findFirst().ifPresent(player -> {
            World world = player.getReference().getStore().getExternalData().getWorld();
            this.world = world;
        });

        scoreboard = new ScorePage(gameModel.getPlayerRefsInGameSet(), gameModel);

        // To whoever reads this code: There is NO WAY THIS IS SAFE.
        // Please help me find a better, safer way to do this! :)
        gameTimerTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            update();
            remainingMs -= HybridgeConstants.GAME_TICK_MILLISECONDS;
            gameModel.setTimeRemaining(remainingMs);
        }, 0, HybridgeConstants.GAME_TICK_MILLISECONDS, TimeUnit.MILLISECONDS);

        gameModel.getPlayerRefsInGameSet().stream().forEach(playerRef -> {
            playerArrowTimers.put(playerRef, new AtomicLong(0));
            playerLastArrowCount.put(playerRef, new AtomicInteger(0));
        });

        enterCageState();
    }

    public void performStopActions() {
        if (world != null && (gameModel.getCurrentState() == GameModel.GameState.CAGE || gameModel.getCurrentState() == GameModel.GameState.ACTIVE)) {
            world.execute(() -> {
                HybridgeUtils.removeCageSync(world, getRedCageCenter());
                HybridgeUtils.removeCageSync(world, getBlueCageCenter());
            });
        }
        cancelTimer();
        gameModel.setGameState(GameModel.GameState.QUEUEING);
        for (Player player : gameModel.getPlayersInGameSet()) {
            //teleport players back to lobby or some safe location
            var entityRef = player.getReference();
            Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " back to lobby.");
            HybridgeUtils.teleportPlayer(entityRef, HybridgeConstants.SPAWN_LOCATION);
            HybridgeUtils.clearPlayerInventory(player);

            resetGameMap();

            PlayerRef playerRef = player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
            EventTitleUtil.showEventTitleToPlayer(playerRef, HybridgeMessages.TITLE_GAME_ENDED, HybridgeMessages.SUBTITLE_GAME_ENDED, true, null, 3, 1, 1);
        }

        //TODO: This isn't removing the player from the BridgeService's playerGameMap, fix that

        gameModel.resetGameProperties();

        if (scoreboard != null)
            scoreboard.removeAll();
        scoreboard = null;

        playerArrowTimers.clear();
        playerLastArrowCount.clear();
    }

    private void cancelTimer() {
        ScheduledFuture<?> f = gameTimerTask;
        if (f != null) {
            f.cancel(false);
            gameTimerTask = null;
        }
        ScheduledFuture<?> cageF = cageCountdownTask;
        if (cageF != null) {
            cageF.cancel(false);
            cageCountdownTask = null;
        }
        remainingMs = HybridgeConstants.GAME_DURATION_MILLISECONDS;
    }

    // Can move game state + winning out of the loop and swap over to event based triggers
    private void update() {
        //THIS IS SO SCUFFFFEEEEDDDDD
        world.execute(() -> {

            if (gameModel.getCurrentState() != GameModel.GameState.ACTIVE) {
                return;
            }

//        Hybridge.LOGGER.atInfo().log("Game time remaining: " + (remainingMs / 1000) + " seconds.");

            for (Player player : gameModel.getPlayersInGameSet()) {
                var entityRef = player.getReference();
                HybridgeUtils.withEntityPosition(entityRef, (pos) -> {
                    if (pos.getY() < gameModel.map.getKillPlaneY()) {
                        Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " fell below kill plane. Respawning.");
                        teleportPlayerToTeamSpawn(player);
                        HybridgeUtils.setPlayerHealthFull(player);
                        HybridgeUtils.playSoundEffectToPlayer(player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType()), HybridgeConstants.SFX_DIE);
                    }
                    // IDEA: are we within any goal areacase with ? two cases: if (isOwnGoal), else if (isNotOwnGoal), then tp all players + add point,
                    // could add placeholder for case with more than two teams
                    //are we within the red goal area?
                    if (HybridgeUtils.isPositionWithinArea(pos, gameModel.map.getRedGoalPos1WithOffset(zOffset), gameModel.map.getRedGoalPos2WithOffset(zOffset))) {
                        GameModel.Team playerTeam = gameModel.getPlayerTeams().get(player);
                        if (playerTeam == null) return;
                        switch (playerTeam) {
                            case RED:
                                teleportPlayerToTeamSpawn(player);
                                HybridgeUtils.setPlayerHealthFull(player);
                                break;
                            case BLUE:
                                Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered RED goal area. BLUE team scores!");
                                gameModel.incrementTeamScore(GameModel.Team.BLUE);
                                HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), HybridgeMessages.BLUE_TEAM_SCORED);
                                HybridgeUtils.showTitleToCollectionOfPlayers(gameModel.getPlayerRefsInGameSet(),
                                        HybridgeMessages.TITLE_BLUE_SCORED,
                                        Message.empty(),
                                        1,
                                        1,
                                        1);
                                HybridgeUtils.playSoundEffectToPlayer(player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType()), HybridgeConstants.SFX_GOAL);
                                if (isGameWon() == null) {
                                    gameModel.setGameState(GameModel.GameState.CAGE);
                                    enterCageState();
                                }
                                break;
                        }
                    }
                    //are we within the blue goal area?
                    if (HybridgeUtils.isPositionWithinArea(pos, gameModel.map.getBlueGoalPos1WithOffset(zOffset), gameModel.map.getBlueGoalPos2WithOffset(zOffset))) {
                        GameModel.Team playerTeam = gameModel.getPlayerTeams().get(player);
                        if (playerTeam == null) return;
                        switch (playerTeam) {
                            case BLUE:
                                Hybridge.LOGGER.atInfo().log("Player " + player.getDisplayName() + " entered BLUE goal area but is on BLUE team. No score.");
                                teleportPlayerToTeamSpawn(player);
                                HybridgeUtils.setPlayerHealthFull(player);
                                break;
                            case RED:
                                gameModel.incrementTeamScore(GameModel.Team.RED);
                                HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), HybridgeMessages.RED_TEAM_SCORED);
                                HybridgeUtils.showTitleToCollectionOfPlayers(gameModel.getPlayerRefsInGameSet(),
                                        HybridgeMessages.TITLE_RED_SCORED,
                                        Message.empty(),
                                        1,
                                        1,
                                        1);
                                HybridgeUtils.playSoundEffectToPlayer(player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType()), HybridgeConstants.SFX_GOAL);
                                if (isGameWon() == null) {
                                    gameModel.setGameState(GameModel.GameState.CAGE);
                                    enterCageState();
                                }
                                break;
                        }
                    }
                });

                detectArrowUsage(getPlayerRefFromPlayer(player));
                tickArrowTimers(getPlayerRefFromPlayer(player));
            }

            if (isGameWon() != null) {
                GameModel.Team winningTeam = isGameWon();
                Hybridge.LOGGER.atInfo().log("Team " + winningTeam + " has won the game! Stopping game.");
                HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(),
                        winningTeam == GameModel.Team.RED ? HybridgeMessages.RED_TEAM_WINS : HybridgeMessages.BLUE_TEAM_WINS);
                requestStopFromService();
                return;
            }

            if (remainingMs <= 0) {
                Hybridge.LOGGER.atInfo().log("Game time over. Stopping game.");
                HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), HybridgeMessages.TIME_OUT);
                requestStopFromService();
            }

        });
    }

    private PlayerRef getPlayerRefFromPlayer(Player player) {
        return player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
    }

    public void teleportPlayerToTeamSpawn(Player player) {
        GameModel.Team team = gameModel.getPlayerTeams().get(player);
        var entityRef = player.getReference();
        Hybridge.LOGGER.atInfo().log("Teleporting player " + player.getDisplayName() + " to team " + team + " spawn point.");
        if (team == GameModel.Team.RED) {
            HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getRedTeamSpawnWithOffset(zOffset), gameModel.map.getRedTeamSpawnRotation());
        } else if (team == GameModel.Team.BLUE) {
            HybridgeUtils.teleportPlayer(entityRef, gameModel.map.getBlueTeamSpawnWithOffset(zOffset), gameModel.map.getBlueTeamSpawnRotation());
        }

        //TODO: separate this logic later
        HybridgeUtils.clearInventoryExceptItemIds(player, List.of(HybridgeConstants.ARROW.getItemId()), false, false);
        HybridgeUtils.providePlayerWithBridgeItems(player, team);
    }

    private void teleportAllInGamePlayersToTeamSpawn() {
        for (Player player : gameModel.getPlayersInGameSet()) {
            teleportPlayerToTeamSpawn(player);
        }
    }

    private void healAllInGamePlayers() {
        for (Player player : gameModel.getPlayersInGameSet()) {
            HybridgeUtils.setPlayerHealthFull(player);
        }
    }

    private GameModel.Team isGameWon() {
        if (gameModel.getTeamScore(GameModel.Team.RED) >= HybridgeConstants.GOALS_TO_WIN) {
            return GameModel.Team.RED;
        } else if (gameModel.getTeamScore(GameModel.Team.BLUE) >= HybridgeConstants.GOALS_TO_WIN) {
            return GameModel.Team.BLUE;
        }
        return null;
    }

    private void requestStopFromService() {
        if (lifecycleListener != null) {
            lifecycleListener.onGameEnd(this);
        }
    }

    // These will be updated later with more advanced cage models / schematic storing
    // (also just ONE method, not red + blue)
    private Vector3i getRedCageCenter() {
        Vector3d spawn = gameModel.map.getRedTeamSpawnWithOffset(zOffset);
        return new Vector3i((int) Math.floor(spawn.getX()), (int) Math.floor(spawn.getY()) + HybridgeConstants.CAGE_HEIGHT_ABOVE_SPAWN, (int) Math.floor(spawn.getZ()));
    }

    private Vector3i getBlueCageCenter() {
        Vector3d spawn = gameModel.map.getBlueTeamSpawnWithOffset(zOffset);
        return new Vector3i((int) Math.floor(spawn.getX()), (int) Math.floor(spawn.getY()) + HybridgeConstants.CAGE_HEIGHT_ABOVE_SPAWN, (int) Math.floor(spawn.getZ()));
    }

    /**
     * Teleport position inside cage: floor of interior (center.y - 1) so players don't spawn on top of the cage.
     */
    private static Vector3d cageCenterToTeleportPosition(Vector3i center) {
        return new Vector3d(center.x + 0.5, center.y - 0.5, center.z + 0.5);
    }

    public void enterCageState() {
        if (world == null) return;
        world.execute(() -> {
            Vector3i redCenter = getRedCageCenter();
            Vector3i blueCenter = getBlueCageCenter();
            HybridgeUtils.placeCageSync(world, redCenter);
            HybridgeUtils.placeCageSync(world, blueCenter);

            for (Player player : gameModel.getPlayersInGameSet()) {
                GameModel.Team team = gameModel.getPlayerTeams().get(player);
                if (team == null) continue;
                Vector3d cagePos = team == GameModel.Team.RED ? cageCenterToTeleportPosition(redCenter) : cageCenterToTeleportPosition(blueCenter);
                HybridgeUtils.teleportPlayer(player.getReference(), cagePos, team == GameModel.Team.RED ? gameModel.map.getRedTeamSpawnRotation() : gameModel.map.getBlueTeamSpawnRotation());
                HybridgeUtils.providePlayerWithBridgeItems(player, team);
                HybridgeUtils.setPlayerHealthFull(player);
            }

            cageCountdownRemaining.set(HybridgeConstants.CAGE_COUNTDOWN_SECONDS);
            HybridgeUtils.sendMessageToCollectionOfPlayers(gameModel.getPlayersInGameSet(), Message.raw(String.valueOf(HybridgeConstants.CAGE_COUNTDOWN_SECONDS)).color(Color.YELLOW).bold(true));
            HybridgeUtils.playSoundEffectToPlayersOnWorldThread(gameModel.getPlayerRefsInGameSet(), HybridgeConstants.SFX_CAGE_COUNTDOWN_TICK);

            BridgeGame game = this;
            final ScheduledFuture<?>[] countdownFutureBox = new ScheduledFuture<?>[1];
            countdownFutureBox[0] = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
                int n = game.cageCountdownRemaining.decrementAndGet();
                ScheduledFuture<?> f = countdownFutureBox[0];
                game.world.execute(() -> {
                    if (n > 0) {
                        HybridgeUtils.sendMessageToCollectionOfPlayers(game.gameModel.getPlayersInGameSet(), Message.raw(String.valueOf(n)).color(Color.YELLOW).bold(true));
                        HybridgeUtils.playSoundEffectToPlayersOnWorldThread(game.gameModel.getPlayerRefsInGameSet(), HybridgeConstants.SFX_CAGE_COUNTDOWN_TICK);
                    } else {
                        HybridgeUtils.sendMessageToCollectionOfPlayers(game.gameModel.getPlayersInGameSet(), Message.raw("Go!").color(Color.GREEN).bold(true));
                        f.cancel(false);
                        game.cageCountdownTask = null;
                        game.endCageState();
                    }
                });
            }, 1, 1, TimeUnit.SECONDS);
            cageCountdownTask = countdownFutureBox[0];
        });
    }

    private void endCageState() {

        gameModel.setGameState(GameModel.GameState.ACTIVE);
        Vector3i redCenter = getRedCageCenter();
        Vector3i blueCenter = getBlueCageCenter();
        HybridgeUtils.removeCageSync(world, redCenter);
        HybridgeUtils.removeCageSync(world, blueCenter);
        cageCountdownTask = null;
    }

    private void resetGameMap() {
        //we're going to copy the original map area, and paste it where this game instance is located
        var block = HybridgeUtils.getBlocksInArea(world, gameModel.map.getMapBound1(), gameModel.map.getMapBound2(), gameModel.map.getMapOrigin());
        HybridgeUtils.setBlocksInArea(world, gameModel.map.getMapOriginWithOffset(zOffset), block);
    }

    public World getWorld() {
        return world;
    }

    public int getzOffset() {
        return zOffset;
    }

    private void giveArrowToPlayer(PlayerRef playerRef) {
        Player player = getPlayerFromRef(playerRef);
        if (player == null) return;
        ItemStack arrowStack = new ItemStack(HybridgeConstants.ARROW.getItemId(), 1);
        player.getInventory().getStorage().addItemStack(arrowStack);
        HybridgeUtils.playSoundEffectToPlayer(playerRef, HybridgeConstants.SFX_ARROW_GIVE);
    }

    private void tickArrowTimers(PlayerRef pr) {
        AtomicLong timer = playerArrowTimers.get(pr);
        long newVal = timer.addAndGet(-HybridgeConstants.GAME_TICK_MILLISECONDS);

        if (newVal <= 0) {
            Player player = getPlayerFromRef(pr);
            if (player == null) return;

            int currentArrows = InventoryHelper.countItems(player.getInventory().getCombinedEverything(), List.of(HybridgeConstants.ARROW.getItemId()));
            if (currentArrows <= 0) {
                giveArrowToPlayer(pr);
                timer.set(HybridgeConstants.ARROW_TIMER_MILLISECONDS);
            }
        }
    }

    private void detectArrowUsage(PlayerRef playerRef) {
        Player player = getPlayerFromRef(playerRef);
        if (player == null) return;
        // get the current arrow count for the player, compare it to the last known arrow count, and if it's less, that means they've used an arrow
        // TODO: edge case where player drops an arrow and can dupe them
        int currentArrowCount = InventoryHelper.countItems(player.getInventory().getCombinedEverything(), List.of(HybridgeConstants.ARROW.getItemId()));
        AtomicInteger lastCount = playerLastArrowCount.computeIfAbsent(playerRef, pr -> new AtomicInteger(currentArrowCount));
        int previousArrowCount = lastCount.getAndSet(currentArrowCount);
        if (currentArrowCount < previousArrowCount) {
            playerArrowTimers.put(playerRef, new AtomicLong(HybridgeConstants.ARROW_TIMER_MILLISECONDS));
        }
    }


    //BridgeGame handles all logic, rules, and state transitions for a single game instance.
}
