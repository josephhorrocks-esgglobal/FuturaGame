package com.futura.game.config;

public final class GameConfig {
    public static final String WINDOW_TITLE = "Futura Game - Tank Duel";
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;

    public static final int TARGET_FPS = 60;
    public static final double DELTA_TIME = 1.0 / TARGET_FPS;

    public static final double PLAYER_MOVE_SPEED = 170.0;
    public static final double PLAYER_ROTATION_SPEED = Math.PI;

    public static final double AI_MOVE_SPEED = 140.0;
    public static final double AI_ROTATION_SPEED = Math.PI * 0.8;

    public static final double PROJECTILE_SPEED = 320.0;
    public static final double PROJECTILE_LIFETIME_SECONDS = 3.5;
    public static final double SHOOT_COOLDOWN_SECONDS = 0.75;
    public static final double TANK_RADIUS = 18.0;
    public static final int TANK_MAX_HEALTH = 3;

    public static final double SLOW_ZONE_RADIUS = 52.0;
    public static final int SLOW_PATCH_COUNT = 3;
    public static final double SLOW_MULTIPLIER = 0.5;

    public static final int CITY_MAP_BUILDING_COUNT = 12;
    public static final int CITY_MAP_LAKE_COUNT = 4;

    public static final double DRAGON_MIN_SPAWN_SECONDS = 10.0;
    public static final double DRAGON_MAX_SPAWN_SECONDS = 20.0;
    public static final double DRAGON_SPEED = 230.0;
    public static final double DRAGON_FIRE_RANGE = 520.0;
    public static final double DRAGON_CORNER_SAFE_SIZE = 115.0;
    public static final double DRAGON_BOMB_WARNING_SECONDS = 1.2;
    public static final double DRAGON_BOMB_EXPLOSION_SECONDS = 0.65;
    public static final double DRAGON_BOMB_EXPLOSION_RADIUS = 72.0;
    public static final double DRAGON_BOMB_SCATTER = 60.0;

    private GameConfig() {
    }
}
