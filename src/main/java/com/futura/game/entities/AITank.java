package com.futura.game.entities;

import com.futura.game.config.GameConfig;
import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;

import java.awt.Color;

public class AITank extends Tank {
    public AITank(Vector2 position) {
        super(position,
                Math.PI / 2.0,
                new Color(212, 84, 66),
                GameConfig.AI_MOVE_SPEED,
                GameConfig.AI_ROTATION_SPEED,
                GameConfig.TANK_RADIUS);
    }

    public Projectile updateAndTryShoot(double deltaTime, Tank target, ArenaMap map) {
        update(deltaTime);

        Vector2 toTarget = target.getPosition().subtract(getPosition());
        double desiredAngle = Math.atan2(toTarget.y(), toTarget.x());
        double angleDelta = normalizeAngle(desiredAngle - getRotation());

        if (angleDelta > 0.08) {
            rotateRight(deltaTime);
        } else if (angleDelta < -0.08) {
            rotateLeft(deltaTime);
        }

        if (Math.abs(angleDelta) < 0.55) {
            moveForward(deltaTime, map, target);
        }

        if (Math.abs(angleDelta) < 0.15 && canShoot()) {
            if (isShotSafe(map, target)) {
                return buildProjectile();
            }
        }

        return null;
    }

    private boolean isShotSafe(ArenaMap map, Tank target) {
        Vector2 simulatedPosition = getMuzzlePosition();
        Vector2 simulatedVelocity = getForwardDirection().scale(GameConfig.PROJECTILE_SPEED);
        double projectileRadius = GameConfig.PROJECTILE_RADIUS;
        double elapsed = 0.0;
        int bounces = 0;

        while (elapsed < GameConfig.PROJECTILE_LIFETIME_SECONDS) {
            double nextX = simulatedPosition.x() + simulatedVelocity.x() * GameConfig.DELTA_TIME;
            boolean xBlocked = isBlocked(map, nextX, simulatedPosition.y(), projectileRadius);
            if (xBlocked) {
                simulatedVelocity = new Vector2(-simulatedVelocity.x(), simulatedVelocity.y());
                bounces++;
            } else {
                simulatedPosition = new Vector2(nextX, simulatedPosition.y());
            }

            double nextY = simulatedPosition.y() + simulatedVelocity.y() * GameConfig.DELTA_TIME;
            boolean yBlocked = isBlocked(map, simulatedPosition.x(), nextY, projectileRadius);
            if (yBlocked) {
                simulatedVelocity = new Vector2(simulatedVelocity.x(), -simulatedVelocity.y());
                bounces++;
            } else {
                simulatedPosition = new Vector2(simulatedPosition.x(), nextY);
            }

            if (bounces > 3) {
                return true;
            }

            if (Vector2.distance(simulatedPosition, target.getPosition()) <= projectileRadius + target.getRadius()) {
                return true;
            }

            if (Vector2.distance(simulatedPosition, getPosition()) <= projectileRadius + getRadius()) {
                return false;
            }

            elapsed += GameConfig.DELTA_TIME;
        }

        return true;
    }

    private boolean isBlocked(ArenaMap map, double x, double y, double radius) {
        return !map.isInsideBounds(x, y, radius) || map.collidesWithObstacle(x, y, radius);
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= Math.PI * 2.0;
        }
        while (angle < -Math.PI) {
            angle += Math.PI * 2.0;
        }
        return angle;
    }
}
