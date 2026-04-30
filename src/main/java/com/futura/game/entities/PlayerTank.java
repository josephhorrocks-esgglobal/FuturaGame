package com.futura.game.entities;

import com.futura.game.config.GameConfig;
import com.futura.game.input.InputHandler;
import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class PlayerTank extends Tank {
    public PlayerTank(Vector2 position) {
        super(position,
                -Math.PI / 2.0,
                new Color(56, 120, 210),
                GameConfig.PLAYER_MOVE_SPEED,
                GameConfig.PLAYER_ROTATION_SPEED,
                GameConfig.TANK_RADIUS);
    }

    public Projectile updateAndTryShoot(double deltaTime, InputHandler input, ArenaMap map, Tank opponentTank) {
        update(deltaTime);

        if (input.isKeyDown(KeyEvent.VK_A)) {
            rotateLeft(deltaTime);
        }
        if (input.isKeyDown(KeyEvent.VK_D)) {
            rotateRight(deltaTime);
        }
        if (input.isKeyDown(KeyEvent.VK_W)) {
            moveForward(deltaTime, map, opponentTank);
        }
        if (input.isKeyDown(KeyEvent.VK_S)) {
            moveBackward(deltaTime, map, opponentTank);
        }

        if (input.isKeyDown(KeyEvent.VK_SPACE) && canShoot()) {
            return buildProjectile();
        }

        return null;
    }
}
