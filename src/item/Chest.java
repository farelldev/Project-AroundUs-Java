package item;

import entity.Player;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Chest — peti harta yang bisa dibuka player.
 *
 * PERUBAHAN:
 *  - Mendukung loot pool yang berisi AK47 mulai wave 3.
 *  - Sound pickup diputar saat peti dibuka (via GamePanel.soundManager).
 */
public class Chest {
    private GamePanel gp;
    private float x;
    private float y;
    private Items content;
    private boolean isOpened;
    private static final int INTERACTION_RANGE = 50;

    private BufferedImage imageClosed, imageOpened;

    // Tingkat wave untuk menentukan loot pool
    private int waveLevel = 1;

    public Chest(GamePanel gp, float x, float y) {
        this.gp       = gp;
        this.x        = x;
        this.y        = y;
        this.isOpened = false;
        this.content  = generateRandomItem();
        loadImages();
    }

    /** Panggil ini sebelum chest pertama kali dibuka jika level >= 3 */
    public void setLootPool(int currentLevel) {
        this.waveLevel = currentLevel;
        // Re-roll konten dengan pool baru
        this.content = generateRandomItem();
    }

    private void loadImages() {
        try {
            imageClosed = ImageIO.read(getClass().getResourceAsStream("/items/chest/chest_closed.png"));
            imageOpened = ImageIO.read(getClass().getResourceAsStream("/items/chest/chest_opened.png"));
        } catch (Exception e) {
            System.out.println("[Chest] Gagal memuat gambar chest!");
        }
    }

    private Items generateRandomItem() {
        Random random = new Random();

        // Wave 1-2: hanya bandage & ammo
        if (waveLevel < 3) {
            int roll = random.nextInt(3);
            if (roll == 0) return new Bandage(20);
            if (roll == 1) return new Bandage(10);
            return new Ammo();
        }

        // Wave 3+: ada kemungkinan dapat AK47 (20%)
        int roll = random.nextInt(10);
        if (roll == 0 || roll == 1) {
            // 20% → AK47
            return new AK47Item(gp);
        } else if (roll < 5) {
            return new Bandage(20);
        } else if (roll < 8) {
            return new Ammo();
        } else {
            return new Bandage(10);
        }
    }

    public boolean isPlayerNearby(Player player) {
        float dx       = player.x - this.x;
        float dy       = player.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= INTERACTION_RANGE;
    }

    public void open(Player player, GamePanel gp) {
        if (!isOpened) {
            if (isPlayerNearby(player)) {
                isOpened = true;

                content.x = this.x;
                content.y = this.y + (gp.tileSize / 2f);
                gp.droppedItems.add(content);

                // Suara buka peti
                gp.soundManager.playSFX("uiClick");

                System.out.println("[Chest] Dibuka! " + content.name + " jatuh ke lantai.");
            } else {
                System.out.println("[Chest] Terlalu jauh dari peti!");
            }
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = (int) x - gp.getPlayer().x + gp.getPlayer().screenX;
        int screenY = (int) y - gp.getPlayer().y + gp.getPlayer().screenY;

        BufferedImage img = isOpened ? imageOpened : imageClosed;
        if (img != null) {
            g2.drawImage(img, screenX, screenY, gp.tileSize, gp.tileSize, null);
        } else {
            g2.setColor(isOpened ? Color.YELLOW : new Color(139, 69, 19));
            g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
        }
    }

    public float getX()        { return x; }
    public float getY()        { return y; }
    public boolean isOpened()  { return isOpened; }
    public Items getContent()  { return content; }
}
