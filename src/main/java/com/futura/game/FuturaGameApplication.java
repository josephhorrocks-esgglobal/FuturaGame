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
import javax.swing.Timer;
import javax.swing.JWindow;
import javax.swing.BorderFactory;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import javax.swing.plaf.FontUIResource;

public final class FuturaGameApplication {
    private enum LaunchMode {
        SINGLE,
        HOST,
        JOIN
    }

    private record StartupConfig(MapType mapType, NetworkManager networkManager) {
    }

    private FuturaGameApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installMilitaryUiFont();
            showSplashScreen(() -> {
            StartupConfig startupConfig = chooseStartupConfig();
            if (startupConfig == null) {
                return;
            }

            GamePanel gamePanel = new GamePanel(startupConfig.mapType(), startupConfig.networkManager());
            GameWindow gameWindow = new GameWindow(gamePanel);
            gameWindow.setVisible(true);
            gamePanel.startGame();
            });
        });
    }

    private static void showSplashScreen(Runnable onComplete) {
        JWindow splash = new JWindow();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(22, 24, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(150, 85, 55), 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("OPERATION TANK DUEL");
        title.setForeground(new Color(242, 206, 143));
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        panel.add(title, gbc);

        gbc.gridy = 1;
        JLabel subtitle = new JLabel("Warzone briefing loading...");
        subtitle.setForeground(new Color(208, 208, 208));
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 16));
        panel.add(subtitle, gbc);

        gbc.gridy = 2;
        JLabel footer = new JLabel("Futura Armored Command");
        footer.setForeground(new Color(171, 184, 200));
        footer.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(footer, gbc);

        splash.setContentPane(panel);
        splash.setSize(520, 220);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        Timer timer = new Timer(2000, e -> {
            splash.dispose();
            onComplete.run();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static void installMilitaryUiFont() {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof FontUIResource fontResource) {
                defaults.put(key, new FontUIResource("Monospaced", fontResource.getStyle(), fontResource.getSize()));
            }
        }
    }

    private static StartupConfig chooseStartupConfig() {
        LaunchMode mode = chooseLaunchMode();
        if (mode == null) {
            return null;
        }

        if (mode == LaunchMode.SINGLE) {
            return new StartupConfig(chooseMapType(), null);
        }

        if (mode == LaunchMode.HOST) {
            MapType selectedMap = chooseMapType();
            NetworkManager manager = hostGame();
            if (manager == null) {
                return null;
            }
            manager.sendMapType(selectedMap.ordinal());
            return new StartupConfig(selectedMap, manager);
        }

        NetworkManager manager = joinGame();
        if (manager == null) {
            return null;
        }

        MapType selectedMap = awaitMapTypeFromHost(manager);
        if (selectedMap == null) {
            manager.close();
            JOptionPane.showMessageDialog(
                    null,
                    "Timed out waiting for host map selection.",
                    "Multiplayer Setup Failed",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return new StartupConfig(selectedMap, manager);
    }

    private static LaunchMode chooseLaunchMode() {
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
            return LaunchMode.HOST;
        }
        if (choice == 2) {
            return LaunchMode.JOIN;
        }
        if (choice == 0) {
            return LaunchMode.SINGLE;
        }
        return null;
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to host game on port " + NetworkManager.PORT + "\n" + message,
                            "Host Failed",
                            JOptionPane.ERROR_MESSAGE);
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
                            "Could not connect to " + trimmedIp + ":" + NetworkManager.PORT
                                    + "\nPlease verify firewall and port forwarding settings.",
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

    private static MapType awaitMapTypeFromHost(NetworkManager manager) {
        JDialog dialog = new JDialog((Frame) null, "Waiting for Host", true);
        JLabel label = new JLabel("<html><center>Connected.<br>Waiting for host to select a map...</center></html>");
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30));
        dialog.add(label);
        dialog.setSize(320, 120);
        dialog.setLocationRelativeTo(null);

        final Integer[] mapOrdinal = {null};
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return manager.awaitMapType(15000L);
            }

            @Override
            protected void done() {
                try {
                    mapOrdinal[0] = get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ignored) {
                }
                dialog.dispose();
            }
        };

        worker.execute();
        dialog.setVisible(true);

        if (mapOrdinal[0] == null) {
            return null;
        }

        MapType[] values = MapType.values();
        int idx = mapOrdinal[0];
        if (idx < 0 || idx >= values.length) {
            return null;
        }
        return values[idx];
    }
}
