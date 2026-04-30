package com.futura.game;

import com.futura.game.core.GamePanel;
import com.futura.game.core.GameWindow;
import com.futura.game.network.NetworkManager;
import com.futura.game.world.MapType;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

public final class FuturaGameApplication {
    private FuturaGameApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MapType selectedMap = chooseMapType();
            NetworkManager networkManager = chooseNetworkMode();
            GamePanel gamePanel = new GamePanel(selectedMap, networkManager);
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

    private static NetworkManager chooseNetworkMode() {
        String[] options = {"Single Player (vs AI)", "Host Game", "Join Game"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select game mode:",
                "Futura Game - Network",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 1) {
            return hostGame();
        }
        if (choice == 2) {
            return joinGame();
        }
        return null;
    }

    private static NetworkManager hostGame() {
        String localIp = NetworkManager.getLocalAddress();

        JDialog dialog = new JDialog((Frame) null, "Hosting Game", true);
        JLabel label = new JLabel(
                "<html><center>Waiting for opponent to connect...<br><br>"
                + "<b>Your IP:</b> " + localIp + "<br>"
                + "<b>Port:</b> " + NetworkManager.PORT
                + "</center></html>");
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(cancelBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(label, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setSize(340, 160);
        dialog.setLocationRelativeTo(null);

        final NetworkManager[] result = {null};
        final ServerSocket[] serverSocketRef = {null};

        SwingWorker<NetworkManager, Void> worker = new SwingWorker<>() {
            @Override
            protected NetworkManager doInBackground() throws Exception {
                ServerSocket ss = new ServerSocket(NetworkManager.PORT);
                serverSocketRef[0] = ss;
                return NetworkManager.host(ss);
            }

            @Override
            protected void done() {
                try {
                    result[0] = get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
                dialog.dispose();
            }
        };

        cancelBtn.addActionListener(e -> {
            worker.cancel(true);
            if (serverSocketRef[0] != null) {
                try { serverSocketRef[0].close(); } catch (Exception ignored) {}
            }
            dialog.dispose();
        });

        worker.execute();
        dialog.setVisible(true); // blocks EDT until disposed
        return result[0];
    }

    private static NetworkManager joinGame() {
        String ip = JOptionPane.showInputDialog(
                null,
                "Enter the host's IP address:",
                "Join Game",
                JOptionPane.QUESTION_MESSAGE);
        if (ip == null || ip.isBlank()) {
            return null;
        }

        JDialog dialog = new JDialog((Frame) null, "Connecting...", true);
        JLabel label = new JLabel(
                "<html><center>Connecting to <b>" + ip.trim() + ":" + NetworkManager.PORT + "</b>...</center></html>");
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30));
        dialog.add(label);
        dialog.setSize(300, 110);
        dialog.setLocationRelativeTo(null);

        final NetworkManager[] result = {null};
        final String trimmedIp = ip.trim();

        SwingWorker<NetworkManager, Void> worker = new SwingWorker<>() {
            @Override
            protected NetworkManager doInBackground() throws Exception {
                return NetworkManager.join(trimmedIp);
            }

            @Override
            protected void done() {
                try {
                    result[0] = get();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null,
                            "Could not connect to " + trimmedIp,
                            "Connection Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
                dialog.dispose();
            }
        };

        worker.execute();
        dialog.setVisible(true); // blocks EDT until disposed
        return result[0];
    }
}
