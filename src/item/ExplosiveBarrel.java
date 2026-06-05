package item;

import combat.Damageable;
import entity.Zombie;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ExplosiveBarrel implements Damageable {
    GamePanel gp;
    public int x, y;
    public Rectangle solidArea;

    // Asumsi damage peluru = 30. Agar 3 kali tembak meledak, HP = 90.
    private int hp = 90;
    public boolean isExploding = false;
    public boolean isDestroyed = false;

    // Animasi
    private BufferedImage idleImg;
    private BufferedImage[] explodeImgs;
    private int spriteCounter = 0;
    private int spriteNum = 0;

    public ExplosiveBarrel(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;

        // Hitbox tong agar bisa ditabrak peluru
        this.solidArea = new Rectangle(x, y, gp.tileSize, gp.tileSize);

        loadImages();
    }

    private void loadImages() {
        try {
            // Pastikan path dan huruf besarnya sesuai dengan foldermu
            idleImg = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel.png"));

            explodeImgs = new BufferedImage[7];
            explodeImgs[0] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explode1.png"));
            explodeImgs[1] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explode2.png"));
            explodeImgs[2] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explode3.png"));
            explodeImgs[3] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explode4.png"));
            explodeImgs[4] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explode5.png"));
            explodeImgs[5] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explode6.png"));
            explodeImgs[6] = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel_explodeEnd.png"));
        } catch (Exception e) {
            System.out.println("[Barrel] Gagal memuat gambar tong meledak!");
        }
    }

    @Override
    public void takeDmg(int dmg) {
        // Kalau sudah mati atau sedang meledak, kebal dari peluru tambahan
        if (isExploding || isDestroyed) return;

        hp -= dmg;
        if (hp <= 0) {
            isExploding = true;
            explode();
        }
    }

    private void explode() {
        System.out.println("BBOOMM! Tong meledak!");

        // Membuat area ledakan 3x3 Tile (Pusatnya adalah tong ini)
        int radius = gp.tileSize;
        Rectangle explosionArea = new Rectangle(x - radius, y - radius, gp.tileSize * 3, gp.tileSize * 3);

        // 1. Berikan damage ke Player jika masuk area
        Rectangle playerHitbox = new Rectangle(gp.getPlayer().x, gp.getPlayer().y, gp.tileSize, gp.tileSize);
        if (explosionArea.intersects(playerHitbox)) {
            gp.getPlayer().takeDmg(30);
            System.out.println("Kena ledakan sendiri!");
        }

        // 2. Berikan damage ke semua Zombie yang masuk area
        for (Zombie z : gp.getActiveZombies()) {
            if (!z.isDead()) {
                Rectangle zombieHitbox = new Rectangle(z.x + z.solidArea.x, z.y + z.solidArea.y, z.solidArea.width, z.solidArea.height);
                if (explosionArea.intersects(zombieHitbox)) {
                    z.takeDmg(30);
                }
            }
        }
    }

    public void update() {
        // Memutar animasi 7 frame berturut-turut lalu hilang
        if (isExploding && !isDestroyed) {
            spriteCounter++;
            if (spriteCounter > 5) { // Atur kecepatan animasi meledaknya di sini (makin kecil makin cepat)
                spriteNum++;
                spriteCounter = 0;
                if (spriteNum >= explodeImgs.length) {
                    isDestroyed = true; // Tandai selesai agar dihapus oleh GamePanel
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        // Rumus kamera
        int screenX = x - gp.getPlayer().x + gp.getPlayer().screenX;
        int screenY = y - gp.getPlayer().y + gp.getPlayer().screenY;

        if (!isExploding) {
            if (idleImg != null) g2.drawImage(idleImg, screenX, screenY, gp.tileSize, gp.tileSize, null);
        } else if (!isDestroyed) {
            if (explodeImgs[spriteNum] != null) {
                int shakeX = (int) (Math.random() * 4 - 2);
                int shakeY = (int) (Math.random() * 4 - 2);
                g2.drawImage(explodeImgs[spriteNum], screenX + shakeX, screenY + shakeY, gp.tileSize, gp.tileSize, null);
            }
        }
    }
}