package combat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Bullet {
    private float x, y;
    private final float speed = 10f;
    private int dmg;
    private boolean isActive = true;
    private double angle;
    private double velX, velY;

    private BufferedImage image;

    public Bullet(float startX, float startY, float targetX, float targetY, int dmg) {
        this.x = startX;
        this.y = startY;
        this.dmg = dmg;

        this.angle = Math.atan2(targetY - startY, targetX - startX);
        this.velX = speed * Math.cos(angle);
        this.velY = speed * Math.sin(angle);

        getBulletImage();
    }

    public void getBulletImage() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/weapon/bullet/bullet.png"));
            System.out.println("Gambar terbaca");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal memuat gambar peluru!");
        }
    }

    public void update() {
        if (isActive) {
            // peluru bergerak
            x += (float) velX;
            y += (float) velY;

            // DRAFT: ukuran frame 800 x 600
            if (x < 0 || x > 800 || y < 0 || y > 600) {
                deactivate();
            }
        }
    }

    public void hit(Damageable target) {
        target.takeDmg(this.dmg);
        deactivate();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public Rectangle getBounds() {
        // DRAFT: ukuran peluru 5 piksel
        return new Rectangle((int) x, (int) y, 5, 5);
    }

    public void draw(Graphics2D g2) {
        if (!isActive) return;

        if (image != null) {
            AffineTransform oldTransform = g2.getTransform();

            g2.translate(x, y);
            g2.rotate(angle);

            float scale = 0.5f;
            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);

            g2.drawImage(image, -width / 2, -height / 2, width, height, null);

            g2.setTransform(oldTransform);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int) x, (int) y, 8, 8);
        }
    }

    public boolean isActive() {
        return isActive;
    }
}