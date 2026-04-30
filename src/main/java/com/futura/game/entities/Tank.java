package com.futura.game.entities;

import com.futura.game.config.GameConfig;
import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Tank extends Entity {
    private final Color bodyColor;
    private final double baseMoveSpeed;
    private final double rotationSpeed;
    private final double radius;

    private double speed;
    private double shootCooldown;

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

    protected Vector2 getForwardDirection() {
        return Vector2.fromAngle(rotation).normalize();
    }

    protected Vector2 getMuzzlePosition() {
        return position.add(getForwardDirection().scale(radius + 8.0));
    }

    protected Projectile buildProjectile() {
        Vector2 direction = getForwardDirection();
        Vector2 muzzle = getMuzzlePosition();
        Vector2 velocity = direction.scale(GameConfig.PROJECTILE_SPEED);
        shootCooldown = GameConfig.SHOOT_COOLDOWN_SECONDS;
        return new Projectile(muzzle, rotation, velocity, GameConfig.PROJECTILE_RADIUS, GameConfig.PROJECTILE_LIFETIME_SECONDS);
    }

    protected void tickCooldown(double deltaTime) {
        shootCooldown -= deltaTime;
    }

    public void reset(Vector2 newPosition, double newRotation) {
        position = newPosition;
        rotation = newRotation;
        speed = baseMoveSpeed;
        shootCooldown = 0.0;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        speed = baseMoveSpeed * speedMultiplier;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public void update(double deltaTime) {
        tickCooldown(deltaTime);
    }

    @Override
    public void render(Graphics2D g2d) {
        int diameter = (int) Math.round(radius * 2.0);
        int x = (int) Math.round(position.x() - radius);
        int y = (int) Math.round(position.y() - radius);

        g2d.setColor(bodyColor);
        g2d.fillOval(x, y, diameter, diameter);

        Vector2 direction = Vector2.fromAngle(rotation);
        int x2 = (int) Math.round(position.x() + direction.x() * (radius + 8.0));
        int y2 = (int) Math.round(position.y() + direction.y() * (radius + 8.0));

        g2d.setStroke(new BasicStroke(4.0f));
        g2d.setColor(new Color(25, 25, 25));
        g2d.drawLine((int) Math.round(position.x()), (int) Math.round(position.y()), x2, y2);
    }

    public double getRadius() {
        return radius;
    }
}
