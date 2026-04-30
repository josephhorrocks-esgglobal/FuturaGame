package com.futura.game.core;

import com.futura.game.config.GameConfig;
import com.futura.game.entities.AITank;
import com.futura.game.entities.Dragon;
import com.futura.game.entities.DragonFireball;
import com.futura.game.entities.PlayerTank;
import com.futura.game.entities.Projectile;
import com.futura.game.entities.Tank;
import com.futura.game.input.InputHandler;
import com.futura.game.math.Vector2;
import com.futura.game.world.ArenaMap;
import com.futura.game.world.MapType;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.futura.game.network.NetworkManager;
import com.futura.game.network.NetworkMessage;

public class GamePanel extends JPanel implements Runnable {
    private static final Vector2 PLAYER_SPAWN = new Vector2(120, 120);
    private static final Vector2 AI_SPAWN = new Vector2(840, 620);
    private static final double PLAYER_SPAWN_ROTATION = -Math.PI / 2.0;
    private static final double AI_SPAWN_ROTATION = Math.PI / 2.0;

    private final InputHandler inputHandler;
    private final ArenaMap arenaMap;

    private final PlayerTank playerTank;
    private final AITank aiTank;
    private final List<Projectile> playerProjectiles;
    private final List<Projectile> aiProjectiles;

    private final Dragon dragon;
    private final List<DragonFireball> dragonFireballs;

    private final NetworkManager networkManager;

    private GameState gameState;
    private Thread gameThread;
    private boolean restartKeyWasDown;

    public GamePanel(MapType mapType, NetworkManager networkManager) {
        setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setFocusable(true);

        this.inputHandler = new InputHandler();
        addKeyListener(inputHandler);

        this.arenaMap = new ArenaMap(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT, mapType);
        this.playerTank = new PlayerTank(PLAYER_SPAWN);
        this.aiTank = new AITank(AI_SPAWN);
        this.playerProjectiles = new ArrayList<>();
        this.aiProjectiles = new ArrayList<>();
        this.dragon = new Dragon(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        this.dragonFireballs = new ArrayList<>();
        this.networkManager = networkManager;
        this.gameState = GameState.RUNNING;
        this.restartKeyWasDown = false;
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
            handleRestartInput();
            return;
        }

        if (networkManager != null) {
            updateMultiplayer(deltaTime);
        } else {
            updateSinglePlayer(deltaTime);
        }
    }

    private void updateSinglePlayer(double deltaTime) {
        playerTank.setSpeedMultiplier(arenaMap.isInsideSlowZone(playerTank.getPosition())
                ? GameConfig.SLOW_MULTIPLIER : 1.0);
        aiTank.setSpeedMultiplier(arenaMap.isInsideSlowZone(aiTank.getPosition())
                ? GameConfig.SLOW_MULTIPLIER : 1.0);

        Projectile playerShot = playerTank.updateAndTryShoot(deltaTime, inputHandler, arenaMap, aiTank);
        if (playerShot != null) {
            playerProjectiles.add(playerShot);
        }

        Projectile aiShot = aiTank.updateAndTryShoot(deltaTime, playerTank, arenaMap);
        if (aiShot != null) {
            aiProjectiles.add(aiShot);
        }

        updateProjectiles(playerProjectiles, aiTank);
        updateProjectiles(aiProjectiles, playerTank);

        dragon.update(deltaTime);
        dragonFireballs.addAll(dragon.tryFire(playerTank.getPosition(), aiTank.getPosition()));
        updateDragonFireballs();
    }

