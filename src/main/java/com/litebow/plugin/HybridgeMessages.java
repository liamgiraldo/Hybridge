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
}
