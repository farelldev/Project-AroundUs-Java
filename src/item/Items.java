package item;

import entity.Player;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class Items {
    public String name;
    public float x, y;
    public BufferedImage image;

    public abstract void use(Player player);

    public void setName(String name) {
        this.name = name;
    }

    public void loadImage(String fileName) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/items/usable/" + fileName + ".png"));
        } catch (Exception e) {
            System.out.println("Gagal memuat gambar item: " + fileName);
        }
    }

    // Method untuk menggambar item di lantai
    public void draw(Graphics2D g2, GamePanel gp) {
        if (image != null) {
            // Rumus Kamera Dunia
            int screenX = (int) x - gp.getPlayer().x + gp.getPlayer().screenX;
            int screenY = (int) y - gp.getPlayer().y + gp.getPlayer().screenY;

            // Gambar item sedikit lebih kecil dari ubin (misal: tileSize / 2) agar terlihat seperti barang jatuh
            int itemSize = gp.tileSize / 2;
            int offsetX = (gp.tileSize - itemSize) / 2; // Biar posisinya di tengah
            int offsetY = (gp.tileSize - itemSize) / 2;

            g2.drawImage(image, screenX + offsetX, screenY + offsetY, itemSize, itemSize, null);
        }
    }
}
