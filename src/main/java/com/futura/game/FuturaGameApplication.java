package com.futura.game;

import com.futura.game.core.GamePanel;
import com.futura.game.core.GameWindow;

import javax.swing.SwingUtilities;

public final class FuturaGameApplication {
    private FuturaGameApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GamePanel gamePanel = new GamePanel();
            GameWindow gameWindow = new GameWindow(gamePanel);
            gameWindow.setVisible(true);
            gamePanel.startGame();
        });
    }
}
