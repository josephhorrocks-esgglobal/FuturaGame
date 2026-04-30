package com.futura.game;

import com.futura.game.core.GamePanel;
import com.futura.game.core.GameWindow;
import com.futura.game.world.MapType;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class FuturaGameApplication {
    private FuturaGameApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MapType selectedMap = chooseMapType();
            GamePanel gamePanel = new GamePanel(selectedMap);
            GameWindow gameWindow = new GameWindow(gamePanel);
            gameWindow.setVisible(true);
            gamePanel.startGame();
        });
    }

    private static MapType chooseMapType() {
        String[] mapOptions = {MapType.MAP_1.getDisplayName(), MapType.CITY_MAP.getDisplayName()};
        int selection = JOptionPane.showOptionDialog(
                null,
                "Choose a map before starting:",
                "Futura Game - Map Select",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                mapOptions,
                mapOptions[0]);

        if (selection == 1) {
            return MapType.CITY_MAP;
        }
        return MapType.MAP_1;
    }
}
