package com.futura.game.entities;

import com.futura.game.math.Vector2;

import java.awt.Graphics2D;

public abstract class Entity {
    protected Vector2 position;
    protected double rotation;

    protected Entity(Vector2 position, double rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public abstract void update(double deltaTime);

    public abstract void render(Graphics2D g2d);

    public Vector2 getPosition() {
        return position;
    }

    public double getRotation() {
        return rotation;
    }
}
