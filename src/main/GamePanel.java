package main;

import combat.Bullet;
import entity.Player;
import entity.Zombie;
import item.Chest;
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
    public final int screenWidht  = tileSize * maxscreenCol;
    public final int screenHeight = tileSize * maxscreenRow;

    // WORLD SETTINGS
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth  = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    int FPS = 60;

    private KeyHandler keyH = new KeyHandler();

    private final KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public TileManager    tileM    = new TileManager(this);
    public CollisionCheck cChecker = new CollisionCheck(this);
    public LevelManager   levelM   = new LevelManager(this);

    Player player = new Player(this, this.keyH);
    public ArrayList<Bullet> bullets = new ArrayList<>();

    // Zombie disimpan per floor — masing-masing floor punya list sendiri
    public final ArrayList<Zombie> zombiesFloor1 = new ArrayList<>();
    public final ArrayList<Zombie> zombiesFloor2 = new ArrayList<>();

    // Floor aktif saat ini (1 atau 2) — JANGAN simpan referensi list langsung
    // karena reassign di tengah iterasi menyebabkan ConcurrentModificationException
    public int activeFloor = 1;

    // Convenience: selalu akses via getZombies() agar aman
    public ArrayList<Zombie> zombies = zombiesFloor1; // legacy compat untuk LevelManager

    public ArrayList<Chest> chests = new ArrayList<>();

    // Flag pindah floor — diproses SETELAH update loop selesai, bukan di tengahnya
    private boolean pendingFloorSwitch = false;
    private int     pendingFloor       = -1;

    // Health bar images
    private BufferedImage hpImg100, hpImg75, hpImg50, hpImg25, hpImg10;

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
        loadFonts();
    }

    private void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            InputStream isReg  = getClass().getResourceAsStream("/fonts/CourierPrime-Regular.ttf");
            InputStream isBold = getClass().getResourceAsStream("/fonts/CourierPrime-Bold.ttf");
            InputStream isItal = getClass().getResourceAsStream("/fonts/CourierPrime-Italic.ttf");
            InputStream isBoldItal = getClass().getResourceAsStream("/fonts/CourierPrime-BoldItalic.ttf");

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

    /** Kembalikan list zombie floor yang sedang aktif */
    public ArrayList<Zombie> getActiveZombies() {
        return activeFloor == 1 ? zombiesFloor1 : zombiesFloor2;
    }

    /** Dipanggil oleh TileManager — tandai perpindahan floor, eksekusi setelah update loop */
    public void requestFloorSwitch(int toFloor) {
        pendingFloorSwitch = true;
        pendingFloor       = toFloor;
    }

    /** Eksekusi perpindahan floor secara aman (di luar loop iterasi zombie) */
    private void applyFloorSwitch() {
        activeFloor = pendingFloor;
        zombies     = getActiveZombies(); // sync referensi legacy
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
        player.update();
        levelM.update();

        ArrayList<Zombie> activeZombies = getActiveZombies();

        // Update & bersihkan bullet
        Iterator<Bullet> bIt = bullets.iterator();
        while (bIt.hasNext()) {
            Bullet b = bIt.next();
            if (!b.isActive()) { bIt.remove(); continue; }
            b.update();

            // Cek hit ke zombie floor aktif
            if (b.isActive()) {
                for (Zombie z : activeZombies) {
                    if (!z.isDead()) {
                        Rectangle bulletBounds = new Rectangle(b.x, b.y, 8, 8);
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
            }
        }

        // Update & bersihkan zombie floor aktif
        Iterator<Zombie> zIt = activeZombies.iterator();
        while (zIt.hasNext()) {
            Zombie z = zIt.next();
            z.update();
            if (z.isDoneWithDeadAnim()) {
                zIt.remove();
            }
        }

        // *** Terapkan floor switch SETELAH semua iterasi selesai ***
        if (pendingFloorSwitch) {
            applyFloorSwitch();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Tile / Map
        tileM.draw(g2);

        // 2. Zombie floor aktif
        for (Zombie z : getActiveZombies()) {
            z.draw(g2);
        }

        // 3. Player
        player.draw(g2);

        // 4. Bullet
        for (Bullet b : bullets) {
            b.draw(g2, this);
        }

        // 5. HUD
        drawHUD(g2);

        g2.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ----------------------------------------------------------------
        // HEALTH BAR — gambar 1:1, dipilih berdasarkan % HP
        // ----------------------------------------------------------------
        float hpRatio = (float) player.getHp() / player.getMaxHp();
        BufferedImage hpImg = getHealthImage(hpRatio);

        if (hpImg != null) {
            int imgSize = 64;
            int imgX    = 10;
            int imgY    = screenHeight - imgSize - 10;
            g2.drawImage(hpImg, imgX, imgY, imgSize, imgSize, null);
        }

        // ---- AMMO — pojok kanan bawah, Bold untuk angka, Italic untuk label ----
        int ammo    = player.getWeapon().getAmmo();
        int maxAmmo = player.getWeapon().getMaxAmmo();
        boolean reloading = player.getWeapon().isReloading();

        int ammoBgX = screenWidht - 150;
        int ammoBgY = screenHeight - 36;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(ammoBgX, ammoBgY, 140, 26, 8, 8);

        if (reloading) {
            // "reloading..." pakai BoldItalic + warna kuning
            g2.setFont(cpBoldItalic);
            g2.setColor(new Color(255, 210, 50));
            g2.drawString("reloading...", ammoBgX + 8, ammoBgY + 18);
        } else {
            // Label "AMMO" pakai Italic kecil abu-abu
            g2.setFont(cpItalic);
            g2.setColor(new Color(180, 180, 180));
            g2.drawString("AMMO", ammoBgX + 8, ammoBgY + 14);
            // Angka pakai Bold putih
            g2.setFont(cpBold);
            g2.setColor(Color.WHITE);
            g2.drawString(ammo + " / " + maxAmmo, ammoBgX + 52, ammoBgY + 19);
        }

        // ---- LEVEL — pojok kiri atas, label Italic + angka Bold ----
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 10, 110, 24, 8, 8);
        g2.setFont(cpItalic);
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("LVL", 18, 24);
        g2.setFont(cpBold);
        g2.setColor(new Color(255, 230, 50));
        g2.drawString(String.valueOf(levelM.currentLevel), 52, 24);

        // ---- FLOOR — di bawah Level, label Regular + angka Bold ----
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 38, 110, 24, 8, 8);
        g2.setFont(cpItalic);
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("FLOOR", 18, 52);
        g2.setFont(cpBold);
        g2.setColor(new Color(150, 210, 255));
        g2.drawString(String.valueOf(tileM.currentFloor), 76, 52);

        // ---- ZOMBIE COUNTER — tengah atas, BoldItalic merah ----
        long aliveF1 = zombiesFloor1.stream().filter(z -> !z.isDead()).count();
        long aliveF2 = zombiesFloor2.stream().filter(z -> !z.isDead()).count();
        long total   = aliveF1 + aliveF2;

        String zombieStr = "[ ZOMBIE: " + total + " ]";
        g2.setFont(cpBoldItalic);
        FontMetrics fmz = g2.getFontMetrics();
        int zW = fmz.stringWidth(zombieStr);

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(screenWidht / 2 - zW / 2 - 6, 8, zW + 12, 22, 8, 8);
        g2.setColor(new Color(230, 80, 80));
        g2.drawString(zombieStr, screenWidht / 2 - zW / 2, 24);
    }

    /**
     * Pilih gambar health bar berdasarkan % HP:
     *   > 75%  → 100percent.png
     *   > 50%  → 75percent.png
     *   > 25%  → 50percent.png
     *   > 10%  → 25percent.png
     *   ≤ 10%  → 10percent.png
     */
    private BufferedImage getHealthImage(float ratio) {
        if (ratio > 0.75f) return hpImg100;
        if (ratio > 0.50f) return hpImg75;
        if (ratio > 0.25f) return hpImg50;
        if (ratio > 0.10f) return hpImg25;
        return hpImg10;
    }

    public Player getPlayer() { return player; }
}
