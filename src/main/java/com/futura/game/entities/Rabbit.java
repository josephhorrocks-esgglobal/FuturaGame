package com.futura.game.entities;

import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Rabbit extends Entity {
    private static final double RADIUS = 10.0;
    private static final double MIN_SPEED = 45.0;
    private static final double MAX_SPEED = 78.0;

    private final Random random;
    private final int arenaWidth;
    private final int arenaHeight;

    private Vector2 velocity;
    private double nibbleTimer;
    private double nextNibbleSwitchSeconds;
    private double grassPatchRadius;

    public Rabbit(int arenaWidth, int arenaHeight, ArenaMap map, Random random) {
        super(findSpawnPosition(arenaWidth, arenaHeight, map, random), 0.0);
        this.random = random;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.velocity = buildRandomVelocity(random);
        this.nibbleTimer = 0.0;
        this.nextNibbleSwitchSeconds = randomRange(0.9, 2.2);
        this.grassPatchRadius = randomRange(6.0, 10.0);
    }

    @Override
    public void update(double deltaTime) {
    }

    public void update(double deltaTime, ArenaMap map) {
        nibbleTimer += deltaTime;
        nextNibbleSwitchSeconds -= deltaTime;

        if (nextNibbleSwitchSeconds <= 0.0) {
            velocity = buildRandomVelocity(random);
            nextNibbleSwitchSeconds = randomRange(0.9, 2.2);
        }

        Vector2 next = position.add(velocity.scale(deltaTime));

        boolean bounced = false;
        if (next.x() - RADIUS < 0.0 || next.x() + RADIUS > arenaWidth) {
            velocity = new Vector2(-velocity.x(), velocity.y());
            bounced = true;
        }
        if (next.y() - RADIUS < 0.0 || next.y() + RADIUS > arenaHeight) {
            velocity = new Vector2(velocity.x(), -velocity.y());
            bounced = true;
        }

        if (!bounced && map.collidesWithObstacle(next.x(), next.y(), RADIUS)) {
            velocity = new Vector2(-velocity.x(), -velocity.y());
            bounced = true;
        }

        if (bounced) {
            next = position.add(velocity.scale(deltaTime));
        }

        position = next;

        if (nibbleTimer > 0.2) {
            nibbleTimer = 0.0;
            grassPatchRadius = Math.max(2.0, grassPatchRadius - 0.18);
            if (grassPatchRadius <= 2.2) {
                grassPatchRadius = randomRange(6.0, 10.0);
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        int grassR = (int) Math.round(grassPatchRadius);
        int gx = (int) Math.round(position.x() - 16);
        int gy = (int) Math.round(position.y() + 8);
        g2d.setColor(new Color(74, 162, 76, 140));
        g2d.fillOval(gx, gy, grassR * 3, grassR * 2);

        int bodyW = 19;
        int bodyH = 14;
        int bx = (int) Math.round(position.x() - bodyW / 2.0);
        int by = (int) Math.round(position.y() - bodyH / 2.0);

        g2d.setColor(new Color(220, 220, 220));
        g2d.fillOval(bx, by, bodyW, bodyH);

        int earPhase = (int) Math.floor((System.nanoTime() / 100_000_000L) % 2);
        int earLift = earPhase == 0 ? 0 : 2;
        g2d.fillRoundRect(bx + 2, by - 10 - earLift, 5, 11, 5, 5);
        g2d.fillRoundRect(bx + 10, by - 11 + earLift, 5, 12, 5, 5);

        g2d.setColor(new Color(255, 184, 194));
        g2d.fillRoundRect(bx + 3, by - 8 - earLift, 3, 8, 3, 3);
        g2d.fillRoundRect(bx + 11, by - 9 + earLift, 3, 8, 3, 3);

        g2d.setColor(new Color(245, 245, 245));
        g2d.fillOval(bx + 12, by + 3, 9, 8);

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillOval(bx + 13, by + 5, 2, 2);
        g2d.fillOval(bx + 17, by + 5, 2, 2);
        g2d.fillOval(bx + 15, by + 7, 2, 2);
    }

    private static Vector2 findSpawnPosition(int arenaWidth, int arenaHeight, ArenaMap map, Random random) {
        for (int i = 0; i < 80; i++) {
            double x = 26 + random.nextDouble() * (arenaWidth - 52);
            double y = 26 + random.nextDouble() * (arenaHeight - 52);
            if (!map.collidesWithObstacle(x, y, RADIUS)) {
                return new Vector2(x, y);
            }
        }
        return new Vector2(arenaWidth * 0.5, arenaHeight * 0.5);
    }

    private static Vector2 buildRandomVelocity(Random random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double speed = MIN_SPEED + random.nextDouble() * (MAX_SPEED - MIN_SPEED);
        return Vector2.fromAngle(angle).scale(speed);
    }

    private double randomRange(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }
}
