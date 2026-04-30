package com.futuragame.core;

import com.futuragame.ai.AiTankController;
import com.futuragame.entities.Obstacle;
import com.futuragame.entities.Projectile;
import com.futuragame.entities.Tank;
import com.futuragame.input.InputState;
import com.futuragame.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameState {
    private final Tank playerTank = new Tank(new Vector2(140, 220), new Color(38, 153, 66));
    private final Tank aiTank = new Tank(new Vector2(640, 220), new Color(171, 43, 43));
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Obstacle> obstacles = createObstacles();
    private final AiTankController aiController = new AiTankController();

    private double playerFireCooldown = 0.0;
    private int playerScore = 0;
    private int aiScore = 0;

    public void update(double deltaTime, InputState inputState, int worldWidth, int worldHeight) {
        updatePlayer(inputState, deltaTime);
        if (aiController.update(aiTank, playerTank, deltaTime)) {
            projectiles.add(aiTank.fire(260.0));
        }

        confineTank(playerTank, worldWidth, worldHeight);
        confineTank(aiTank, worldWidth, worldHeight);
        resolveObstacleCollision(playerTank);
        resolveObstacleCollision(aiTank);

        updateProjectiles(deltaTime, worldWidth, worldHeight);
    }

    private void updatePlayer(InputState inputState, double deltaTime) {
        if (inputState.rotateLeft) {
            playerTank.rotate(-2.2 * deltaTime);
        }
        if (inputState.rotateRight) {
            playerTank.rotate(2.2 * deltaTime);
        }
        if (inputState.moveForward) {
            playerTank.move(120.0 * deltaTime);
        }
        if (inputState.moveBackward) {
            playerTank.move(-90.0 * deltaTime);
        }

        playerFireCooldown -= deltaTime;
        if (inputState.firePressed && playerFireCooldown <= 0.0) {
            projectiles.add(playerTank.fire(320.0));
            playerFireCooldown = 0.35;
        }
    }

    private void updateProjectiles(double deltaTime, int worldWidth, int worldHeight) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile projectile = it.next();
            projectile.update(deltaTime);
            Vector2 pos = projectile.getPosition();
            if (pos.x < 0 || pos.y < 0 || pos.x > worldWidth || pos.y > worldHeight) {
                it.remove();
                continue;
            }

            if (hitsTank(projectile, playerTank)) {
                aiScore++;
                resetRound();
                return;
            }
            if (hitsTank(projectile, aiTank)) {
                playerScore++;
                resetRound();
                return;
            }

            if (hitsObstacle(projectile)) {
                it.remove();
            }
        }
    }

    private boolean hitsTank(Projectile projectile, Tank tank) {
        double dx = projectile.getPosition().x - tank.getPosition().x;
        double dy = projectile.getPosition().y - tank.getPosition().y;
        double minDist = projectile.getRadius() + tank.getRadius() * 0.8;
        return ((dx * dx) + (dy * dy)) < (minDist * minDist);
    }

    private boolean hitsObstacle(Projectile projectile) {
        Rectangle bulletBounds = new Rectangle(
                (int) (projectile.getPosition().x - projectile.getRadius()),
                (int) (projectile.getPosition().y - projectile.getRadius()),
                (int) (projectile.getRadius() * 2),
                (int) (projectile.getRadius() * 2)
        );
        for (Obstacle obstacle : obstacles) {
            if (obstacle.getBounds().intersects(bulletBounds)) {
                return true;
            }
        }
        return false;
    }

    private void resetRound() {
        projectiles.clear();
        playerTank.setPosition(new Vector2(140, 220));
        aiTank.setPosition(new Vector2(640, 220));
    }

    private void confineTank(Tank tank, int width, int height) {
        Vector2 p = tank.getPosition();
        double r = tank.getRadius();
        p.x = Math.max(r, Math.min(width - r, p.x));
        p.y = Math.max(r, Math.min(height - r, p.y));
    }

    private void resolveObstacleCollision(Tank tank) {
        Rectangle tankBounds = new Rectangle(
                (int) (tank.getPosition().x - tank.getRadius()),
                (int) (tank.getPosition().y - tank.getRadius()),
                (int) (tank.getRadius() * 2),
                (int) (tank.getRadius() * 2)
        );

        for (Obstacle obstacle : obstacles) {
            if (!obstacle.getBounds().intersects(tankBounds)) {
                continue;
            }

            Rectangle intersection = obstacle.getBounds().intersection(tankBounds);
            if (intersection.width < intersection.height) {
                if (tank.getPosition().x < obstacle.getBounds().getCenterX()) {
                    tank.getPosition().x -= intersection.width;
                } else {
                    tank.getPosition().x += intersection.width;
                }
            } else {
                if (tank.getPosition().y < obstacle.getBounds().getCenterY()) {
                    tank.getPosition().y -= intersection.height;
                } else {
                    tank.getPosition().y += intersection.height;
                }
            }
        }
    }

    public void draw(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(36, 123, 72));
        g2d.fillRect(0, 0, width, height);

        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g2d);
        }
        playerTank.draw(g2d);
        aiTank.draw(g2d);

        for (Projectile projectile : projectiles) {
            projectile.draw(g2d);
        }

        g2d.setColor(Color.WHITE);
        g2d.drawString("Player: " + playerScore + "  AI: " + aiScore, 12, 20);
        g2d.drawString("Controls: W/S move, A/D rotate, SPACE fire", 12, 38);
    }

    private List<Obstacle> createObstacles() {
        List<Obstacle> result = new ArrayList<>();
        result.add(new Obstacle(280, 120, 90, 30));
        result.add(new Obstacle(420, 280, 90, 30));
        result.add(new Obstacle(350, 180, 30, 90));
        return result;
    }
}
