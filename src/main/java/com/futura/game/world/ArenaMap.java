package com.futura.game.world;

import com.futura.game.config.GameConfig;
import com.futura.game.math.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArenaMap {
    private static final Vector2 PLAYER_SPAWN = new Vector2(120, 120);
    private static final Vector2 AI_SPAWN = new Vector2(840, 620);

    private final int width;
    private final int height;
    private final MapType mapType;
    private final List<Rectangle> obstacles;
    private final List<Rectangle> lakes;
    private final Random random;
    private final List<Vector2> slowPatches;

    public ArenaMap(int width, int height, MapType mapType) {
        this.width = width;
        this.height = height;
        this.mapType = mapType;
        this.random = new Random();
        this.obstacles = new ArrayList<>();
        this.lakes = new ArrayList<>();
        buildMapGeometry();
        this.slowPatches = buildSlowPatches();
    }

    private void buildMapGeometry() {
        if (mapType == MapType.CITY_MAP) {
            buildCityMapGeometry();
            return;
        }

        obstacles.add(new Rectangle(220, 150, 65, 280));
        obstacles.add(new Rectangle(480, 330, 120, 50));
        obstacles.add(new Rectangle(700, 120, 55, 250));
        obstacles.add(new Rectangle(360, 560, 320, 35));
    }

    private void buildCityMapGeometry() {
        for (int i = 0; i < GameConfig.CITY_MAP_BUILDING_COUNT; i++) {
            Rectangle building = findValidRect(48, 120, 48, 120, obstacles, 80.0);
            if (building != null) {
                obstacles.add(building);
            }
        }

        for (int i = 0; i < GameConfig.CITY_MAP_LAKE_COUNT; i++) {
            Rectangle lake = findValidRect(60, 140, 60, 140, lakes, 95.0);
            if (lake != null) {
                lakes.add(lake);
            }
        }
    }

    private Rectangle findValidRect(int minWidth,
                                    int maxWidth,
                                    int minHeight,
                                    int maxHeight,
                                    List<Rectangle> ownCollection,
                                    double spawnClearance) {
        for (int i = 0; i < 80; i++) {
            int rectWidth = minWidth + random.nextInt(maxWidth - minWidth + 1);
            int rectHeight = minHeight + random.nextInt(maxHeight - minHeight + 1);
            int x = random.nextInt(Math.max(1, width - rectWidth));
            int y = random.nextInt(Math.max(1, height - rectHeight));

            Rectangle candidate = new Rectangle(x, y, rectWidth, rectHeight);
            if (intersectsAny(candidate, obstacles, 16)
                    || intersectsAny(candidate, lakes, 16)
                    || intersectsAny(candidate, ownCollection, 16)
                    || isNearSpawn(candidate, PLAYER_SPAWN, spawnClearance)
                    || isNearSpawn(candidate, AI_SPAWN, spawnClearance)) {
                continue;
            }
            return candidate;
        }

        return null;
    }

    private boolean intersectsAny(Rectangle candidate, List<Rectangle> others, int padding) {
        Rectangle padded = new Rectangle(
                candidate.x - padding,
                candidate.y - padding,
                candidate.width + (padding * 2),
                candidate.height + (padding * 2));
        for (Rectangle other : others) {
            if (padded.intersects(other)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNearSpawn(Rectangle area, Vector2 spawn, double minDistance) {
        double centerX = area.getCenterX();
        double centerY = area.getCenterY();
        return Vector2.distance(new Vector2(centerX, centerY), spawn) < minDistance;
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

        for (Rectangle lake : lakes) {
            if (lake.intersects(hitBox)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInsideSlowZone(Vector2 point) {
        for (Vector2 slowPatch : slowPatches) {
            if (Vector2.distance(point, slowPatch) <= GameConfig.SLOW_ZONE_RADIUS) {
                return true;
            }
        }
        return false;
    }

    public int getSlowPatchCount() {
        return slowPatches.size();
    }

    private List<Vector2> buildSlowPatches() {
        List<Vector2> patches = new ArrayList<>();
        for (int i = 0; i < GameConfig.SLOW_PATCH_COUNT; i++) {
            Vector2 candidate = findValidSlowPatchCenter(patches);
            if (candidate != null) {
                patches.add(candidate);
            }
        }
        return patches;
    }

    private Vector2 findValidSlowPatchCenter(List<Vector2> existingPatches) {
        double radius = GameConfig.SLOW_ZONE_RADIUS;
        double minX = radius;
        double maxX = width - radius;
        double minY = radius;
        double maxY = height - radius;

        for (int i = 0; i < 20; i++) {
            double x = minX + random.nextDouble() * (maxX - minX);
            double y = minY + random.nextDouble() * (maxY - minY);
            if (collidesWithObstacle(x, y, radius)) {
                continue;
            }

            Vector2 candidate = new Vector2(x, y);
            if (isFarEnoughFromExistingPatches(candidate, existingPatches, radius * 1.35)) {
                return new Vector2(x, y);
            }
        }
        return null;
    }

    private boolean isFarEnoughFromExistingPatches(Vector2 candidate, List<Vector2> existingPatches, double minDistance) {
        for (Vector2 patch : existingPatches) {
            if (Vector2.distance(candidate, patch) < minDistance) {
                return false;
            }
        }
        return true;
    }

    public void render(Graphics2D g2d) {
        g2d.setColor(new Color(181, 205, 154));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(92, 114, 74));
        for (Rectangle obstacle : obstacles) {
            g2d.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        g2d.setColor(new Color(72, 132, 197));
        for (Rectangle lake : lakes) {
            g2d.fillOval(lake.x, lake.y, lake.width, lake.height);
        }

        g2d.setColor(new Color(40, 95, 152));
        for (Rectangle lake : lakes) {
            g2d.drawOval(lake.x, lake.y, lake.width, lake.height);
        }

        for (Vector2 slowPatch : slowPatches) {
            int radius = (int) Math.round(GameConfig.SLOW_ZONE_RADIUS);
            int diameter = radius * 2;
            int x = (int) Math.round(slowPatch.x()) - radius;
            int y = (int) Math.round(slowPatch.y()) - radius;

            g2d.setColor(new Color(58, 112, 194, 70));
            g2d.fillOval(x, y, diameter, diameter);

            g2d.setColor(new Color(41, 86, 161));
            g2d.drawOval(x, y, diameter, diameter);
        }
    }

    public List<Rectangle> getObstacles() {
        return List.copyOf(obstacles);
    }

    public String getMapName() {
        return mapType.getDisplayName();
    }
}
