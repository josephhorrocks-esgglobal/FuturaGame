package com.futuragame.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Obstacle {
    private final Rectangle bounds;

    public Obstacle(int x, int y, int width, int height) {
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(95, 95, 95));
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
