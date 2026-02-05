package com.litebow.plugin;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

public class MapModel {
    private String mapName;
    private String mapAuthor;
    private String mapDescription;


    //each map will have properties that need to be defined.
    /**
     * spawn location for each team (can just be a single point),
     * position of the goals (using two corners to define a cuboid area),
     * boundaries of the map (to prevent players from going out of bounds),
     * the kill plane height (y level at which players die),
     * building bounds (area within which players can place blocks),
     * map's origin point (usually at the center of the bridge structure).
     * */
    private Vector3d redTeamSpawn;
    private Vector3d blueTeamSpawn;

    private Vector3f redTeamSpawnRotation;
    private Vector3f blueTeamSpawnRotation;

    private Vector3d redGoalPos1;
    private Vector3d redGoalPos2;
    private Vector3d blueGoalPos1;
    private Vector3d blueGoalPos2;
    private Vector3d buildAreaMin;
    private Vector3d buildAreaMax;
    private double killPlaneY;

    //these should encompass the entire map area for saving /loading purposes
    private Vector3i mapBound1;
    private Vector3i mapBound2;
    //this is for copying and pasting the map relative to an origin point
    private Vector3d mapOrigin;

    public MapModel(String mapName, String mapAuthor, String mapDescription,
                    Vector3d mapOrigin,
                    Vector3d redTeamSpawn, Vector3d blueTeamSpawn,
                    Vector3d redGoalPos1, Vector3d redGoalPos2,
                    Vector3d blueGoalPos1, Vector3d blueGoalPos2,
                    Vector3d buildAreaMin, Vector3d buildAreaMax,
                    double killPlaneY, Vector3i mapBound1, Vector3i mapBound2, Vector3f redTeamSpawnRotation, Vector3f blueTeamSpawnRotation) {
        this.mapName = mapName;
        this.mapAuthor = mapAuthor;
        this.mapDescription = mapDescription;
        this.mapOrigin = mapOrigin;
        this.redTeamSpawn = redTeamSpawn;
        this.blueTeamSpawn = blueTeamSpawn;
        this.redGoalPos1 = redGoalPos1;
        this.redGoalPos2 = redGoalPos2;
        this.blueGoalPos1 = blueGoalPos1;
        this.blueGoalPos2 = blueGoalPos2;
        this.buildAreaMin = buildAreaMin;
        this.buildAreaMax = buildAreaMax;
        this.killPlaneY = killPlaneY;

        this.mapBound1 = mapBound1;
        this.mapBound2 = mapBound2;

        this.redTeamSpawnRotation = redTeamSpawnRotation;
        this.blueTeamSpawnRotation = blueTeamSpawnRotation;

    }

    public String getMapName() {
        return mapName;
    }

    public String getMapAuthor() {
        return mapAuthor;
    }

    public String getMapDescription() {
        return mapDescription;
    }

    public Vector3d getRedTeamSpawn() {
        return redTeamSpawn;
    }

    public Vector3d getBlueTeamSpawn() {
        return blueTeamSpawn;
    }

    public Vector3d getRedGoalPos1() {
        return redGoalPos1;
    }

    public Vector3d getRedGoalPos2() {
        return redGoalPos2;
    }

    public Vector3d getBlueGoalPos1() {
        return blueGoalPos1;
    }

    public Vector3d getBlueGoalPos2() {
        return blueGoalPos2;
    }

    public Vector3d getBuildAreaMin() {
        return buildAreaMin;
    }

    public Vector3d getBuildAreaMax() {
        return buildAreaMax;
    }

    public double getKillPlaneY() {
        return killPlaneY;
    }

    public Vector3i getMapOrigin() {
        //idk why maporigin is a 3d when it really should be a 3i
        //TODO: fix tech debt later
        return new Vector3i((int)mapOrigin.getX(), (int)mapOrigin.getY(), (int)mapOrigin.getZ());
    }

    public Vector3i getMapBound1() {
        return mapBound1;
    }

    public Vector3i getMapBound2() {
        return mapBound2;
    }

    //I need new getters that account for a z offset
    //this feels really clunky
    public Vector3d getRedTeamSpawnWithOffset(int zOffset) {
        return new Vector3d(redTeamSpawn.getX(), redTeamSpawn.getY(), redTeamSpawn.getZ() + zOffset);
    }
    public Vector3d getBlueTeamSpawnWithOffset(int zOffset) {
        return new Vector3d(blueTeamSpawn.getX(), blueTeamSpawn.getY(), blueTeamSpawn.getZ() + zOffset);
    }
    public Vector3d getBuildAreaMinWithOffset(int zOffset) {
        return new Vector3d(buildAreaMin.getX(), buildAreaMin.getY(), buildAreaMin.getZ() + zOffset);
    }
    public Vector3d getBuildAreaMaxWithOffset(int zOffset) {
        return new Vector3d(buildAreaMax.getX(), buildAreaMax.getY(), buildAreaMax.getZ() + zOffset);
    }
    public Vector3d getRedGoalPos1WithOffset(int zOffset) {
        return new Vector3d(redGoalPos1.getX(), redGoalPos1.getY(), redGoalPos1.getZ() + zOffset);
    }
    public Vector3d getRedGoalPos2WithOffset(int zOffset) {
        return new Vector3d(redGoalPos2.getX(), redGoalPos2.getY(), redGoalPos2.getZ() + zOffset);
    }
    public Vector3d getBlueGoalPos1WithOffset(int zOffset) {
        return new Vector3d(blueGoalPos1.getX(), blueGoalPos1.getY(), blueGoalPos1.getZ() + zOffset);
    }
    public Vector3d getBlueGoalPos2WithOffset(int zOffset) {
        return new Vector3d(blueGoalPos2.getX(), blueGoalPos2.getY(), blueGoalPos2.getZ() + zOffset);
    }
    public Vector3i getMapBound1WithOffset(int zOffset) {
        return new Vector3i(mapBound1.getX(), mapBound1.getY(), mapBound1.getZ() + zOffset);
    }
    public Vector3i getMapBound2WithOffset(int zOffset) {
        return new Vector3i(mapBound2.getX(), mapBound2.getY(), mapBound2.getZ() + zOffset);
    }
    public Vector3i getMapOriginWithOffset(int zOffset) {
        return new Vector3i((int)mapOrigin.getX(), (int)mapOrigin.getY(), (int)mapOrigin.getZ() + zOffset);
    }

    public Vector3f getRedTeamSpawnRotation() {
        return redTeamSpawnRotation;
    }

    public Vector3f getBlueTeamSpawnRotation() {
        return blueTeamSpawnRotation;
    }
}
