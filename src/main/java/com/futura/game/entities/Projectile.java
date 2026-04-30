package com.futura.game.entities;

import com.futura.game.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;

public class Projectile extends Entity {
    private final Vector2 velocity;
    private final double radius;
    private double lifeRemainingSeconds;

    public Projectile(Vector2 position, double rotation, Vector2 velocity, double radius, double lifeRemainingSeconds) {
        super(position, rotation);
        this.velocity = velocity;
        this.radius = radius;
        this.lifeRemainingSeconds = lifeRemainingSeconds;
    }

    @Override
    public void update(double deltaTime) {
        position = position.add(velocity.scale(deltaTime));
        lifeRemainingSeconds -= deltaTime;
    }

    @Override
    public void render(Graphics2D g2d) {
        int d = (int) Math.round(radius * 2.0);
        int x = (int) Math.round(position.x() - radius);
        int y = (int) Math.round(position.y() - radius);

        g2d.setColor(new Color(36, 36, 36));
        g2d.fillOval(x, y, d, d);

        g2d.setColor(new Color(232, 132, 70));
        g2d.fillOval(x + 1, y + 1, Math.max(1, d - 2), Math.max(1, d - 2));

        int core = Math.max(2, d / 2);
        int coreX = (int) Math.round(position.x() - core / 2.0);
        int coreY = (int) Math.round(position.y() - core / 2.0);
        g2d.setColor(new Color(255, 233, 150));
        g2d.fillOval(coreX, coreY, core, core);

        int shine = Math.max(2, d / 3);
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillOval(x + 1, y + 1, shine, shine);
    }

    public boolean isExpired() {
        return lifeRemainingSeconds <= 0.0;
    }

    public double getRadius() {
        return radius;
    }
}