    private void updateMultiplayer(double deltaTime) {
        for (NetworkMessage msg : networkManager.pollMessages()) {
            handleNetworkMessage(msg);
        }
        if (gameState != GameState.RUNNING) {
            return;
        }

        playerTank.setSpeedMultiplier(arenaMap.isInsideSlowZone(playerTank.getPosition())
                ? GameConfig.SLOW_MULTIPLIER : 1.0);

        Projectile playerShot = playerTank.updateAndTryShoot(deltaTime, inputHandler, arenaMap, aiTank);
        if (playerShot != null) {
            playerProjectiles.add(playerShot);
            networkManager.sendShot(
                    playerShot.getPosition().x(),
                    playerShot.getPosition().y(),
                    playerShot.getRotation());
        }

        updatePlayerProjectilesNetwork();
        updateRemoteProjectilesCosmetic();

        dragon.update(deltaTime);
        dragonFireballs.addAll(dragon.tryFire(playerTank.getPosition(), aiTank.getPosition()));
        updateDragonFireballsMultiplayer();

        networkManager.sendPosition(
                playerTank.getPosition().x(),
                playerTank.getPosition().y(),
                playerTank.getRotation());
    }

    private void handleNetworkMessage(NetworkMessage msg) {
        switch (msg.type()) {
            case POS -> aiTank.applyNetworkState(msg.data()[0], msg.data()[1], msg.data()[2]);
            case SHOT -> {
                Vector2 muzzle = new Vector2(msg.data()[0], msg.data()[1]);
                double rot = msg.data()[2];
                Vector2 vel = Vector2.fromAngle(rot).scale(GameConfig.PROJECTILE_SPEED);
                aiProjectiles.add(new Projectile(muzzle, rot, vel, 4.0, GameConfig.PROJECTILE_LIFETIME_SECONDS));
            }
            case HIT -> {
                boolean destroyed = playerTank.applyHit();
                networkManager.sendHealth(playerTank.getHealth());
                if (destroyed) {
                    networkManager.sendDead();
                    gameState = GameState.AI_WON;
                }
            }
            case HEALTH -> aiTank.setHealth((int) msg.data()[0]);
            case DEAD -> gameState = GameState.PLAYER_WON;
            case RESTART -> resetRound(false);
            default -> {}
        }
    }

    private void updatePlayerProjectilesNetwork() {
        Iterator<Projectile> iterator = playerProjectiles.iterator();
        while (iterator.hasNext()) {
            Projectile proj = iterator.next();
            proj.update(GameConfig.DELTA_TIME);

            Vector2 pos = proj.getPosition();
            if (proj.isExpired()
                    || !arenaMap.isInsideBounds(pos.x(), pos.y(), proj.getRadius())
                    || arenaMap.collidesWithObstacle(pos.x(), pos.y(), proj.getRadius())) {
                iterator.remove();
                continue;
            }

            if (Vector2.distance(pos, aiTank.getPosition()) <= proj.getRadius() + aiTank.getRadius()) {
                networkManager.sendHit();
                iterator.remove();
            }
        }
    }

    private void updateRemoteProjectilesCosmetic() {
        Iterator<Projectile> iterator = aiProjectiles.iterator();
        while (iterator.hasNext()) {
            Projectile proj = iterator.next();
            proj.update(GameConfig.DELTA_TIME);
            Vector2 pos = proj.getPosition();
            if (proj.isExpired()
                    || !arenaMap.isInsideBounds(pos.x(), pos.y(), proj.getRadius())
                    || arenaMap.collidesWithObstacle(pos.x(), pos.y(), proj.getRadius())) {
                iterator.remove();
            }
        }
    }

    private void updateDragonFireballsMultiplayer() {
        Iterator<DragonFireball> it = dragonFireballs.iterator();
        while (it.hasNext()) {
            DragonFireball bomb = it.next();
            bomb.update(GameConfig.DELTA_TIME);
            if (bomb.isDone()) {
                it.remove();
                continue;
            }
            if (bomb.shouldApplyDamage()) {
                bomb.markDamageApplied();
                Vector2 blast = bomb.getTargetPosition();
                double radius = bomb.getExplosionRadius();
                if (Vector2.distance(blast, playerTank.getPosition()) <= radius + playerTank.getRadius()) {
                    boolean destroyed = playerTank.applyHit();
                    networkManager.sendHealth(playerTank.getHealth());
                    if (destroyed) {
                        networkManager.sendDead();
                        gameState = GameState.AI_WON;
                    }
                }
            }
        }
    }

