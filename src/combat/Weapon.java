package combat;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Weapon {
    private GamePanel gp;
    private float weaponX, weaponY;
    private int ammo = 30; // Contoh amunisi awal
    private int damage = 10;
    private double angle;

    private BufferedImage idleImage, shoot1, shoot2, shoot3;
    private boolean isShooting = false;
    private int spriteCounter = 0;
    private int spriteNum = 1;

    private int shootCooldown = 0;
    private final int SHOT_DELAY = 30; // 0.5 detik

    public Weapon(GamePanel gp){
        this.gp = gp;
        getWeaponImage();
    }

    public void getWeaponImage() {
        try {
            idleImage = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_Idle.png"));
            shoot1    = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_shoot1.png"));
            shoot2    = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_shoot2.png"));
            shoot3    = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_shoot3.png"));
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Gagal memuat gambar senjata!");
        }
    }

    public float getX() {
        return weaponX;
    }

    public float getY() {
        return weaponY;
    }

    public Bullet shoot(float startX, float startY, float targetX, float targetY) {
        if (shootCooldown > 0 || isShooting) {
            return null; // Mengembalikan null tanda tembakan gagal
        }

        if (ammo > 0) {
            ammo--;

            isShooting = true;
            spriteNum = 1;
            spriteCounter = 0;

            shootCooldown = SHOT_DELAY;

            float gunLength = 25f;

            float muzzleX = startX + (float) (gunLength * Math.cos(angle));
            float muzzleY = startY + (float) (gunLength * Math.sin(angle));

            return new Bullet(muzzleX, muzzleY, targetX, targetY, damage);
        }
        System.out.println("Klik! Peluru habis.");
        return null;
    }

    public void update(int playerCenterX, int playerCenterY) {
        int targetX = gp.getKeyH().mouseX;
        int targetY = gp.getKeyH().mouseY;

        angle = Math.atan2(targetY - playerCenterY, targetX - playerCenterX);

        int orbitRadius = 25;
        weaponX = (float) (playerCenterX + orbitRadius * Math.cos(angle));
        weaponY = (float) (playerCenterY + orbitRadius * Math.sin(angle));

        if (shootCooldown > 0) {
            shootCooldown--;
        }

        if (isShooting) {
            spriteCounter++;
            if (spriteCounter > 5) {
                spriteNum++;
                spriteCounter = 0;

                if (spriteNum > 3) {
                    spriteNum = 1;
                    isShooting = false;
                }
            }
        }
    }

    public void addAmmo(int amount) {
        this.ammo += amount;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = idleImage;
        if (isShooting) {
            if (spriteNum == 1) image = shoot1;
            else if (spriteNum == 2) image = shoot2;
            else if (spriteNum == 3) image = shoot3;
        }

        if (image != null) {
            AffineTransform oldTransform = g2.getTransform();

            g2.translate(weaponX, weaponY);
            g2.rotate(angle);

            g2.scale(-1, 1);

            if (Math.abs(angle) > Math.PI / 2) {
                g2.scale(1, -1);
            }

            float scale = 0.4f;
            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);

            g2.drawImage(image, -width / 2, -height / 2, width, height, null);
            g2.setTransform(oldTransform);
        } else {
            AffineTransform oldTransform = g2.getTransform();
            g2.translate(weaponX, weaponY);
            g2.rotate(angle);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, -5, 20, 10);
            g2.setTransform(oldTransform);
        }
    }
}
