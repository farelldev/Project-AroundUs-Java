package main;

import combat.Bullet;
import entity.Player;
import entity.Zombie;
import item.Chest;
import item.ExplosiveBarrel;
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

    // ===== Chest & Barrel TERPISAH per lantai =====
    public final ArrayList<Chest>           chestsFloor1  = new ArrayList<>();
    public final ArrayList<Chest>           chestsFloor2  = new ArrayList<>();
    public final ArrayList<ExplosiveBarrel> barrelsFloor1 = new ArrayList<>();
    public final ArrayList<ExplosiveBarrel> barrelsFloor2 = new ArrayList<>();

    /** Dropped items tetap global (item jatuh di mana player berada) */
    public ArrayList<item.Items> droppedItems = new ArrayList<>();

    public TileManager    tileM    = new TileManager(this);
    public CollisionCheck cChecker = new CollisionCheck(this);
    public LevelManager   levelM   = new LevelManager(this);

    /** SoundManager terpusat — dipakai oleh semua class */
    public final SoundManager soundManager = new SoundManager();

    Player player = new Player(this, this.keyH);
    public ArrayList<Bullet> bullets = new ArrayList<>();

    // Zombie per floor
    public final ArrayList<Zombie> zombiesFloor1 = new ArrayList<>();
    public final ArrayList<Zombie> zombiesFloor2 = new ArrayList<>();

    public int activeFloor = 1;
    public ArrayList<Zombie> zombies = zombiesFloor1; // legacy compat

    // Flag pindah floor
    private boolean pendingFloorSwitch = false;
    private int     pendingFloor       = -1;

    // Flag deteksi tangga
    public boolean nearStair    = false;
    public int     nearStairCol = -1;
    public int     nearStairRow = -1;

    // Deteksi chest
    private BufferedImage buttonFSprite1, buttonFSprite2;
    public boolean nearChest  = false;
    public Chest   activeChest = null;

    // UI Images
    private BufferedImage hpImg100, hpImg75, hpImg50, hpImg25, hpImg10;
    private BufferedImage zombieCounterBG;
    private BufferedImage buttonESprite1, buttonESprite2;
    private int buttonAnimCounter = 0;
    private int buttonAnimFrame   = 0;

    // Fonts
    private Font cpRegular, cpBold, cpItalic, cpBoldItalic;

    // ===== Compat helpers untuk LevelManager =====
    /** Chest aktif di floor yang sedang aktif */
    public ArrayList<Chest> getActiveChests() {
        return activeFloor == 1 ? chestsFloor1 : chestsFloor2;
    }
    /** Barrel aktif di floor yang sedang aktif */
    public ArrayList<ExplosiveBarrel> getActiveBarrels() {
        return activeFloor == 1 ? barrelsFloor1 : barrelsFloor2;
    }

    // Legacy field — tidak dipakai tapi tetap ada agar tidak error compile lama
    /** @deprecated Gunakan getActiveChests() */
    @Deprecated public ArrayList<Chest>           chests  = chestsFloor1;
    /** @deprecated Gunakan getActiveBarrels() */
    @Deprecated public final ArrayList<ExplosiveBarrel> barrels = barrelsFloor1;

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
        loadUIImages();
        loadFonts();

        // Mainkan BGM intro saat game mulai
        soundManager.playBGM("beforePlay");
    }

    private void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            InputStream isReg = getClass().getResourceAsStream("/fonts/NineByFiveNbp-MypB.ttf");
            Font base = Font.createFont(Font.TRUETYPE_FONT, isReg);
            ge.registerFont(base);
            cpRegular    = base.deriveFont(Font.PLAIN,  12f);
            cpBold       = base.deriveFont(Font.PLAIN,  13f);
            cpItalic     = base.deriveFont(Font.PLAIN,  11f);
            cpBoldItalic = base.deriveFont(Font.PLAIN,  13f);
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
                + " — zombie: " + getActiveZombies().size()
                + " | chest: " + getActiveChests().size()
                + " | barrel: " + getActiveBarrels().size());
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
            if (timer >= 1_000_000_000) {
                timer = 0;
            }
        }
    }

    public void update() {
        // Reset flag tangga & chest
        nearStair    = false;
        nearStairCol = -1;
        nearStairRow = -1;
        nearChest    = false;
        activeChest  = null;

        cChecker.checkNearStair();
        player.update();
        levelM.update();

        ArrayList<Zombie>           activeZombies = getActiveZombies();
        ArrayList<ExplosiveBarrel>  activeBarrels = getActiveBarrels();

        // ===== Update barrel di floor aktif saja =====
        Iterator<ExplosiveBarrel> barrelIt = activeBarrels.iterator();
        while (barrelIt.hasNext()) {
            ExplosiveBarrel barrel = barrelIt.next();
            if (barrel.isDestroyed) { barrelIt.remove(); continue; }
            barrel.update();
        }

        // ===== Update bullets =====
        Iterator<Bullet> bIt = bullets.iterator();
        while (bIt.hasNext()) {
            Bullet b = bIt.next();
            if (!b.isActive()) { bIt.remove(); continue; }
            b.update();

            if (b.isActive()) {
                Rectangle bulletBounds = new Rectangle((int)b.getBounds().x, (int)b.getBounds().y, 8, 8);

                // Cek tembak zombie
                for (Zombie z : activeZombies) {
                    if (!z.isDead()) {
                        Rectangle zombieBounds = new Rectangle(
                                z.x + z.solidArea.x,
                                z.y + z.solidArea.y,
                                z.solidArea.width,
                                z.solidArea.height);
                        if (bulletBounds.intersects(zombieBounds)) {
                            b.hit(z);
                            soundManager.playSFX("flesh_hit");
                            break;
                        }
                    }
                }

                // Cek tembak barrel (hanya floor aktif)
                if (b.isActive()) {
                    for (ExplosiveBarrel barrel : activeBarrels) {
                        if (!barrel.isExploding && !barrel.isDestroyed) {
                            if (bulletBounds.intersects(barrel.solidArea)) {
                                b.hit(barrel);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // ===== Update zombie =====
        Iterator<Zombie> zIt = activeZombies.iterator();
        while (zIt.hasNext()) {
            Zombie z = zIt.next();
            z.update();
            if (z.isDoneWithDeadAnim()) zIt.remove();
        }

        // ===== Pickup dropped items =====
        Iterator<item.Items> itemIt = droppedItems.iterator();
        while (itemIt.hasNext()) {
            item.Items itm = itemIt.next();
            float dx = player.x - itm.x;
            float dy = player.y - itm.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < tileSize / 1.5) {
                itm.use(player);
                itemIt.remove();
                soundManager.playSFX("uiClick");
            }
        }

        // ===== Terapkan pindah floor =====
        if (pendingFloorSwitch) {
            applyFloorSwitch();
        }

        // ===== Interaksi tangga =====
        if (nearStair && keyH.ePressed) {
            tileM.switchFloor(nearStairCol, nearStairRow);
            keyH.ePressed = false;
        }

        // ===== Interaksi chest (hanya floor aktif) =====
        ArrayList<Chest> activeChests = getActiveChests();
        for (Chest c : activeChests) {
            if (!c.isOpened() && c.isPlayerNearby(player)) {
                nearChest  = true;
                activeChest = c;

                if (keyH.fPressed) {
                    c.open(player, this);
                    keyH.fPressed = false;
                    soundManager.playSFX("uiClick");
                }
                break;
            }
        }

        // ===== Animasi tombol interaksi =====
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

        // Gambar hanya chest & barrel floor aktif
        for (Chest c : getActiveChests())       { c.draw(g2); }
        for (item.Items itm : droppedItems)     { itm.draw(g2, this); }
        for (ExplosiveBarrel barrel : getActiveBarrels()) { barrel.draw(g2); }
        for (Zombie z : getActiveZombies())     { z.draw(g2); }
        player.draw(g2);
        for (Bullet b : bullets)                { b.draw(g2, this); }

        drawHUD(g2);
        g2.dispose();
    }

    private void drawHUD(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ---- HEALTH BAR ----
        float hpRatio = (float) player.getHp() / player.getMaxHp();
        BufferedImage hpImg = getHealthImage(hpRatio);
        if (hpImg != null) {
            g2.drawImage(hpImg, 10, screenHeight - 74, 64, 64, null);
        }

        // ---- AMMO ----
        int ammo      = player.getWeapon().getAmmo();
        int maxAmmo   = player.getWeapon().getMaxAmmo();
        boolean reloading = player.getWeapon().isReloading();
        String weaponLabel = player.getWeapon().getType().name();

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
            g2.drawString(weaponLabel, ammoBgX + 8, ammoBgY + 14);
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

        // ---- ZOMBIE COUNTER ----
        long aliveF1 = zombiesFloor1.stream().filter(z -> !z.isDead()).count();
        long aliveF2 = zombiesFloor2.stream().filter(z -> !z.isDead()).count();
        long total   = aliveF1 + aliveF2;

        String zombieStr = "[ ZOMBIE: " + total + " ]";
        g2.setFont(cpBoldItalic);
        FontMetrics fmz = g2.getFontMetrics();
        int zW      = fmz.stringWidth(zombieStr);
        int bgWidth = zW + 24;
        int bgHeight = 32;
        int bgX = (screenWidht / 2) - (bgWidth / 2);
        int bgY = 4;

        if (zombieCounterBG != null) {
            g2.drawImage(zombieCounterBG, bgX, bgY, bgWidth, bgHeight, null);
        } else {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 8, 8);
        }

        int textX = (screenWidht / 2) - (zW / 2);
        int textY = bgY + ((bgHeight - fmz.getHeight()) / 2) + fmz.getAscent();
        g2.setColor(new Color(230, 80, 80));
        g2.drawString(zombieStr, textX, textY);

        // ---- PROMPT ----
        if (nearStair) drawStairPrompt(g2);
        if (nearChest && activeChest != null) drawChestPrompt(g2);
    }

    private void drawStairPrompt(Graphics2D g2) {
        int stairWorldX  = nearStairCol * tileSize;
        int stairWorldY  = nearStairRow * tileSize;
        int stairScreenX = stairWorldX - player.x + player.screenX;
        int stairScreenY = stairWorldY - player.y + player.screenY;

        BufferedImage btnSprite = (buttonAnimFrame == 0) ? buttonESprite1 : buttonESprite2;
        if (btnSprite == null) return;

        int btnW = 20, btnH = 20;
        int btnX = stairScreenX + (tileSize / 2) - (btnW / 2);
        int btnY = stairScreenY - btnH - 6 + (buttonAnimFrame == 1 ? 4 : 0);
        g2.drawImage(btnSprite, btnX, btnY, btnW, btnH, null);
    }

    private void drawChestPrompt(Graphics2D g2) {
        int chestScreenX = (int) activeChest.getX() - player.x + player.screenX;
        int chestScreenY = (int) activeChest.getY() - player.y + player.screenY;

        BufferedImage btnSprite = (buttonAnimFrame == 0) ? buttonFSprite1 : buttonFSprite2;
        if (btnSprite == null) return;

        int btnW = 20, btnH = 20;
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
