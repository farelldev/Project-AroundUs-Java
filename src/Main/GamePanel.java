package Main;

import Entity.Player;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    //SCREEN SETTINGS
    final int originalTileSize = 30; //16x16
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    final int maxscreenCol = 16;
    final int maxscreenRow = 12;
    final int screenWidht = tileSize * maxscreenCol;
    final int screenHeight = tileSize * maxscreenRow;

    //FPS limit
    int FPS = 60;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player =  new Player(this, keyH);


    //set player default position
    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;


    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidht, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

//    @Override
//    public void run() {
//
//        double drawInterval = 1000000000 / FPS;
//        double nextDrawTime = System.nanoTime() + drawInterval;
//        while(gameThread != null) {
//
//
//            // Update info seperti posisi chara
//            update();
//            // Menulis Ulang layar dengan updated info
//            repaint();
//
//            double remainingTime = nextDrawTime - System.nanoTime();
//            remainingTime /= 1000000;
//
//            if  (remainingTime < 0) {
//                remainingTime = 0;
//            }
//
//            // --- BAGIAN YANG DIPERBAIKI ---
//            try {
//                Thread.sleep((long) remainingTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace(); // Menampilkan pesan error di konsol jika thread terganggu
//            }
//
//            nextDrawTime += drawInterval;
//        }

//    }

    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime)/drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {

                update();
                repaint();
                delta--;
                drawCount ++;
            }
            //FPS show ini guis
            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {

    player.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        player.draw(g2);
        g2.dispose();
    }


}
