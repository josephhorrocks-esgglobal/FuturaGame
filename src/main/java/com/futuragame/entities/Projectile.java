package com.futuragame.entities;

import com.futuragame.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;

public class Projectile {
    private Vector2 position;
    private final Vector2 velocity;
    private final double radius;
    private final Color color;
    private boolean active = true;

    public Projectile(Vector2 position, Vector2 velocity, double radius, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
        this.color = color;
    }

    public void update(double deltaTime) {
        if (!active) {
            return;
        }
        position.add(velocity.copy().scale(deltaTime));
    }

    public void draw(Graphics2D g2d) {
        if (!active) {
            return;
        }
        g2d.setColor(color);
        g2d.fillOval((int) (position.x - radius), (int) (position.y - radius), (int) (radius * 2), (int) (radius * 2));
    }

    public Vector2 getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
