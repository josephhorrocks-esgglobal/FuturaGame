package com.futura.game.core;

import com.futura.game.config.GameConfig;

import javax.swing.JFrame;

public class GameWindow extends JFrame {
    public GameWindow(GamePanel gamePanel) {
        super(GameConfig.WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);
    }
}
