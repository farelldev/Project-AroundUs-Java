package combat;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Bullet extends Entity {

    GamePanel gp;

    private float preciseX, preciseY;

    private int dmg;
    private boolean isActive = true;

    private double angle;
    private double velX, velY;

    private BufferedImage image;

    public Bullet(GamePanel gp, float startX, float startY,
                  float targetX, float targetY, int dmg) {

        this.gp = gp;

        this.preciseX = startX;
        this.preciseY = startY;

        this.x = (int) startX;
        this.y = (int) startY;

        this.dmg = dmg;

        speed = 20;

        solidArea = new Rectangle(0, 0, 8, 8);

        this.angle = Math.atan2(targetY - startY, targetX - startX);

        this.velX = speed * Math.cos(angle);
        this.velY = speed * Math.sin(angle);

        // arah bullet untuk collision
        if (Math.abs(velX) > Math.abs(velY)) {
            direction = velX > 0 ? "right" : "left";
        } else {
            direction = velY > 0 ? "down" : "up";
        }

        getBulletImage();
    }

    public void getBulletImage() {
        try {
            image = ImageIO.read(
                    getClass().getResourceAsStream("/weapon/bullet/bullet.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {

        if (!isActive) return;

        collisionOn = false;

        // cek collision tile
        gp.cChecker.checkTile(this);

        // kalau nabrak wall
        if (collisionOn) {
            deactivate();
            return;
        }

        preciseX += velX;
        preciseY += velY;

        x = (int) preciseX;
        y = (int) preciseY;

        // keluar map
        if (x < 0 || y < 0
                || x > gp.maxscreenCol * gp.tileSize
                || y > gp.maxscreenRow * gp.tileSize) {

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

    public void draw(Graphics2D g2) {

        if (!isActive) return;

        if (image != null) {

            AffineTransform oldTransform = g2.getTransform();

            g2.translate(x, y);
            g2.rotate(angle);

            float scale = 0.35f;

            int width = (int)(image.getWidth() * scale);
            int height = (int)(image.getHeight() * scale);

            g2.drawImage(
                    image,
                    -width / 2,
                    -height / 2,
                    width,
                    height,
                    null
            );

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