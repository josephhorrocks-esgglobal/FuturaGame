package com.futuragame.entities;

import com.futuragame.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Tank {
    private final Color color;
    private final double radius;
    private Vector2 position;
    private double rotationRadians;

    public Tank(Vector2 position, Color color) {
        this.position = position;
        this.color = color;
        this.radius = 20.0;
    }

    public void rotate(double deltaRadians) {
        rotationRadians += deltaRadians;
    }

    public void move(double distance) {
        Vector2 direction = Vector2.fromAngle(rotationRadians).scale(distance);
        position.add(direction);
    }

    public Projectile fire(double speed) {
        Vector2 facing = Vector2.fromAngle(rotationRadians).normalize();
        Vector2 spawnPosition = position.copy().add(facing.copy().scale(radius + 8.0));
        Vector2 velocity = facing.copy().scale(speed);
        return new Projectile(spawnPosition, velocity, 4.0, color);
    }

    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();

        g2d.translate(position.x, position.y);
        g2d.rotate(rotationRadians);

        g2d.setColor(color);
        g2d.fillRoundRect(-(int) radius, -(int) radius + 2, (int) radius * 2, (int) radius * 2 - 4, 8, 8);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int) (radius * 0.2), -4, (int) radius + 8, 8);

        g2d.setTransform(oldTransform);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public double getRotationRadians() {
        return rotationRadians;
    }

    public double getRadius() {
        return radius;
    }
}
