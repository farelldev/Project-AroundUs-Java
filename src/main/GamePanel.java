package main;

import combat.Bullet;
import entity.Player;
import entity.Zombie;
import item.Chest;
import item.ExplosiveBarrel;
import tiles.TileManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class GamePanel extends JPanel implements Runnable {

    // TILE & SCREEN SETTINGS
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize     = originalTileSize * scale;
    public final int maxscreenCol = 16;
    public final int maxscreenRow = 12;
    public final int screenWidht  = tileSize * maxscreenCol; // Tetap pakai ejaan ini agar tidak error di class lain
    public final int screenHeight = tileSize * maxscreenRow;

    // WORLD SETTINGS
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth  = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    int FPS = 60;

    private KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public ArrayList<Chest> chests = new ArrayList<>();
    public ArrayList<item.Items> droppedItems = new ArrayList<>();
    public final ArrayList<ExplosiveBarrel> barrels = new ArrayList<>();
    public TileManager    tileM    = new TileManager(this);
    public CollisionCheck cChecker = new CollisionCheck(this);
    public LevelManager   levelM   = new LevelManager(this);

    Player player = new Player(this, this.keyH);
    public ArrayList<Bullet> bullets = new ArrayList<>();

    // Zombie disimpan per floor — masing-masing floor punya list sendiri
    public final ArrayList<Zombie> zombiesFloor1 = new ArrayList<>();
    public final ArrayList<Zombie> zombiesFloor2 = new ArrayList<>();

    // Floor aktif saat ini (1 atau 2)
    public int activeFloor = 1;

    // Convenience: selalu akses via getZombies() agar aman
    public ArrayList<Zombie> zombies = zombiesFloor1; // legacy compat untuk LevelManager

    // Flag pindah floor
    private boolean pendingFloorSwitch = false;
    private int     pendingFloor       = -1;

    // Flag deteksi tangga — di-set oleh CollisionCheck, di-reset setiap update
    public boolean nearStair    = false;
    public int     nearStairCol = -1;
    public int     nearStairRow = -1;

    // Deteksi chest
    private BufferedImage buttonFSprite1, buttonFSprite2;
    public boolean nearChest = false;
    public Chest activeChest = null;

    // UI Images
    private BufferedImage hpImg100, hpImg75, hpImg50, hpImg25, hpImg10;
    private BufferedImage zombieCounterBG; // <-- VARIABEL BARU UNTUK BG ZOMBIE
    private BufferedImage buttonESprite1, buttonESprite2;
    private int buttonAnimCounter = 0;
    private int buttonAnimFrame   = 0; // 0 = sprite1, 1 = sprite2

    // Courier Prime fonts
    private Font cpRegular, cpBold, cpItalic, cpBoldItalic;

    public KeyHandler getKeyH() { return keyH; }

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidht, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseMotionListener(keyH);
        this.addMouseListener(keyH);
        this.setFocusable(true);

        loadHealthImages();
        loadUIImages(); // <-- PANGGIL METHOD LOAD GAMBAR UI DISINI
        loadFonts();
    }

    private void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            InputStream isReg  = getClass().getResourceAsStream("/fonts/NineByFiveNbp-MypB.ttf");
            InputStream isBold = getClass().getResourceAsStream("/fonts/NineByFiveNbp-MypB.ttf");
            InputStream isItal = getClass().getResourceAsStream("/fonts/NineByFiveNbp-MypB.ttf");
            InputStream isBoldItal = getClass().getResourceAsStream("/fonts/NineByFiveNbp-MypB.ttf");

            Font baseReg      = Font.createFont(Font.TRUETYPE_FONT, isReg);
            Font baseBold     = Font.createFont(Font.TRUETYPE_FONT, isBold);
            Font baseItal     = Font.createFont(Font.TRUETYPE_FONT, isItal);
            Font baseBoldItal = Font.createFont(Font.TRUETYPE_FONT, isBoldItal);

            ge.registerFont(baseReg);
            ge.registerFont(baseBold);
            ge.registerFont(baseItal);
            ge.registerFont(baseBoldItal);

            cpRegular    = baseReg.deriveFont(Font.PLAIN,  12f);
            cpBold       = baseBold.deriveFont(Font.PLAIN, 13f);
            cpItalic     = baseItal.deriveFont(Font.PLAIN, 11f);
            cpBoldItalic = baseBoldItal.deriveFont(Font.PLAIN, 13f);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[GamePanel] Gagal memuat Courier Prime font!");
            cpRegular    = new Font("Monospaced", Font.PLAIN,  12);
            cpBold       = new Font("Monospaced", Font.BOLD,   13);
            cpItalic     = new Font("Monospaced", Font.ITALIC, 11);
            cpBoldItalic = new Font("Monospaced", Font.BOLD,   13);
        }
    }

    private void loadHealthImages() {
        try {
            hpImg100 = ImageIO.read(getClass().getResourceAsStream("/player/healthPercentage/100percent.png"));
            hpImg75  = ImageIO.read(getClass().getResourceAsStream("/player/healthPercentage/75percent.png"));
            hpImg50  = ImageIO.read(getClass().getResourceAsStream("/player/healthPercentage/50percent.png"));
            hpImg25  = ImageIO.read(getClass().getResourceAsStream("/player/healthPercentage/25percent.png"));
            hpImg10  = ImageIO.read(getClass().getResourceAsStream("/player/healthPercentage/10percent.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[GamePanel] Gagal memuat health percentage images!");
        }
    }

    // <-- METHOD BARU UNTUK LOAD GAMBAR UI LAINNYA
    private void loadUIImages() {
        try {
            zombieCounterBG = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/zombieCounterBG.png"));

            buttonESprite1  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonE_sprite1.png"));
            buttonESprite2  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonE_sprite2.png"));

            buttonFSprite1  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonF_sprite1.png"));
            buttonFSprite2  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonF_sprite2.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[GamePanel] Gagal memuat UI zombieCounterBG.png!");
        }
    }

    public ArrayList<Zombie> getActiveZombies() {
        return activeFloor == 1 ? zombiesFloor1 : zombiesFloor2;
    }

    public void requestFloorSwitch(int toFloor) {
        pendingFloorSwitch = true;
        pendingFloor       = toFloor;
    }

    private void applyFloorSwitch() {
        activeFloor = pendingFloor;
        zombies     = getActiveZombies();
        pendingFloorSwitch = false;
        System.out.println("[GamePanel] Pindah ke Floor " + activeFloor
                + " — zombie di floor ini: " + getActiveZombies().size());
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta  = 0;
        long lastTime = System.nanoTime();
        long timer    = 0;
        int  drawCount = 0;

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }
            if (timer >= 1_000_000_000) {
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        // Reset deteksi tangga setiap frame — CollisionCheck akan set ulang jika masih dekat
        nearStair    = false;
        nearStairCol = -1;
        nearStairRow = -1;

        nearChest = false;
        activeChest = null;

        // Cek tangga setiap frame (termasuk saat player diam)
        cChecker.checkNearStair();

        player.update();
        levelM.update();

        ArrayList<Zombie> activeZombies = getActiveZombies();

        Iterator<item.ExplosiveBarrel> barrelIt = barrels.iterator();
        while (barrelIt.hasNext()) {
            item.ExplosiveBarrel barrel = barrelIt.next();
            if (barrel.isDestroyed) {
                barrelIt.remove(); // Hapus tong yang asapnya udah hilang
                continue;
            }
            barrel.update();
        }

        Iterator<Bullet> bIt = bullets.iterator();
        while (bIt.hasNext()) {
            Bullet b = bIt.next();
            if (!b.isActive()) { bIt.remove(); continue; }
            b.update();

            if (b.isActive()) {
                Rectangle bulletBounds = new Rectangle((int)b.getBounds().x, (int)b.getBounds().y, 8, 8);

                for (Zombie z : activeZombies) {
                    if (!z.isDead()) {
                        Rectangle zombieBounds = new Rectangle(
                                z.x + z.solidArea.x,
                                z.y + z.solidArea.y,
                                z.solidArea.width,
                                z.solidArea.height
                        );
                        if (bulletBounds.intersects(zombieBounds)) {
                            b.hit(z);
                            break;
                        }
                    }
                }

                if (b.isActive()) { // Pastikan peluru blm hancur kena zombie
                    for (item.ExplosiveBarrel barrel : barrels) {
                        if (!barrel.isExploding && !barrel.isDestroyed) {
                            if (bulletBounds.intersects(barrel.solidArea)) {
                                b.hit(barrel); // Peluru hancur, HP tong berkurang
                                break;
                            }
                        }
                    }
                }
            }
        }

        Iterator<Zombie> zIt = activeZombies.iterator();
        while (zIt.hasNext()) {
            Zombie z = zIt.next();
            z.update();
            if (z.isDoneWithDeadAnim()) {
                zIt.remove();
            }
        }

        Iterator<item.Items> itemIt = droppedItems.iterator();
        while (itemIt.hasNext()) {
            item.Items item = itemIt.next();

            // Hitung jarak player dengan item
            float dx = player.x - item.x;
            float dy = player.y - item.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < tileSize / 1.5) {
                item.use(player); // Gunakan efeknya (nambah HP/Peluru)
                itemIt.remove();  // Hapus item dari lantai
            }
        }

        if (pendingFloorSwitch) {
            applyFloorSwitch();
        }

        // Cek tombol E untuk naik/turun tangga
        if (nearStair && keyH.ePressed) {
            tileM.switchFloor(nearStairCol, nearStairRow);
            keyH.ePressed = false; // konsumsi satu kali tekan
        }

        for (item.Chest c : chests) {
            if (!c.isOpened() && c.isPlayerNearby(player)) {
                nearChest = true;
                activeChest = c;

                if (keyH.fPressed) {
                    c.open(player, this);
                    keyH.fPressed = false; // Konsumsi klik agar tidak spam
                }
                break; // Cukup deteksi 1 peti terdekat
            }
        }

        // Animasi sprite tombol E
        if (nearStair || nearChest) {
            buttonAnimCounter++;
            if (buttonAnimCounter >= 30) {
                buttonAnimFrame   = (buttonAnimFrame == 0) ? 1 : 0;
                buttonAnimCounter = 0;
            }
        } else {
            buttonAnimCounter = 0;
            buttonAnimFrame   = 0;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tileM.draw(g2);
        for (Chest c : chests) { c.draw(g2); }
        for (item.Items item : droppedItems) { item.draw(g2, this); }
        for (item.ExplosiveBarrel barrel : barrels) { barrel.draw(g2); }
        for (Zombie z : getActiveZombies()) { z.draw(g2); }
        player.draw(g2);
        for (Bullet b : bullets) { b.draw(g2, this); }

        drawHUD(g2);

        g2.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ---- HEALTH BAR ----
        float hpRatio = (float) player.getHp() / player.getMaxHp();
        BufferedImage hpImg = getHealthImage(hpRatio);

        if (hpImg != null) {
            int imgSize = 64;
            int imgX    = 10;
            int imgY    = screenHeight - imgSize - 10;
            g2.drawImage(hpImg, imgX, imgY, imgSize, imgSize, null);
        }

        // ---- AMMO ----
        int ammo    = player.getWeapon().getAmmo();
        int maxAmmo = player.getWeapon().getMaxAmmo();
        boolean reloading = player.getWeapon().isReloading();

        int ammoBgX = screenWidht - 150;
        int ammoBgY = screenHeight - 36;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(ammoBgX, ammoBgY, 140, 26, 8, 8);

        if (reloading) {
            g2.setFont(cpBoldItalic);
            g2.setColor(new Color(255, 210, 50));
            g2.drawString("reloading...", ammoBgX + 8, ammoBgY + 18);
        } else {
            g2.setFont(cpItalic);
            g2.setColor(new Color(180, 180, 180));
            g2.drawString("AMMO", ammoBgX + 8, ammoBgY + 14);
            g2.setFont(cpBold);
            g2.setColor(Color.WHITE);
            g2.drawString(ammo + " / " + maxAmmo, ammoBgX + 52, ammoBgY + 19);
        }

        // ---- LEVEL ----
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 10, 110, 24, 8, 8);
        g2.setFont(cpItalic);
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("LVL", 18, 24);
        g2.setFont(cpBold);
        g2.setColor(new Color(255, 230, 50));
        g2.drawString(String.valueOf(levelM.currentLevel), 52, 24);

        // ---- FLOOR ----
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 38, 110, 24, 8, 8);
        g2.setFont(cpItalic);
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("FLOOR", 18, 52);
        g2.setFont(cpBold);
        g2.setColor(new Color(150, 210, 255));
        g2.drawString(String.valueOf(tileM.currentFloor), 76, 52);

        // ---- ZOMBIE COUNTER (UPDATED DENGAN GAMBAR BG) ----
        // ---- ZOMBIE COUNTER ----
        long aliveF1 = zombiesFloor1.stream().filter(z -> !z.isDead()).count();
        long aliveF2 = zombiesFloor2.stream().filter(z -> !z.isDead()).count();
        long total   = aliveF1 + aliveF2;

        String zombieStr = "[ ZOMBIE: " + total + " ]";
        g2.setFont(cpBoldItalic);
        FontMetrics fmz = g2.getFontMetrics();
        int zW = fmz.stringWidth(zombieStr);

        // Ukuran dan posisi background
        int bgWidth = zW + 24;
        int bgHeight = 32;
        int bgX = (screenWidht / 2) - (bgWidth / 2);
        int bgY = 4;

        // Gambar background
        if (zombieCounterBG != null) {
            g2.drawImage(zombieCounterBG, bgX, bgY, bgWidth, bgHeight, null);
        } else {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 8, 8);
        }

        // Posisi teks (Otomatis presisi di tengah background)
        int textX = (screenWidht / 2) - (zW / 2);
        int textY = bgY + ((bgHeight - fmz.getHeight()) / 2) + fmz.getAscent();

        // Tulis teksnya
        g2.setColor(new Color(230, 80, 80));
        g2.drawString(zombieStr, textX, textY);

        // ---- STAIR INTERACTION PROMPT (muncul saat dekat tangga) ----
        if (nearStair) {
            drawStairPrompt(g2);
        }

        if (nearChest && activeChest != null) {
            drawChestPrompt(g2);
        }
    }

    private void drawStairPrompt(Graphics2D g2) {
        // Konversi posisi tangga dari world coords ke screen coords
        int stairWorldX = nearStairCol * tileSize;
        int stairWorldY = nearStairRow * tileSize;
        int stairScreenX = stairWorldX - player.x + player.screenX;
        int stairScreenY = stairWorldY - player.y + player.screenY;

        // Pilih sprite berdasarkan frame animasi
        BufferedImage btnSprite = (buttonAnimFrame == 0) ? buttonESprite1 : buttonESprite2;
        if (btnSprite == null) return;

        int btnW = 20;
        int btnH = 20;

        // Posisi di tengah atas tile tangga
        int btnX = stairScreenX + (tileSize / 2) - (btnW / 2);
        // Sprite2 turun sedikit untuk efek "klik"
        int btnY = stairScreenY - btnH - 6 + (buttonAnimFrame == 1 ? 4 : 0);

        g2.drawImage(btnSprite, btnX, btnY, btnW, btnH, null);
    }

    private void drawChestPrompt(Graphics2D g2) {
        // Konversi posisi peti dari world ke screen
        int chestScreenX = (int) activeChest.getX() - player.x + player.screenX;
        int chestScreenY = (int) activeChest.getY() - player.y + player.screenY;

        // Gunakan animasi yang sama dengan tombol E
        BufferedImage btnSprite = (buttonAnimFrame == 0) ? buttonFSprite1 : buttonFSprite2;
        if (btnSprite == null) return;

        int btnW = 20;
        int btnH = 20;

        // Posisi di atas peti
        int btnX = chestScreenX + (tileSize / 2) - (btnW / 2);
        int btnY = chestScreenY - btnH - 6 + (buttonAnimFrame == 1 ? 4 : 0);

        g2.drawImage(btnSprite, btnX, btnY, btnW, btnH, null);
    }

    private BufferedImage getHealthImage(float ratio) {
        if (ratio > 0.75f) return hpImg100;
        if (ratio > 0.50f) return hpImg75;
        if (ratio > 0.25f) return hpImg50;
        if (ratio > 0.10f) return hpImg25;
        return hpImg10;
    }

    public Player getPlayer() { return player; }
}