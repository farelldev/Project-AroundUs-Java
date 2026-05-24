package main;


import combat.Bullet;
import entity.Player;
import entity.Zombie;
import item.Chest;
import tiles.TileManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    //SCREEN SETTINGS
    final int originalTileSize = 16; //16x16 harusnya tpi bisa ganti ganti
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxscreenCol = 16;
    public final int maxscreenRow = 12;
    final int screenWidht = tileSize * maxscreenCol;
    final int screenHeight = tileSize * maxscreenRow;

    //FPS limit
    int FPS = 60;


    private KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public TileManager tileM = new TileManager(this);
    public CollisionCheck cChecker = new CollisionCheck(this);
    public LevelManager levelM = new LevelManager(this);


    //Objek dalam game
    Player player =  new Player(this, this.keyH);
    public ArrayList<Bullet> bullets = new ArrayList<>();
    public ArrayList<Zombie> zombies = new ArrayList<>();
    public ArrayList<Chest> chests = new ArrayList<>();

    //set player default position
//    int playerX = 100;
//    int playerY = 100;
//    int playerSpeed = 4;

    public KeyHandler getKeyH() {
        return keyH;
    }

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidht, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseMotionListener(keyH);
        this.addMouseListener(keyH);
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
        levelM.update();

        for (int i = 0; i < bullets.size(); i++) {
            if (bullets.get(i).isActive()) {
                bullets.get(i).update();
            } else {
                bullets.remove(i);
                i--;
            }
        }
    }

    //run visual
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        tileM.draw(g2);
        player.draw(g2);

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g2);
        }

        g2.dispose();
    }

    public Player getPlayer() {
        return player;
    }
}