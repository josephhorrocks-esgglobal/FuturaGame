package com.futura.game.entities;

import com.futura.game.config.GameConfig;
import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public abstract class Tank extends Entity {
    private final Color bodyColor;
    private final double baseMoveSpeed;
    private final double rotationSpeed;
    private final double radius;
    private final int maxHealth;

    private double speed;
    private double shootCooldown;
    private int health;

    protected Tank(Vector2 position,
                   double rotation,
                   Color bodyColor,
                   double moveSpeed,
                   double rotationSpeed,
                   double radius) {
        super(position, rotation);
        this.bodyColor = bodyColor;
        this.baseMoveSpeed = moveSpeed;
        this.speed = moveSpeed;
        this.rotationSpeed = rotationSpeed;
        this.radius = radius;
        this.maxHealth = GameConfig.TANK_MAX_HEALTH;
        this.health = maxHealth;
        this.shootCooldown = 0.0;
    }

    protected void rotateLeft(double deltaTime) {
        rotation -= rotationSpeed * deltaTime;
    }

    protected void rotateRight(double deltaTime) {
        rotation += rotationSpeed * deltaTime;
    }

    protected void moveForward(double deltaTime, ArenaMap map, Tank blockingTank) {
        attemptMove(speed * deltaTime, map, blockingTank);
    }

    protected void moveBackward(double deltaTime, ArenaMap map, Tank blockingTank) {
        attemptMove(-speed * deltaTime, map, blockingTank);
    }

    private void attemptMove(double distance, ArenaMap map, Tank blockingTank) {
        Vector2 forward = Vector2.fromAngle(rotation);
        Vector2 next = position.add(forward.scale(distance));
        if (blockingTank != null) {
            double minDistance = radius + blockingTank.getRadius();
            if (Vector2.distance(next, blockingTank.getPosition()) < minDistance) {
                return;
            }
        }

        if (map.isInsideBounds(next.x(), next.y(), radius) && !map.collidesWithObstacle(next.x(), next.y(), radius)) {
            position = next;
        }
    }

    protected boolean canShoot() {
        return shootCooldown <= 0.0;
    }

    protected Projectile buildProjectile() {
        Vector2 direction = Vector2.fromAngle(rotation).normalize();
        Vector2 muzzle = position.add(direction.scale(radius + 8.0));
        Vector2 velocity = direction.scale(GameConfig.PROJECTILE_SPEED);
        shootCooldown = GameConfig.SHOOT_COOLDOWN_SECONDS;
        return new Projectile(muzzle, rotation, velocity, 4.0, GameConfig.PROJECTILE_LIFETIME_SECONDS);
    }

    protected void tickCooldown(double deltaTime) {
        shootCooldown -= deltaTime;
    }

    public void reset(Vector2 newPosition, double newRotation) {
        position = newPosition;
        rotation = newRotation;
        speed = baseMoveSpeed;
        health = maxHealth;
        shootCooldown = 0.0;
    }

    public boolean applyHit() {
        if (health <= 0) {
            return false;
        }

        health--;
        return health <= 0;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        speed = baseMoveSpeed * speedMultiplier;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
    }

    public void applyNetworkState(double x, double y, double rotation) {
        this.position = new Vector2(x, y);
        this.rotation = rotation;
    }

    public double getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void update(double deltaTime) {
        tickCooldown(deltaTime);
    }

    @Override
    public void render(Graphics2D g2d) {
        Graphics2D tankGraphics = (Graphics2D) g2d.create();
        AffineTransform oldTransform = tankGraphics.getTransform();

        tankGraphics.translate(position.x(), position.y());
        tankGraphics.rotate(rotation);

        int hullWidth = (int) Math.round(radius * 2.35);
        int hullHeight = (int) Math.round(radius * 1.8);
        int halfHullWidth = hullWidth / 2;
        int halfHullHeight = hullHeight / 2;
        int treadWidth = 7;

        tankGraphics.setColor(new Color(28, 34, 38));
        tankGraphics.fillRoundRect(-halfHullWidth - treadWidth, -halfHullHeight + 2, treadWidth, hullHeight - 4, 7, 7);
        tankGraphics.fillRoundRect(halfHullWidth, -halfHullHeight + 2, treadWidth, hullHeight - 4, 7, 7);

        tankGraphics.setColor(new Color(82, 90, 96));
        for (int i = -halfHullHeight + 5; i < halfHullHeight - 2; i += 6) {
            tankGraphics.fillRect(-halfHullWidth - treadWidth + 1, i, treadWidth - 2, 3);
            tankGraphics.fillRect(halfHullWidth + 1, i, treadWidth - 2, 3);
        }

        tankGraphics.setColor(bodyColor);
        tankGraphics.fillRoundRect(-halfHullWidth, -halfHullHeight, hullWidth, hullHeight, 16, 16);

        tankGraphics.setColor(bodyColor.darker());
        tankGraphics.setStroke(new BasicStroke(2.0f));
        tankGraphics.drawRoundRect(-halfHullWidth, -halfHullHeight, hullWidth, hullHeight, 16, 16);

        tankGraphics.setColor(new Color(255, 255, 255, 80));
        tankGraphics.fillRoundRect(-halfHullWidth + 4, -halfHullHeight + 3, hullWidth - 10, 8, 8, 8);

        int turretSize = (int) Math.round(radius * 1.25);
        int halfTurret = turretSize / 2;
        tankGraphics.setColor(bodyColor.darker().darker());
        tankGraphics.fillOval(-halfTurret, -halfTurret, turretSize, turretSize);

        tankGraphics.setColor(new Color(240, 240, 240, 120));
        tankGraphics.fillOval(-halfTurret + 3, -halfTurret + 2, turretSize / 2, turretSize / 2);

        tankGraphics.setColor(new Color(44, 44, 44));
        tankGraphics.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int barrelStart = halfTurret - 1;
        int barrelEnd = halfHullWidth + 14;
        tankGraphics.drawLine(barrelStart, 0, barrelEnd, 0);

        tankGraphics.setColor(new Color(120, 120, 120));
        tankGraphics.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        tankGraphics.drawLine(barrelStart + 1, 0, barrelEnd - 2, 0);

        tankGraphics.setColor(new Color(30, 30, 30, 140));
        tankGraphics.fillOval(-4, -4, 8, 8);

        tankGraphics.setTransform(oldTransform);
        tankGraphics.dispose();
    }

    public double getRadius() {
        return radius;
    }
}
