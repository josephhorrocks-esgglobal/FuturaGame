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

        int hullSize = (int) Math.round(radius * 2.0);
        int halfHull = hullSize / 2;
        int treadWidth = 5;
        int cornerArc = 6;

        tankGraphics.setColor(new Color(34, 34, 34));
        tankGraphics.fillRoundRect(-halfHull - treadWidth, -halfHull + 2, treadWidth, hullSize - 4, 4, 4);
        tankGraphics.fillRoundRect(halfHull, -halfHull + 2, treadWidth, hullSize - 4, 4, 4);

        tankGraphics.setColor(bodyColor);
        tankGraphics.fillRoundRect(-halfHull, -halfHull, hullSize, hullSize, cornerArc, cornerArc);

        tankGraphics.setColor(bodyColor.darker());
        tankGraphics.drawRoundRect(-halfHull, -halfHull, hullSize, hullSize, cornerArc, cornerArc);

        int turretSize = (int) Math.round(radius * 1.1);
        int halfTurret = turretSize / 2;
        tankGraphics.setColor(new Color(58, 58, 58));
        tankGraphics.fillOval(-halfTurret, -halfTurret, turretSize, turretSize);

        tankGraphics.setColor(new Color(28, 28, 28));
        tankGraphics.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int barrelStart = halfTurret - 1;
        int barrelEnd = halfHull + 12;
        tankGraphics.drawLine(barrelStart, 0, barrelEnd, 0);

        tankGraphics.setColor(new Color(220, 220, 220, 160));
        tankGraphics.fillOval(-halfTurret + 4, -halfTurret + 3, turretSize / 3, turretSize / 3);

        tankGraphics.setTransform(oldTransform);
        tankGraphics.dispose();
    }

    public double getRadius() {
        return radius;
    }
}
