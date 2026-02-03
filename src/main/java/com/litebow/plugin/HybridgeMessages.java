package com.litebow.plugin;

import com.hypixel.hytale.server.core.Message;

import java.awt.*;

public class HybridgeMessages {
    public static final Message BLUE_TEAM_WINS = Message.raw("Blue Team Wins!").color(Color.BLUE);
    public static final Message BLUE_TEAM_SCORED = Message.raw("Blue Team Scored!").color(Color.BLUE);

    public static final Message RED_TEAM_WINS = Message.raw("Red Team Wins!").color(Color.RED);
    public static final Message RED_TEAM_SCORED = Message.raw("Red Team Scored!").color(Color.RED);

    public static final Message TIME_OUT = Message.raw("Time has run out!").color(Color.YELLOW);

    public  static final Message ALREADY_IN_GAME = Message.raw("You can't queue while you're in a game.").color(Color.RED);
    public static final Message NO_AVAILABLE_GAMES = Message.raw("There are no available games to join right now. Please try again later.").color(Color.RED);

    public static final Message TITLE_GAME_ENDED = Message.raw("Game Ended").bold(true).color(Color.YELLOW);
    public static final Message SUBTITLE_GAME_ENDED = Message.raw("Returning to lobby...").color(Color.WHITE);

    public static final Message TITLE_RED_SCORED = Message.raw("Red Team Scored!").bold(true).color(Color.RED);
    public static final Message TITLE_BLUE_SCORED = Message.raw("Blue Team Scored!").bold(true).color(Color.BLUE);
}
