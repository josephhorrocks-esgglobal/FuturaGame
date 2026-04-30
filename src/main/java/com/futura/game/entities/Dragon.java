package com.futura.game.entities;

import com.futura.game.config.GameConfig;
import com.futura.game.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dragon {
    private enum State { INACTIVE, FLYING }

    private State state;
    private Vector2 position;
    private Vector2 velocity;
    private double spawnTimer;
    private boolean hasFired;
    private double fireBreathTimer;
    private final Random random;
    private final int arenaWidth;
    private final int arenaHeight;

    public Dragon(int arenaWidth, int arenaHeight) {
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.random = new Random();
        this.state = State.INACTIVE;
        this.spawnTimer = randomSpawnDelay();
        this.hasFired = false;
        this.fireBreathTimer = 0.0;
    }

    public void update(double deltaTime) {
        if (fireBreathTimer > 0.0) {
            fireBreathTimer -= deltaTime;
        }

        if (state == State.INACTIVE) {
            spawnTimer -= deltaTime;
            if (spawnTimer <= 0.0) {
                spawn();
            }
            return;
        }

        position = position.add(velocity.scale(deltaTime));

        double exitMargin = 130.0;
        if (position.x() < -exitMargin || position.x() > arenaWidth + exitMargin
                || position.y() < -exitMargin || position.y() > arenaHeight + exitMargin) {
            state = State.INACTIVE;
            spawnTimer = randomSpawnDelay();
            hasFired = false;
        }
    }

    private void spawn() {
        int edge = random.nextInt(4);
        double buf = 150.0;
        double enterX, enterY, exitX, exitY;

        switch (edge) {
            case 0 -> {
                enterX = -65;
                enterY = buf + random.nextDouble() * (arenaHeight - buf * 2);
                exitX = arenaWidth + 65;
                exitY = buf + random.nextDouble() * (arenaHeight - buf * 2);
            }
            case 1 -> {
                enterX = arenaWidth + 65;
                enterY = buf + random.nextDouble() * (arenaHeight - buf * 2);
                exitX = -65;
                exitY = buf + random.nextDouble() * (arenaHeight - buf * 2);
            }
            case 2 -> {
                enterX = buf + random.nextDouble() * (arenaWidth - buf * 2);
                enterY = -65;
                exitX = buf + random.nextDouble() * (arenaWidth - buf * 2);
                exitY = arenaHeight + 65;
            }
            default -> {
                enterX = buf + random.nextDouble() * (arenaWidth - buf * 2);
                enterY = arenaHeight + 65;
                exitX = buf + random.nextDouble() * (arenaWidth - buf * 2);
                exitY = -65;
            }
        }

        position = new Vector2(enterX, enterY);
        Vector2 dir = new Vector2(exitX - enterX, exitY - enterY).normalize();
        velocity = dir.scale(GameConfig.DRAGON_SPEED);
        state = State.FLYING;
        hasFired = false;
    }

    public List<DragonFireball> tryFire(Vector2 tank1Pos, Vector2 tank2Pos) {
        if (state != State.FLYING || hasFired) {
            return List.of();
        }

        boolean t1Safe = isInCorner(tank1Pos);
        boolean t2Safe = isInCorner(tank2Pos);

        double d1 = t1Safe ? Double.MAX_VALUE : Vector2.distance(position, tank1Pos);
        double d2 = t2Safe ? Double.MAX_VALUE : Vector2.distance(position, tank2Pos);
        double nearestDist = Math.min(d1, d2);

        if (nearestDist > GameConfig.DRAGON_FIRE_RANGE) {
            return List.of();
        }

        Vector2 target = (d1 <= d2) ? tank1Pos : tank2Pos;
        hasFired = true;
        fireBreathTimer = 0.55;
        return buildFireballBurst(target);
    }

    private List<DragonFireball> buildFireballBurst(Vector2 target) {
        List<DragonFireball> bombs = new ArrayList<>();
        double scatter = GameConfig.DRAGON_BOMB_SCATTER;

        bombs.add(new DragonFireball(target));
        bombs.add(new DragonFireball(new Vector2(
                target.x() + (random.nextDouble() * 2 - 1) * scatter,
                target.y() + (random.nextDouble() * 2 - 1) * scatter)));
        bombs.add(new DragonFireball(new Vector2(
                target.x() + (random.nextDouble() * 2 - 1) * scatter,
                target.y() + (random.nextDouble() * 2 - 1) * scatter)));
        return bombs;
    }

    public boolean isInCorner(Vector2 pos) {
        double s = GameConfig.DRAGON_CORNER_SAFE_SIZE;
        return (pos.x() < s && pos.y() < s)
                || (pos.x() > arenaWidth - s && pos.y() < s)
                || (pos.x() < s && pos.y() > arenaHeight - s)
                || (pos.x() > arenaWidth - s && pos.y() > arenaHeight - s);
    }

    public void render(Graphics2D g2d) {
        if (state != State.FLYING) {
            return;
        }

        double angle = Math.atan2(velocity.y(), velocity.x());
        Graphics2D dg = (Graphics2D) g2d.create();
        dg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        dg.translate((int) Math.round(position.x()), (int) Math.round(position.y()));
        dg.rotate(angle);

        // Drop shadow
        dg.setColor(new Color(0, 0, 0, 55));
        dg.fillOval(-28, -6, 56, 18);

        // Wings (bat-style, folded back from body centre)
        dg.setColor(new Color(105, 30, 0, 215));
        dg.fillPolygon(
                new int[]{8, -12, -38, -14},
                new int[]{-8, -44, -20, -8},
                4);
        dg.fillPolygon(
                new int[]{8, -12, -38, -14},
                new int[]{8, 44, 20, 8},
                4);

        // Wing edge highlight
        dg.setColor(new Color(160, 55, 0, 130));
        dg.drawPolygon(
                new int[]{8, -12, -38, -14},
                new int[]{-8, -44, -20, -8},
                4);
        dg.drawPolygon(
                new int[]{8, -12, -38, -14},
                new int[]{8, 44, 20, 8},
                4);

        // Body
        dg.setColor(new Color(38, 108, 28));
        dg.fillRoundRect(-28, -10, 54, 20, 12, 12);

        // Head
        dg.setColor(new Color(50, 128, 35));
        dg.fillOval(20, -10, 22, 20);

        // Eye
        dg.setColor(new Color(250, 215, 0));
        dg.fillOval(30, -5, 8, 8);
        dg.setColor(Color.BLACK);
        dg.fillOval(32, -3, 5, 5);

        // Tail
        dg.setColor(new Color(30, 90, 22));
        dg.fillPolygon(
                new int[]{-22, -22, -44},
                new int[]{-6, 6, 0},
                3);

        // Fire breath glow when actively breathing
        if (fireBreathTimer > 0.0) {
            dg.setColor(new Color(255, 130, 0, 190));
            dg.fillOval(36, -7, 22, 14);
            dg.setColor(new Color(255, 230, 80, 160));
            dg.fillOval(42, -4, 12, 8);
        }

        dg.dispose();
    }

    public boolean isFlying() {
        return state == State.FLYING;
    }

    public double getSpawnTimerRemaining() {
        return Math.max(0.0, spawnTimer);
    }

    public void reset() {
        state = State.INACTIVE;
        spawnTimer = randomSpawnDelay();
        hasFired = false;
        fireBreathTimer = 0.0;
    }

    private double randomSpawnDelay() {
        double min = GameConfig.DRAGON_MIN_SPAWN_SECONDS;
        double max = GameConfig.DRAGON_MAX_SPAWN_SECONDS;
        return min + random.nextDouble() * (max - min);
    }
}
