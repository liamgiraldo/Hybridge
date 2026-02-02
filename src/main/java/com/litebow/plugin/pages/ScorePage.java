package com.litebow.plugin.pages;

import au.ellie.hyui.builders.HudBuilder;
import au.ellie.hyui.builders.HyUIHud;
import au.ellie.hyui.builders.LabelBuilder;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.litebow.plugin.GameModel;
import com.litebow.plugin.Hybridge;

import java.util.HashSet;
import java.util.List;

public class ScorePage {
    private String html = """
            <style>
                #blueScoreLabel{
                    color: #0057f7;
                }
                #redScoreLabel{
                    color: #f71900
                }
            </style>
            <div>
                <p id="blueScoreLabel">Blue Team Score: </p>
                <p id="blueScore">0/5</p>
                <p id="redScoreLabel">Red Team Score: </p>
                <p id="redScore">0/5</p>
            </div>
            """;

    private HudBuilder page;

    private HashSet<PlayerRef> players;

    public ScorePage(HashSet<PlayerRef> playerRefList, GameModel game){
        page = HudBuilder.detachedHud().fromHtml(html).withRefreshRate(200).onRefresh(
                hyUIHud -> {
                    hyUIHud.getById("blueScore", LabelBuilder.class).ifPresent(labelBuilder -> {
                        labelBuilder.withText(""+(game.getTeamScore(GameModel.Team.BLUE)));
                    });
                    hyUIHud.getById("redScore", LabelBuilder.class).ifPresent(labelBuilder -> {
                        labelBuilder.withText(""+game.getTeamScore(GameModel.Team.RED));
                    });
                }
        );

        for(PlayerRef ref : playerRefList){
            var playerWorld = ref.getReference().getStore().getExternalData().getWorld();
            playerWorld.execute(()->{
                page.show(ref);
            });
        }
    }

    public void removeFromPage(PlayerRef playerRef){
        H
    }
}
