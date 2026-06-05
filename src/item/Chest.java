package item;

import entity.Player;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Chest {
    private GamePanel gp;
    private float x;
    private float y;
    private Items content;
    private boolean isOpened;
    private static final int INTERACTION_RANGE = 50;

    // Gambar Peti
    private BufferedImage imageClosed, imageOpened;

    // Tambahkan GamePanel di konstruktor
    public Chest(GamePanel gp, float x, float y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.isOpened = false;
        this.content = generateRandomItem();

        loadImages();
    }

    private void loadImages() {
        try {
            imageClosed = ImageIO.read(getClass().getResourceAsStream("/items/chest/chest_closed.png"));
            imageOpened = ImageIO.read(getClass().getResourceAsStream("/items/chest/chest_opened.png"));
        } catch (Exception e) {
            System.out.println("Gagal memuat gambar chest!");
        }
    }

    private Items generateRandomItem() {
        Random random = new Random();
        int roll = random.nextInt(3);

        if (roll == 0) {
            return new Bandage(20);
        } else if (roll == 1) {
            return new Bandage(10);
        } else {
            return new Ammo();
        }
    }

    public boolean isPlayerNearby(Player player) {
        float dx = player.x - this.x;
        float dy = player.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= INTERACTION_RANGE;
    }

    public void open(Player player, GamePanel gp) { // Tambahkan parameter gp
        if (!isOpened) {
            if (isPlayerNearby(player)) {
                isOpened = true;

                // Lempar item ke tanah (geser sedikit ke bawah peti agar visualnya tidak tertumpuk)
                content.x = this.x;
                content.y = this.y + (gp.tileSize / 2);

                // Masukkan item ke daftar barang jatuh di GamePanel
                gp.droppedItems.add(content);

                System.out.println("Chest dibuka! " + content.name + " jatuh ke lantai.");
            } else {
                System.out.println("Kamu terlalu jauh dari chest!");
            }
        }
    }

    // Fungsi menggambar peti dengan Rumus Kamera Dunia
    public void draw(Graphics2D g2) {
        int screenX = (int) x - gp.getPlayer().x + gp.getPlayer().screenX;
        int screenY = (int) y - gp.getPlayer().y + gp.getPlayer().screenY;

        BufferedImage img = isOpened ? imageOpened : imageClosed;

        if (img != null) {
            g2.drawImage(img, screenX, screenY, gp.tileSize, gp.tileSize, null);
        } else {
            // Fallback kalau gambar error (Kotak coklat/kuning)
            g2.setColor(isOpened ? Color.YELLOW : new Color(139, 69, 19));
            g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isOpened() { return isOpened; }
    public Items getContent() { return content; }
}