package com.futura.game.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkManager implements Closeable {

    public static final int PORT = 7777;

    private final Socket socket;
    private final PrintWriter writer;
    private final LinkedBlockingQueue<NetworkMessage> inbox;
    private final Thread receiveThread;
    private volatile boolean connected;

    private NetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        this.inbox = new LinkedBlockingQueue<>();
        this.connected = true;

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
        return new NetworkManager(new Socket(host, PORT));
    }

    private void receiveLoop() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                NetworkMessage msg = NetworkMessage.parse(line);
                if (msg != null) {
                    inbox.offer(msg);
                }
            }
        } catch (IOException ignored) {
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

    private void send(String message) {
        if (connected) {
            writer.println(message);
        }
    }

    public boolean isConnected() {
        return connected;
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
