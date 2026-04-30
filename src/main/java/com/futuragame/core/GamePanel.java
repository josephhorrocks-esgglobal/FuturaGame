package com.futuragame.core;

import com.futuragame.input.InputHandler;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GamePanel extends JPanel implements Runnable {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 480;

    private final GameState gameState = new GameState();
    private final InputHandler inputHandler = new InputHandler();
    private Thread gameThread;
    private boolean running;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(inputHandler);
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        gameThread = new Thread(this, "game-loop");
        gameThread.start();
    }

    @Override
    public void run() {
        final double targetDelta = 1.0 / 60.0;
        long lastTimeNanos = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            double deltaSeconds = (now - lastTimeNanos) / 1_000_000_000.0;
            lastTimeNanos = now;

            gameState.update(deltaSeconds, inputHandler.getState(), getWidth(), getHeight());
            repaint();

            long sleepMillis = Math.max(1L, (long) ((targetDelta - deltaSeconds) * 1000));
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gameState.draw(g2d, getWidth(), getHeight());
    }
}
