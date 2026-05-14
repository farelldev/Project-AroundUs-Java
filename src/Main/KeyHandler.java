package Main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public ArrayList<String> directionList = new ArrayList<>();
    public KeyHandler() {}

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
    }
}
