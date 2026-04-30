package com.futuragame;

import com.futuragame.core.GameWindow;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.showWindow();
        });
    }
}
