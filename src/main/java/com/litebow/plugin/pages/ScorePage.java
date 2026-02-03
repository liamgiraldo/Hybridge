package com.litebow.plugin.pages;

import au.ellie.hyui.builders.HudBuilder;
import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.builders.LabelBuilder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.litebow.plugin.GameModel;
import com.litebow.plugin.Hybridge;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ScorePage {
    private String html = """
<div id="main" style="layout-mode: top; anchor-left: 50; anchor-top: 350">
  <p id="redScoreLabel" style="color: #ff0000">Red Team Score:</p>
  <p id="redScore" style="color: #ffffff">Text</p>
  <p id="blueTeamScore" style="color: #0400ff">Blue Team Score:</p>
  <p id="blueScore" style="color: #ffffff">Text</p>
  <p id="timeLabel" style="color: #ffffff">Time Remaining: </p>
  <p id="time" style="color: #ffffff">Text</p>
</div>
            """;

    private HudBuilder builder;

    private final ConcurrentHashMap<PlayerRef, HyUIHud> hudByPlayer = new ConcurrentHashMap<>();

    private HashSet<PlayerRef> players;

    public ScorePage(HashSet<PlayerRef> playerRefList, GameModel game){
        builder = HudBuilder.detachedHud().fromHtml(html).withRefreshRate(200).onRefresh(
                hyUIHud -> {
                    hyUIHud.getById("blueScore", LabelBuilder.class).ifPresent(labelBuilder -> {
                        labelBuilder.withText(""+(game.getTeamScore(GameModel.Team.BLUE)));
                    });
                    hyUIHud.getById("redScore", LabelBuilder.class).ifPresent(labelBuilder -> {
                        labelBuilder.withText(""+game.getTeamScore(GameModel.Team.RED));
                    });
                    hyUIHud.getById("time", LabelBuilder.class).ifPresent(labelBuilder -> {
                        long remainingMs = (long)game.getTimeRemaining();
                        long seconds = (remainingMs / 1000) % 60;
                        long minutes = (remainingMs / (1000 * 60)) % 60;
                        String timeString = String.format("%02d:%02d", minutes, seconds);
                        labelBuilder.withText(timeString);
                    });
                }
        );

        for(PlayerRef playerRef : playerRefList){
            var store = playerRef.getReference().getStore();
            var world = store.getExternalData().getWorld();
            world.execute(() -> {
                HyUIHud hud = builder.show(playerRef, store);
                hudByPlayer.put(playerRef, hud);
            });
        }
    }

    public void removeAll() {
        for (var entry : hudByPlayer.entrySet()) {
            PlayerRef playerRef = entry.getKey();
            HyUIHud hud = entry.getValue();

            var ref = playerRef.getReference();
            var store = ref.getStore();
            World world = store.getExternalData().getWorld();

            world.execute(() -> hud.remove());
        }
        hudByPlayer.clear();
    }
}
