package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class HybridgeConstants {
    public static final long GAME_TICK_MILLISECONDS = 100L;

    public static final long GAME_DURATION_MINUTES = (long) 5.0f;
    public static final long GAME_DURATION_MILLISECONDS = GAME_DURATION_MINUTES * 60 * 1000;
    public static final long GAME_DURATION_SECONDS = GAME_DURATION_MINUTES * 60;

    public static final Vector3d SPAWN_LOCATION = new Vector3d(678, 121, -68);

    public static final int GOALS_TO_WIN = 5;

    public static final ItemStack RED_BLOCK = new ItemStack("Soil_Clay_Smooth_Red", 100);
    public static final ItemStack BLUE_BLOCK = new ItemStack("Soil_Clay_Smooth_Blue", 100);
    public static final ItemStack WHITE_BLOCK = new ItemStack("Soil_Clay_Smooth_White", 100);

    public static final ItemStack SWORD = new ItemStack("Weapon_Sword_Thorium", 1);
    public static final ItemStack SHIELD = new ItemStack("Weapon_Shield_Wood",1);
    public static final ItemStack PICK = new ItemStack("Tool_Pickaxe_Cobalt",1);
    public static final ItemStack BOW = new ItemStack("Weapon_Shortbow_Iron", 1);
    public static final ItemStack ARROW = new ItemStack("Weapon_Arrow_Crude", 1);

    public static final ItemStack HEAL = new ItemStack("Potion_Health_Greater", 4);

}
