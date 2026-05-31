package combat;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

// Wajib extends Entity agar bisa masuk ke CollisionCheck buatan temenmu
public class Bullet extends Entity {

    GamePanel gp;

    // Pakai precise untuk pergerakan float yang mulus
    private float preciseX, preciseY;
    private int dmg;
    private boolean isActive = true;

    private double angle;
    private double velX, velY;

    private BufferedImage image;

    // Konstruktor disesuaikan dengan versimu (langsung pakai angle)
    public Bullet(GamePanel gp, float startX, float startY, double angle, int dmg) {
        this.gp = gp;

        this.preciseX = startX;
        this.preciseY = startY;
        this.x = (int) startX;
        this.y = (int) startY;

        this.dmg = dmg;
        this.angle = angle;

        // Speed diwarisi dari Entity
        speed = 20;

        // Hitbox peluru untuk nabrak tembok
        solidArea = new Rectangle(0, 0, 8, 8);

        this.velX = speed * Math.cos(angle);
        this.velY = speed * Math.sin(angle);

        // Menentukan arah (direction) untuk sistem CollisionCheck temenmu
        if (Math.abs(velX) > Math.abs(velY)) {
            direction = velX > 0 ? "right" : "left";
        } else {
            direction = velY > 0 ? "down" : "up";
        }

        getBulletImage();
    }

    public void getBulletImage() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/weapon/bullet/bullet.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal memuat gambar peluru!");
        }
    }

    public void update() {
        if (!isActive) return;

        collisionOn = false;

        // Cek tabrakan dengan tembok (fitur temenmu)
        gp.cChecker.checkTile(this);

        // Kalau nabrak wall, peluru langsung hancur
        if (collisionOn) {
            deactivate();
            return;
        }

        preciseX += velX;
        preciseY += velY;

        x = (int) preciseX;
        y = (int) preciseY;

        // Batas keluar map yang lebih dinamis menyesuaikan ukuran dunia
        if (x < 0 || y < 0 ||
                x > gp.maxWorldCol * gp.tileSize ||
                y > gp.maxWorldRow * gp.tileSize) {
            deactivate();
        }
    }

    public void hit(Damageable target) {
        target.takeDmg(this.dmg);
        deactivate();
    }

    public void deactivate() {
        isActive = false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 8, 8);
    }

    // Menggunakan fungsi draw milikmu dengan logika Kamera (screenX/Y)
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!isActive) return;

        if (image != null) {
            AffineTransform oldTransform = g2.getTransform();

            // RUMUS KAMERA DUNIA
            float screenBulletX = x - gp.getPlayer().x + gp.getPlayer().screenX;
            float screenBulletY = y - gp.getPlayer().y + gp.getPlayer().screenY;

            g2.translate(screenBulletX, screenBulletY);
            g2.rotate(angle);

            float scale = 0.35f;
            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);

            g2.drawImage(image, -width / 2, -height / 2, width, height, null);

            g2.setTransform(oldTransform);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval(x, y, 8, 8);
        }
    }

    public boolean isActive() {
        return isActive;
    }
}