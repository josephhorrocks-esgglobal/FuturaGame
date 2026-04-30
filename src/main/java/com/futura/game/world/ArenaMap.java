package com.futura.game.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class ArenaMap {
    private final int width;
    private final int height;
    private final List<Rectangle> obstacles;

    public ArenaMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.obstacles = buildDefaultObstacles();
    }

    private List<Rectangle> buildDefaultObstacles() {
        List<Rectangle> walls = new ArrayList<>();
        walls.add(new Rectangle(220, 150, 65, 280));
        walls.add(new Rectangle(480, 330, 120, 50));
        walls.add(new Rectangle(700, 120, 55, 250));
        walls.add(new Rectangle(360, 560, 320, 35));
        return walls;
    }

    public boolean isInsideBounds(double x, double y, double radius) {
        return x - radius >= 0
                && y - radius >= 0
                && x + radius <= width
                && y + radius <= height;
    }

    public boolean collidesWithObstacle(double x, double y, double radius) {
        int left = (int) Math.round(x - radius);
        int top = (int) Math.round(y - radius);
        int size = (int) Math.round(radius * 2.0);
        Rectangle hitBox = new Rectangle(left, top, size, size);

        for (Rectangle obstacle : obstacles) {
            if (obstacle.intersects(hitBox)) {
                return true;
            }
        }
        return false;
    }

    public void render(Graphics2D g2d) {
        g2d.setColor(new Color(181, 205, 154));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(92, 114, 74));
        for (Rectangle obstacle : obstacles) {
            g2d.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
    }

    public List<Rectangle> getObstacles() {
        return List.copyOf(obstacles);
    }
}
