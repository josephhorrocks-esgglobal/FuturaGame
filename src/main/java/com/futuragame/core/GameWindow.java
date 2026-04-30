package com.futuragame.core;

import javax.swing.JFrame;

public class GameWindow {
    private final JFrame frame;
    private final GamePanel gamePanel;

    public GameWindow() {
        frame = new JFrame("FuturaGame");
        gamePanel = new GamePanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void showWindow() {
        frame.setVisible(true);
        gamePanel.start();
        gamePanel.requestFocusInWindow();
    }
}