    private void updateDragonFireballs() {
        Iterator<DragonFireball> it = dragonFireballs.iterator();
        while (it.hasNext()) {
            DragonFireball bomb = it.next();
            bomb.update(GameConfig.DELTA_TIME);

            if (bomb.isDone()) {
                it.remove();
                continue;
            }

            if (bomb.shouldApplyDamage()) {
                bomb.markDamageApplied();
                applyBombDamage(bomb);
            }
        }
    }

    private void applyBombDamage(DragonFireball bomb) {
        Vector2 blast = bomb.getTargetPosition();
        double radius = bomb.getExplosionRadius();

        if (Vector2.distance(blast, playerTank.getPosition()) <= radius + playerTank.getRadius()) {
            boolean destroyed = playerTank.applyHit();
            if (destroyed) {
                gameState = GameState.AI_WON;
            }
        }

        if (Vector2.distance(blast, aiTank.getPosition()) <= radius + aiTank.getRadius()) {
            boolean destroyed = aiTank.applyHit();
            if (destroyed) {
                gameState = GameState.PLAYER_WON;
            }
        }
    }

    private void handleRestartInput() {
        boolean restartDown = inputHandler.isKeyDown(KeyEvent.VK_R);
        if (restartDown && !restartKeyWasDown) {
            resetRound();
        }
        restartKeyWasDown = restartDown;
    }

    private void resetRound() {
        resetRound(true);
    }

    private void resetRound(boolean sendNetworkMessage) {
        if (sendNetworkMessage && networkManager != null) {
            networkManager.sendRestart();
        }
        playerTank.reset(PLAYER_SPAWN, PLAYER_SPAWN_ROTATION);
        aiTank.reset(AI_SPAWN, AI_SPAWN_ROTATION);
        playerProjectiles.clear();
        aiProjectiles.clear();
        dragon.reset();
        dragonFireballs.clear();
        gameState = GameState.RUNNING;
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
                boolean targetDestroyed = target.applyHit();
                if (targetDestroyed) {
                    gameState = (target == playerTank) ? GameState.AI_WON : GameState.PLAYER_WON;
                }
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
        drawCornerZones(g2d);
        playerTank.render(g2d);
        aiTank.render(g2d);

        for (Projectile p : playerProjectiles) {
            p.render(g2d);
        }
        for (Projectile p : aiProjectiles) {
            p.render(g2d);
        }
        for (DragonFireball fb : dragonFireballs) {
            fb.render(g2d);
        }
        dragon.render(g2d);

        drawHud(g2d);
        drawDragonHud(g2d);
        if (dragon.isFlying()) {
            drawDragonWarning(g2d);
        }
    }

    private void drawHud(Graphics2D g2d) {
        boolean playerSlowed = arenaMap.isInsideSlowZone(playerTank.getPosition());
        boolean aiSlowed = arenaMap.isInsideSlowZone(aiTank.getPosition());

        g2d.setColor(new Color(25, 25, 25, 170));
        g2d.fillRoundRect(14, 12, 420, networkManager != null ? 196 : 172, 12, 12);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("W/S: move  A/D: rotate  SPACE: shoot", 24, 35);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2d.drawString("Player slowed: " + (playerSlowed ? "YES" : "NO"), 24, 58);
        g2d.drawString("AI slowed: " + (aiSlowed ? "YES" : "NO"), 24, 78);

        g2d.drawString("Static slow patches: " + arenaMap.getSlowPatchCount(), 24, 98);
        g2d.drawString("Map: " + arenaMap.getMapName(), 24, 118);

        drawHealthBar(g2d, 24, 132, "Player HP", playerTank.getHealth(), playerTank.getMaxHealth(), new Color(56, 120, 210));
        String opponentLabel = networkManager != null ? "Opponent HP" : "AI HP";
        drawHealthBar(g2d, 224, 132, opponentLabel, aiTank.getHealth(), aiTank.getMaxHealth(), new Color(212, 84, 66));

        if (networkManager != null) {
            boolean connected = networkManager.isConnected();
            g2d.setColor(connected ? new Color(80, 210, 80) : new Color(220, 70, 70));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2d.drawString("Multiplayer: " + (connected ? "Connected" : "Disconnected"), 24, 168);
            g2d.setColor(new Color(180, 180, 180));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("You = Blue   Opponent = Red", 24, 185);
        }

        if (gameState == GameState.PLAYER_WON) {
            drawCenterMessage(g2d, "Player Wins");
        } else if (gameState == GameState.AI_WON) {
            drawCenterMessage(g2d, "AI Wins");
        }
    }

