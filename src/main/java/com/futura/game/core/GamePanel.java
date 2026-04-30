package com.futura.game.core;

import com.futura.game.config.GameConfig;
import com.futura.game.entities.AITank;
import com.futura.game.entities.PlayerTank;
import com.futura.game.entities.Projectile;
import com.futura.game.entities.Tank;
import com.futura.game.input.InputHandler;
import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    private final InputHandler inputHandler;
    private final ArenaMap arenaMap;

    private final PlayerTank playerTank;
    private final AITank aiTank;
    private final List<Projectile> playerProjectiles;
    private final List<Projectile> aiProjectiles;

    private GameState gameState;
    private Thread gameThread;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setFocusable(true);

        this.inputHandler = new InputHandler();
        addKeyListener(inputHandler);

        this.arenaMap = new ArenaMap(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        this.playerTank = new PlayerTank(new Vector2(120, 120));
        this.aiTank = new AITank(new Vector2(840, 620));
        this.playerProjectiles = new ArrayList<>();
        this.aiProjectiles = new ArrayList<>();
        this.gameState = GameState.RUNNING;
    }

    public void startGame() {
        if (gameThread == null) {
            gameThread = new Thread(this, "game-loop");
            gameThread.start();
        }
    }

    @Override
    public void run() {
        long frameDurationNs = 1_000_000_000L / GameConfig.TARGET_FPS;
        long nextFrameTime = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();

            if (now >= nextFrameTime) {
                updateGame(GameConfig.DELTA_TIME);
                repaint();
                nextFrameTime += frameDurationNs;
            } else {
                long sleepMs = Math.max(1L, (nextFrameTime - now) / 1_000_000L);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void updateGame(double deltaTime) {
        if (gameState != GameState.RUNNING) {
            return;
        }

        Projectile playerShot = playerTank.updateAndTryShoot(deltaTime, inputHandler, arenaMap);
        if (playerShot != null) {
            playerProjectiles.add(playerShot);
        }

        Projectile aiShot = aiTank.updateAndTryShoot(deltaTime, playerTank, arenaMap);
        if (aiShot != null) {
            aiProjectiles.add(aiShot);
        }

        updateProjectiles(playerProjectiles, aiTank);
        updateProjectiles(aiProjectiles, playerTank);
    }

    private void updateProjectiles(List<Projectile> projectiles, Tank target) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update(GameConfig.DELTA_TIME);

            Vector2 pos = projectile.getPosition();
            if (projectile.isExpired()
                    || !arenaMap.isInsideBounds(pos.x(), pos.y(), projectile.getRadius())
                    || arenaMap.collidesWithObstacle(pos.x(), pos.y(), projectile.getRadius())) {
                iterator.remove();
                continue;
            }

            double hitDistance = projectile.getRadius() + target.getRadius();
            if (Vector2.distance(pos, target.getPosition()) <= hitDistance) {
                gameState = (target == playerTank) ? GameState.AI_WON : GameState.PLAYER_WON;
                iterator.remove();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        arenaMap.render(g2d);
        playerTank.render(g2d);
        aiTank.render(g2d);

        for (Projectile p : playerProjectiles) {
            p.render(g2d);
        }
        for (Projectile p : aiProjectiles) {
            p.render(g2d);
        }

        drawHud(g2d);
    }

    private void drawHud(Graphics2D g2d) {
        g2d.setColor(new Color(25, 25, 25, 170));
        g2d.fillRoundRect(14, 12, 320, 58, 12, 12);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("W/S: move  A/D: rotate  SPACE: shoot", 24, 35);

        if (gameState == GameState.PLAYER_WON) {
            drawCenterMessage(g2d, "Player Wins");
        } else if (gameState == GameState.AI_WON) {
            drawCenterMessage(g2d, "AI Wins");
        }
    }

    private void drawCenterMessage(Graphics2D g2d, String message) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(355, 318, 320, 120, 18, 18);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2d.drawString(message, 420, 388);
    }
}
