package com.futuragame.ai;

import com.futuragame.entities.Tank;
import com.futuragame.math.Vector2;

public class AiTankController {
    private double fireCooldownSeconds = 0.0;

    public boolean update(Tank aiTank, Tank playerTank, double deltaTime) {
        Vector2 toPlayer = playerTank.getPosition().copy().subtract(aiTank.getPosition());
        double targetRotation = Math.atan2(toPlayer.y, toPlayer.x);
        double angleDiff = normalizeAngle(targetRotation - aiTank.getRotationRadians());

        double maxTurn = 1.5 * deltaTime;
        if (Math.abs(angleDiff) > maxTurn) {
            aiTank.rotate(Math.signum(angleDiff) * maxTurn);
        } else {
            aiTank.rotate(angleDiff);
        }

        aiTank.move(55.0 * deltaTime);

        fireCooldownSeconds -= deltaTime;
        if (Math.abs(angleDiff) < 0.2 && fireCooldownSeconds <= 0.0) {
            fireCooldownSeconds = 0.95;
            return true;
        }

        return false;
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