    private void drawCornerZones(Graphics2D g2d) {
        int s = (int) Math.round(GameConfig.DRAGON_CORNER_SAFE_SIZE);
        int w = GameConfig.WINDOW_WIDTH;
        int h = GameConfig.WINDOW_HEIGHT;

        g2d.setColor(new Color(80, 200, 80, 35));
        g2d.fillRect(0, 0, s, s);
        g2d.fillRect(w - s, 0, s, s);
        g2d.fillRect(0, h - s, s, s);
        g2d.fillRect(w - s, h - s, s, s);

        g2d.setColor(new Color(50, 170, 50, 110));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                10.0f, new float[]{5, 4}, 0));
        g2d.drawRect(0, 0, s, s);
        g2d.drawRect(w - s, 0, s, s);
        g2d.drawRect(0, h - s, s, s);
        g2d.drawRect(w - s, h - s, s, s);
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawDragonHud(Graphics2D g2d) {
        int boxX = GameConfig.WINDOW_WIDTH - 268;
        g2d.setColor(new Color(25, 25, 25, 170));
        g2d.fillRoundRect(boxX, 12, 254, 54, 12, 12);

        if (dragon.isFlying()) {
            g2d.setColor(new Color(255, 100, 0));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2d.drawString("DRAGON INCOMING!", boxX + 12, 36);
            g2d.setColor(new Color(200, 200, 200));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("Hide in the green corners!", boxX + 12, 55);
        } else {
            double remaining = dragon.getSpawnTimerRemaining();
            String timeStr = remaining >= 60
                    ? String.format("%.0fm %.0fs", Math.floor(remaining / 60), remaining % 60)
                    : String.format("%.0fs", remaining);
            g2d.setColor(new Color(200, 200, 200));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("Dragon attack in: " + timeStr, boxX + 12, 36);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("Corners are dragon-free zones", boxX + 12, 55);
        }
    }

    private void drawDragonWarning(Graphics2D g2d) {
        int w = GameConfig.WINDOW_WIDTH;
        g2d.setColor(new Color(180, 40, 0, 190));
        g2d.fillRoundRect(w / 2 - 175, 6, 350, 36, 10, 10);
        g2d.setColor(new Color(255, 210, 50));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("DRAGON - HIDE IN THE CORNERS!", w / 2 - 165, 31);
    }

    private void drawHealthBar(Graphics2D g2d,
                               int x,
                               int y,
                               String label,
                               int health,
                               int maxHealth,
                               Color fillColor) {
        int width = 170;
        int height = 16;

        g2d.setColor(Color.WHITE);
        g2d.drawString(label + ": " + health + "/" + maxHealth, x, y - 5);

        g2d.setColor(new Color(70, 70, 70));
        g2d.fillRoundRect(x, y, width, height, 8, 8);

        int filled = (int) Math.round((health / (double) maxHealth) * width);
        g2d.setColor(fillColor);
        g2d.fillRoundRect(x, y, filled, height, 8, 8);

        g2d.setColor(new Color(20, 20, 20));
        g2d.drawRoundRect(x, y, width, height, 8, 8);
    }

    private void drawCenterMessage(Graphics2D g2d, String message) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(355, 318, 320, 120, 18, 18);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2d.drawString(message, 420, 388);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.drawString("Press R to restart", 430, 418);
    }
}
