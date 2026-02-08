package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class HybridgeConstants {
    public static final long GAME_TICK_MILLISECONDS = 300L;

    public static final long GAME_DURATION_MINUTES = (long) 5.0f;
    public static final long GAME_DURATION_MILLISECONDS = GAME_DURATION_MINUTES * 60 * 1000;
    public static final long GAME_DURATION_SECONDS = GAME_DURATION_MINUTES * 60;

    public static final Vector3d SPAWN_LOCATION = new Vector3d(678, 121, -68);

    public static final int GOALS_TO_WIN = 5;

    public static final int CAGE_COUNTDOWN_SECONDS = 5;
    public static final long CAGE_COUNTDOWN_MILLISECONDS = CAGE_COUNTDOWN_SECONDS * 1000L;
    // Temporary, to be replaced with value map setup file
    public static final int CAGE_HEIGHT_ABOVE_SPAWN = 4;
    // Temporary, to be removed with addition of cage templates
    public static final String CAGE_BLOCK_ID = "Barrier";

    public static final int ARROW_TIMER_SECONDS = 3;
    public static final long ARROW_TIMER_MILLISECONDS = ARROW_TIMER_SECONDS * 1000L;

    public static final ItemStack RED_BLOCK = new ItemStack("Soil_Clay_Smooth_Red", 100);
    public static final ItemStack BLUE_BLOCK = new ItemStack("Soil_Clay_Smooth_Blue", 100);
    public static final ItemStack WHITE_BLOCK = new ItemStack("Soil_Clay_Smooth_White", 100);

    public static final ItemStack SWORD = new ItemStack("Weapon_Sword_Thorium", 1);
    public static final ItemStack SHIELD = new ItemStack("Weapon_Shield_Wood", 1);
    public static final ItemStack PICK = new ItemStack("Tool_Pickaxe_Cobalt", 1);
    public static final ItemStack BOW = new ItemStack("Weapon_Shortbow_Iron", 1);
    public static final ItemStack ARROW = new ItemStack("Weapon_Arrow_Crude", 1);

    public static final ItemStack HEAL = new ItemStack("Potion_Health_Greater", 4);

    public static final String SFX_DIE = "SFX_Player_Death";
    public static final String SFX_GOAL = "SFX_Discovery_Z1_Short";
    // Tick sound for cage countdown. Should probably add separate sound for cage release
    public static final String SFX_CAGE_COUNTDOWN_TICK = "SFX_Blunderbuss_No_Ammo";
    public static final String SFX_ARROW_GIVE = "SFX_Player_Pickup_Item";

}
