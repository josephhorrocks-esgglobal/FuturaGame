package com.futura.game.entities;

import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;
import com.futura.game.config.GameConfig;

import java.awt.Color;
import java.awt.Graphics2D;

public class Projectile extends Entity {
    private Vector2 velocity;
    private final double radius;
    private double lifeRemainingSeconds;
    private double ageSeconds;

    public Projectile(Vector2 position, double rotation, Vector2 velocity, double radius, double lifeRemainingSeconds) {
        super(position, rotation);
        this.velocity = velocity;
        this.radius = radius;
        this.lifeRemainingSeconds = lifeRemainingSeconds;
        this.ageSeconds = 0.0;
    }

    public void update(double deltaTime, ArenaMap map) {
        double nextX = position.x() + velocity.x() * deltaTime;
        boolean xBlocked = isBlocked(map, nextX, position.y());
        if (xBlocked) {
            velocity = new Vector2(-velocity.x(), velocity.y());
        } else {
            position = new Vector2(nextX, position.y());
        }

        double nextY = position.y() + velocity.y() * deltaTime;
        boolean yBlocked = isBlocked(map, position.x(), nextY);
        if (yBlocked) {
            velocity = new Vector2(velocity.x(), -velocity.y());
        } else {
            position = new Vector2(position.x(), nextY);
        }

        lifeRemainingSeconds -= deltaTime;
        ageSeconds += deltaTime;
    }

    @Override
    public void update(double deltaTime) {
        position = position.add(velocity.scale(deltaTime));
        lifeRemainingSeconds -= deltaTime;
        ageSeconds += deltaTime;
    }

    @Override
    public void render(Graphics2D g2d) {
        int d = (int) Math.round(radius * 2.0);
        int x = (int) Math.round(position.x() - radius);
        int y = (int) Math.round(position.y() - radius);

        g2d.setColor(new Color(40, 40, 40));
        g2d.fillOval(x, y, d, d);
    }

    public boolean isExpired() {
        return lifeRemainingSeconds <= 0.0 || ageSeconds >= GameConfig.PROJECTILE_MAX_AGE_SECONDS;
    }

    private boolean isBlocked(ArenaMap map, double x, double y) {
        return !map.isInsideBounds(x, y, radius) || map.collidesWithObstacle(x, y, radius);
    }

    public double getRadius() {
        return radius;
    }
}
