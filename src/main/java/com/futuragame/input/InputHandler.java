package com.futuragame.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private final InputState state = new InputState();

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> state.moveForward = true;
            case KeyEvent.VK_S -> state.moveBackward = true;
            case KeyEvent.VK_A -> state.rotateLeft = true;
            case KeyEvent.VK_D -> state.rotateRight = true;
            case KeyEvent.VK_SPACE -> state.firePressed = true;
            default -> {
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> state.moveForward = false;
            case KeyEvent.VK_S -> state.moveBackward = false;
            case KeyEvent.VK_A -> state.rotateLeft = false;
            case KeyEvent.VK_D -> state.rotateRight = false;
            case KeyEvent.VK_SPACE -> state.firePressed = false;
            default -> {
            }
        }
    }

    public InputState getState() {
        return state;
    }
}
