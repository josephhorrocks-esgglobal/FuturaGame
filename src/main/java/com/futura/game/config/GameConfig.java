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
    public static final double PROJECTILE_RADIUS = 4.0;
    public static final double PROJECTILE_LIFETIME_SECONDS = 3.5;
    public static final double PROJECTILE_MAX_AGE_SECONDS = 6.0;
    public static final double SHOOT_COOLDOWN_SECONDS = 0.75;
    public static final double TANK_RADIUS = 18.0;

    public static final double SLOW_ZONE_RADIUS = 52.0;
    public static final int SLOW_PATCH_COUNT = 3;
    public static final double SLOW_MULTIPLIER = 0.5;

    private GameConfig() {
    }
}
