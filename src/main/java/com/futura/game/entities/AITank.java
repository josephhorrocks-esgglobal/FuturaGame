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
            return buildProjectile();
        }

        return null;
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
