package main;

import combat.Bullet;
import entity.Player;
import entity.Zombie;
import item.Chest;
import item.ExplosiveBarrel;
import item.Items;
import tiles.TileManager;
import ui.SoundManager;

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
    Thread gameThread;

    // ── FloorState: semua entity per lantai ada di sini ──────────────────────
    // Index 0 tidak dipakai — gunakan floor[1] dan floor[2] langsung
    public final FloorState[] floor = new FloorState[] {
        null,               // index 0 (tidak dipakai)
        new FloorState(1),  // Lantai 1
        new FloorState(2)   // Lantai 2
    };

    public int activeFloor = 1;

    /** Kembalikan FloorState yang sedang aktif */
    public FloorState getActiveFloor() {
        return floor[activeFloor];
    }

    /** Convenience getters — agar kode lama yang akses langsung tetap compile */
    public ArrayList<Zombie>          getActiveZombies()  { return getActiveFloor().zombies; }
    public ArrayList<Chest>           getActiveChests()   { return getActiveFloor().chests; }
    public ArrayList<ExplosiveBarrel> getActiveBarrels()  { return getActiveFloor().barrels; }
    public ArrayList<Items>           getActiveDropped()  { return getActiveFloor().droppedItems; }
    public ArrayList<Bullet>          getActiveBullets()  { return getActiveFloor().bullets; }

    // ── Managers ─────────────────────────────────────────────────────────────
    public TileManager    tileM    = new TileManager(this);
    public CollisionCheck cChecker = new CollisionCheck(this);
    public LevelManager   levelM   = new LevelManager(this);
    public final SoundManager soundManager;
    public final ui.BloodStainOverlay bloodStain = new ui.BloodStainOverlay(this);

    Player player = new Player(this, this.keyH);

    // ── Flag pindah floor ─────────────────────────────────────────────────────
    private boolean pendingFloorSwitch = false;
    private int     pendingFloor       = -1;

    // ── Flag deteksi tangga ───────────────────────────────────────────────────
    public boolean nearStair    = false;
    public int     nearStairCol = -1;
    public int     nearStairRow = -1;

    // ── Deteksi chest ─────────────────────────────────────────────────────────
    public boolean nearChest   = false;
    public Chest   activeChest = null;

    // ── UI Images ─────────────────────────────────────────────────────────────
    private BufferedImage hpImg100, hpImg75, hpImg50, hpImg25, hpImg10;
    private BufferedImage zombieCounterBG;
    private BufferedImage buttonESprite1, buttonESprite2;
    private BufferedImage buttonFSprite1, buttonFSprite2;
    private int buttonAnimCounter = 0;
    private int buttonAnimFrame   = 0;

    private Font cpRegular, cpBold, cpItalic, cpBoldItalic;

    public KeyHandler getKeyH() { return keyH; }

    public GamePanel() {
        this(new SoundManager());
    }

    public GamePanel(SoundManager soundManager) {
        this.soundManager = soundManager != null ? soundManager : new SoundManager();
        this.setPreferredSize(new Dimension(screenWidht, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseMotionListener(keyH);
        this.addMouseListener(keyH);
        this.setFocusable(true);

        loadHealthImages();
        loadUIImages();
        loadFonts();

        soundManager.playBGM("beforePlay");
    }

    // ── Loader ────────────────────────────────────────────────────────────────

    private void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            InputStream isReg = getClass().getResourceAsStream("/fonts/NineByFiveNbp-MypB.ttf");
            Font base = Font.createFont(Font.TRUETYPE_FONT, isReg);
            ge.registerFont(base);
            cpRegular    = base.deriveFont(Font.PLAIN, 12f);
            cpBold       = base.deriveFont(Font.PLAIN, 13f);
            cpItalic     = base.deriveFont(Font.PLAIN, 11f);
            cpBoldItalic = base.deriveFont(Font.PLAIN, 13f);
        } catch (Exception e) {
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
            System.out.println("[GamePanel] Gagal memuat health images!");
        }
    }

    private void loadUIImages() {
        try {
            zombieCounterBG = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/zombieCounterBG.png"));
            buttonESprite1  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonE_sprite1.png"));
            buttonESprite2  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonE_sprite2.png"));
            buttonFSprite1  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonF_sprite1.png"));
            buttonFSprite2  = ImageIO.read(getClass().getResourceAsStream("/uiGraphics/button/buttonF_sprite2.png"));
        } catch (Exception e) {
            System.out.println("[GamePanel] Gagal memuat UI images!");
        }
    }

    // ── Floor switch ──────────────────────────────────────────────────────────

    public void requestFloorSwitch(int toFloor) {
        pendingFloorSwitch = true;
        pendingFloor       = toFloor;
    }

    private void applyFloorSwitch() {
        activeFloor = pendingFloor;
        pendingFloorSwitch = false;
        FloorState fs = getActiveFloor();
        System.out.println("[GamePanel] Pindah ke Floor " + activeFloor
                + " — zombie: " + fs.zombies.size()
                + " | chest: "  + fs.chests.size()
                + " | barrel: " + fs.barrels.size()
                + " | loot: "   + fs.droppedItems.size());
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

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

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
            if (timer >= 1_000_000_000) timer = 0;
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void update() {
        nearStair    = false;
        nearStairCol = -1;
        nearStairRow = -1;
        nearChest    = false;
        activeChest  = null;

        bloodStain.update();
        cChecker.checkNearStair();
        player.update();
        levelM.update();

        // Ambil snapshot list floor aktif untuk frame ini
        FloorState          fs            = getActiveFloor();
        ArrayList<Zombie>          zombies = fs.zombies;
        ArrayList<ExplosiveBarrel> barrels = fs.barrels;
        ArrayList<Items>           dropped = fs.droppedItems;
        ArrayList<Bullet>          bullets = fs.bullets;

        // ── Update barrels ────────────────────────────────────────────────────
        Iterator<ExplosiveBarrel> barrelIt = barrels.iterator();
        while (barrelIt.hasNext()) {
            ExplosiveBarrel b = barrelIt.next();
            if (b.isDestroyed) { barrelIt.remove(); continue; }
            b.update();
        }

        // ── Update & collision bullets ────────────────────────────────────────
        Iterator<Bullet> bIt = bullets.iterator();
        while (bIt.hasNext()) {
            Bullet b = bIt.next();
            if (!b.isActive()) { bIt.remove(); continue; }
            b.update();

            if (b.isActive()) {
                Rectangle bb = new Rectangle((int) b.getBounds().x, (int) b.getBounds().y, 8, 8);

                // Cek zombie
                for (Zombie z : zombies) {
                    if (!z.isDead()) {
                        Rectangle zb = new Rectangle(z.x + z.solidArea.x, z.y + z.solidArea.y,
                                z.solidArea.width, z.solidArea.height);
                        if (bb.intersects(zb)) {
                            b.hit(z);
                            soundManager.playSFX("flesh_hit");
                            break;
                        }
                    }
                }

                // Cek barrel
                if (b.isActive()) {
                    for (ExplosiveBarrel barrel : barrels) {
                        if (!barrel.isExploding && !barrel.isDestroyed) {
                            if (bb.intersects(barrel.solidArea)) {
                                b.hit(barrel);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // ── Update zombies ────────────────────────────────────────────────────
        Iterator<Zombie> zIt = zombies.iterator();
        while (zIt.hasNext()) {
            Zombie z = zIt.next();
            z.update();
            if (z.isDoneWithDeadAnim()) zIt.remove();
        }

        // ── Pickup dropped items (hanya floor aktif) ──────────────────────────
        Iterator<Items> itemIt = dropped.iterator();
        while (itemIt.hasNext()) {
            Items itm = itemIt.next();
            float dx   = player.x - itm.x;
            float dy   = player.y - itm.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < tileSize / 1.5) {
                itm.use(player);
                itemIt.remove();
                soundManager.playSFX("uiClick");
            }
        }

        // ── Terapkan floor switch ─────────────────────────────────────────────
        if (pendingFloorSwitch) applyFloorSwitch();

        // ── Interaksi tangga ──────────────────────────────────────────────────
        if (nearStair && keyH.ePressed) {
            tileM.switchFloor(nearStairCol, nearStairRow);
            keyH.ePressed = false;
        }

        // ── Interaksi chest ───────────────────────────────────────────────────
        for (Chest c : fs.chests) {
            if (!c.isOpened() && c.isPlayerNearby(player)) {
                nearChest   = true;
                activeChest = c;
                if (keyH.fPressed) {
                    c.open(player, this);
                    keyH.fPressed = false;
                }
                break;
            }
        }

        // ── Animasi tombol interaksi ──────────────────────────────────────────
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

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FloorState fs = getActiveFloor();

        tileM.draw(g2);
        for (Chest c           : fs.chests)       c.draw(g2);
        for (Items itm         : fs.droppedItems) itm.draw(g2, this);
        for (ExplosiveBarrel b : fs.barrels)       b.draw(g2);
        for (Zombie z          : fs.zombies)       z.draw(g2);
        player.draw(g2);
        for (Bullet b          : fs.bullets)       b.draw(g2, this);

        bloodStain.draw(g2);
        drawHUD(g2);
        g2.dispose();
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Health
        float hpRatio = (float) player.getHp() / player.getMaxHp();
        BufferedImage hpImg = getHealthImage(hpRatio);
        if (hpImg != null) g2.drawImage(hpImg, 10, screenHeight - 74, 64, 64, null);

        // Ammo
        int     ammo      = player.getWeapon().getAmmo();
        int     maxAmmo   = player.getWeapon().getMaxAmmo();
        boolean reloading = player.getWeapon().isReloading();
        String  wpnLabel  = player.getWeapon().getType().name();

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
            g2.drawString(wpnLabel, ammoBgX + 8, ammoBgY + 14);
            g2.setFont(cpBold);
            g2.setColor(Color.WHITE);
            g2.drawString(ammo + " / " + maxAmmo, ammoBgX + 52, ammoBgY + 19);
        }

        // Level
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 10, 110, 24, 8, 8);
        g2.setFont(cpItalic);   g2.setColor(new Color(200, 200, 200));
        g2.drawString("LVL", 18, 24);
        g2.setFont(cpBold);     g2.setColor(new Color(255, 230, 50));
        g2.drawString(String.valueOf(levelM.currentLevel), 52, 24);

        // Floor
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 38, 110, 24, 8, 8);
        g2.setFont(cpItalic);   g2.setColor(new Color(200, 200, 200));
        g2.drawString("FLOOR", 18, 52);
        g2.setFont(cpBold);     g2.setColor(new Color(150, 210, 255));
        g2.drawString(String.valueOf(tileM.currentFloor), 76, 52);

        // Zombie counter (total semua lantai)
        long total = floor[1].zombies.stream().filter(z -> !z.isDead()).count()
                   + floor[2].zombies.stream().filter(z -> !z.isDead()).count();
        String zombieStr = "[ ZOMBIE: " + total + " ]";
        g2.setFont(cpBoldItalic);
        FontMetrics fmz = g2.getFontMetrics();
        int zW      = fmz.stringWidth(zombieStr);
        int bgWidth = zW + 24, bgHeight = 32;
        int bgX = (screenWidht / 2) - (bgWidth / 2), bgY = 4;

        if (zombieCounterBG != null) g2.drawImage(zombieCounterBG, bgX, bgY, bgWidth, bgHeight, null);
        else { g2.setColor(new Color(0, 0, 0, 160)); g2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 8, 8); }

        g2.setColor(new Color(230, 80, 80));
        g2.drawString(zombieStr, (screenWidht / 2) - (zW / 2),
                bgY + ((bgHeight - fmz.getHeight()) / 2) + fmz.getAscent());

        if (nearStair) drawStairPrompt(g2);
        if (nearChest && activeChest != null) drawChestPrompt(g2);
    }

    private void drawStairPrompt(Graphics2D g2) {
        int sx = nearStairCol * tileSize - player.x + player.screenX;
        int sy = nearStairRow * tileSize - player.y + player.screenY;
        BufferedImage spr = (buttonAnimFrame == 0) ? buttonESprite1 : buttonESprite2;
        if (spr == null) return;
        g2.drawImage(spr, sx + tileSize / 2 - 10, sy - 26 + (buttonAnimFrame == 1 ? 4 : 0), 20, 20, null);
    }

    private void drawChestPrompt(Graphics2D g2) {
        int cx = (int) activeChest.getX() - player.x + player.screenX;
        int cy = (int) activeChest.getY() - player.y + player.screenY;
        BufferedImage spr = (buttonAnimFrame == 0) ? buttonFSprite1 : buttonFSprite2;
        if (spr == null) return;
        g2.drawImage(spr, cx + tileSize / 2 - 10, cy - 26 + (buttonAnimFrame == 1 ? 4 : 0), 20, 20, null);
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
