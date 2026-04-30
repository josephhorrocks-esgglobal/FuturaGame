package com.futura.game.entities;

import com.futura.game.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Cloud extends Entity {
    private final int arenaWidth;
    private final int arenaHeight;
    private final Random random;
    private final double speed;
    private final int width;
    private final int height;
    private final double driftAmplitude;
    private final double driftSpeed;
    private final double baseY;
    private double driftTime;

    public Cloud(int arenaWidth, int arenaHeight, Random random) {
        super(new Vector2(-140 - random.nextInt(320), 60 + random.nextInt(Math.max(1, arenaHeight / 2))), 0.0);
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.random = random;
        this.speed = 10.0 + random.nextDouble() * 12.0;
        this.width = 92 + random.nextInt(42);
        this.height = 34 + random.nextInt(18);
        this.driftAmplitude = 5.0 + random.nextDouble() * 8.0;
        this.driftSpeed = 0.3 + random.nextDouble() * 0.5;
        this.baseY = position.y();
        this.driftTime = random.nextDouble() * Math.PI * 2.0;
    }

    @Override
    public void update(double deltaTime) {
        driftTime += deltaTime * driftSpeed;
        double nextX = position.x() + speed * deltaTime;
        double nextY = baseY + Math.sin(driftTime) * driftAmplitude;

        if (nextX - width / 2.0 > arenaWidth + 40) {
            nextX = -width - random.nextInt(180);
            nextY = 40 + random.nextInt(Math.max(1, arenaHeight / 2));
        }

        position = new Vector2(nextX, nextY);
    }

    @Override
    public void render(Graphics2D g2d) {
        int x = (int) Math.round(position.x());
        int y = (int) Math.round(position.y());

        g2d.setColor(new Color(255, 255, 255, 175));
        g2d.fillOval(x - width / 2, y - height / 2, width / 2, height);
        g2d.fillOval(x - width / 5, y - height / 2 - 8, width / 2, height + 6);
        g2d.fillOval(x + width / 8, y - height / 2, width / 2, height);

        g2d.setColor(new Color(210, 220, 228, 160));
        g2d.drawOval(x - width / 2, y - height / 2, width / 2, height);
        g2d.drawOval(x - width / 5, y - height / 2 - 8, width / 2, height + 6);
        g2d.drawOval(x + width / 8, y - height / 2, width / 2, height);
    }
}
