package main;

import java.awt.event.*;
import java.util.ArrayList;

public class KeyHandler implements KeyListener, MouseMotionListener, MouseListener {
    public int mouseX, mouseY;
    public boolean upPressed, downPressed, leftPressed, rightPressed, leftMousePressed, reloadPressed;
    public ArrayList<String> directionList = new ArrayList<>();
    public KeyHandler() {}

    // IMPLEMENTASI KEY LISTENER (Keyboard)
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = true;
            if (!directionList.contains("up")) directionList.add("up");
        }
        if (code == KeyEvent.VK_S) {
            downPressed = true;
            if (!directionList.contains("down")) directionList.add("down");
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = true;
            if (!directionList.contains("left")) directionList.add("left");
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = true;
            if (!directionList.contains("right")) directionList.add("right");
        }
        if (code == KeyEvent.VK_R) {
            reloadPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = false;
            directionList.remove("up");
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
            directionList.remove("down");
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
            directionList.remove("left");
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
            directionList.remove("right");
        }
        if (code == KeyEvent.VK_R) {
            reloadPressed = false;
        }
    }

    // IMPLEMENTASI MOUSE MOTION LISTENER (Mouse)
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    // IMPLEMENTASI MOUSE LISTENER (Klik Mouse)
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
