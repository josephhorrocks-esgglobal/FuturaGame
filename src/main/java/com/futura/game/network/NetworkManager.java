package com.futura.game.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkManager implements Closeable {

    public static final int PORT = 7777;
    public static final int CONNECT_TIMEOUT_MS = 8000;

    private final Socket socket;
    private final PrintWriter writer;
    private final LinkedBlockingQueue<NetworkMessage> inbox;
    private final Thread receiveThread;
    private volatile boolean connected;
    private volatile String lastError;

    private NetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        this.inbox = new LinkedBlockingQueue<>();
        this.connected = true;
        this.lastError = "";

        this.receiveThread = new Thread(this::receiveLoop, "network-receive");
        this.receiveThread.setDaemon(true);
        this.receiveThread.start();
    }

    public static NetworkManager host(ServerSocket serverSocket) throws IOException {
        Socket client = serverSocket.accept();
        serverSocket.close();
        return new NetworkManager(client);
    }

    public static NetworkManager join(String host) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, PORT), CONNECT_TIMEOUT_MS);
        return new NetworkManager(socket);
    }

    private void receiveLoop() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                NetworkMessage msg = NetworkMessage.parse(line);
                if (msg != null) {
                    inbox.offer(msg);
                } else {
                    System.err.println("[Network] Ignored malformed message: " + line);
                }
            }
        } catch (IOException e) {
            lastError = e.getMessage();
            System.err.println("[Network] Connection error: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    public List<NetworkMessage> pollMessages() {
        List<NetworkMessage> messages = new ArrayList<>();
        inbox.drainTo(messages);
        return messages;
    }

    public void sendPosition(double x, double y, double rotation) {
        send(String.format("POS:%.2f,%.2f,%.5f", x, y, rotation));
    }

    public void sendShot(double muzzleX, double muzzleY, double rotation) {
        send(String.format("SHOT:%.2f,%.2f,%.5f", muzzleX, muzzleY, rotation));
    }

    public void sendHit() {
        send("HIT");
    }

    public void sendHealth(int health) {
        send("HEALTH:" + health);
    }

    public void sendDead() {
        send("DEAD");
    }

    public void sendRestart() {
        send("RESTART");
    }

    public void sendMapType(int mapOrdinal) {
        send("MAP:" + mapOrdinal);
    }

    public Integer awaitMapType(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        List<NetworkMessage> deferred = new ArrayList<>();
        while (System.currentTimeMillis() < deadline && connected) {
            List<NetworkMessage> messages = pollMessages();
            for (NetworkMessage message : messages) {
                if (message.type() == NetworkMessage.Type.MAP && message.data().length > 0) {
                    inbox.addAll(deferred);
                    return (int) message.data()[0];
                }
                deferred.add(message);
            }

            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                inbox.addAll(deferred);
                return null;
            }
        }
        inbox.addAll(deferred);
        return null;
    }

    private void send(String message) {
        if (connected) {
            writer.println(message);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getLastError() {
        return lastError;
    }

    public static String getLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    @Override
    public void close() {
        connected = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
