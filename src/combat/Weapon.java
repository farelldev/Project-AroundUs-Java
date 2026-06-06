package combat;

import entity.Player;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Weapon {
    private GamePanel gp;
    private float weaponX, weaponY;

    // ===== Tipe senjata =====
    public enum WeaponType { DEAGLE, AK47 }
    private WeaponType currentType = WeaponType.DEAGLE;

    // ===== Stats Deagle =====
    private static final int DEAGLE_MAX_AMMO  = 7;
    private static final int DEAGLE_DAMAGE    = 30;
    private static final int DEAGLE_SHOT_DELAY = 30;

    // ===== Stats AK47 =====
    private static final int AK_MAX_AMMO   = 30;
    private static final int AK_DAMAGE     = 20;
    private static final int AK_SHOT_DELAY = 8;

    // ===== State aktif =====
    private int ammo;
    private int maxAmmo;
    private int damage;
    private int SHOT_DELAY;

    private double angle;
    private boolean isShooting   = false;
    private int spriteCounter    = 0;
    private int spriteNum        = 1;
    private int shootCooldown    = 0;

    // ===== Reload =====
    private boolean isReloading  = false;
    private int reloadTimer      = 0;
    private final int RELOAD_DURATION = 120;
    private final double RELOAD_ANGLE = Math.PI / 2 + Math.PI / 4;

    // ===== Gambar Deagle =====
    private BufferedImage dg_idle, dg_shoot1, dg_shoot2, dg_shoot3;

    // ===== Gambar AK47 =====
    private BufferedImage ak_idle, ak_shoot1, ak_shoot2;

    public Weapon(GamePanel gp) {
        this.gp = gp;
        loadImages();
        applyWeaponStats();
    }

    private void loadImages() {
        try {
            dg_idle   = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_Idle.png"));
            dg_shoot1 = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_shoot1.png"));
            dg_shoot2 = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_shoot2.png"));
            dg_shoot3 = ImageIO.read(getClass().getResourceAsStream("/weapon/deagle/DG_shoot3.png"));
        } catch (Exception e) {
            System.out.println("[Weapon] Gagal load gambar Deagle!");
        }
        try {
            ak_idle   = ImageIO.read(getClass().getResourceAsStream("/weapon/ak47/ak_1.png"));
            ak_shoot1 = ImageIO.read(getClass().getResourceAsStream("/weapon/ak47/ak_shoot1.png"));
            ak_shoot2 = ImageIO.read(getClass().getResourceAsStream("/weapon/ak47/ak_shoot2.png"));
        } catch (Exception e) {
            System.out.println("[Weapon] Gagal load gambar AK47!");
        }
    }

    /** Terapkan stats sesuai senjata aktif */
    private void applyWeaponStats() {
        if (currentType == WeaponType.DEAGLE) {
            maxAmmo    = DEAGLE_MAX_AMMO;
            damage     = DEAGLE_DAMAGE;
            SHOT_DELAY = DEAGLE_SHOT_DELAY;
        } else {
            maxAmmo    = AK_MAX_AMMO;
            damage     = AK_DAMAGE;
            SHOT_DELAY = AK_SHOT_DELAY;
        }
        ammo = maxAmmo;
    }

    /** Dipanggil saat player memungut item AK47 */
    public void switchToAK47() {
        currentType = WeaponType.AK47;
        applyWeaponStats();
        isReloading  = false;
        isShooting   = false;
        shootCooldown = 0;
        System.out.println("[Weapon] Ganti ke AK47! Ammo: " + ammo + "/" + maxAmmo);
    }

    // ===== Getter =====
    public float getX()          { return weaponX; }
    public float getY()          { return weaponY; }
    public boolean isReloading() { return isReloading; }
    public int getAmmo()         { return ammo; }
    public int getMaxAmmo()      { return maxAmmo; }
    public double getAngle()     { return angle; }
    public WeaponType getType()  { return currentType; }

    public void startReload() {
        if (isReloading) return;
        if (ammo >= maxAmmo) return;
        isReloading = true;
        reloadTimer = 0;
        isShooting  = false;
        gp.soundManager.playSFX("reload");
        System.out.println("[Weapon] Reloading...");
    }

    public Bullet shoot(float startX, float startY, float targetX, float targetY) {
        if (shootCooldown > 0 || isShooting || isReloading) return null;

        if (ammo > 0) {
            ammo--;
            isShooting    = true;
            spriteNum     = 1;
            spriteCounter = 0;
            shootCooldown = SHOT_DELAY;

            // Sound tembak sesuai senjata
            if (currentType == WeaponType.DEAGLE) {
                gp.soundManager.playSFX("deagle_shot");
            } else {
                gp.soundManager.playSFX("ak_shot");
            }

            float gunLength = 25f;
            float muzzleX   = startX + (float)(gunLength * Math.cos(angle));
            float muzzleY   = startY + (float)(gunLength * Math.sin(angle));
            return new Bullet(gp, muzzleX, muzzleY, angle, damage);

        } else {
            // Hanya bunyi klik sekali tiap ~0.5 detik saat peluru habis
            if (shootCooldown <= 0) {
                gp.soundManager.playSFX("empty_mag");
                shootCooldown = 30; // 0.5 detik di 60 FPS
            }
        }
        return null;
    }

    public void update(Player player) {
        int playerWorldCenterX = player.x + (gp.tileSize / 2);
        int playerWorldCenterY = player.y + (gp.tileSize / 2);
        int orbitRadius = 30;

        // == Proses Reload ==
        if (isReloading) {
            reloadTimer++;
            angle   = RELOAD_ANGLE;
            weaponX = (float)(playerWorldCenterX + orbitRadius * Math.cos(angle));
            weaponY = (float)(playerWorldCenterY + Math.sin(angle));

            if (reloadTimer >= RELOAD_DURATION) {
                ammo        = maxAmmo;
                isReloading = false;
                reloadTimer = 0;
                System.out.println("[Weapon] Reload selesai! Ammo: " + ammo + "/" + maxAmmo);
            }
            return;
        }

        // == Update Normal (Ikut Mouse) ==
        int playerScreenCenterX = player.screenX + (gp.tileSize / 2);
        int playerScreenCenterY = player.screenY + (gp.tileSize / 2);

        int mouseX = gp.getKeyH().mouseX;
        int mouseY = gp.getKeyH().mouseY;

        angle   = Math.atan2(mouseY - playerScreenCenterY, mouseX - playerScreenCenterX);
        weaponX = (float)(playerWorldCenterX + orbitRadius * Math.cos(angle));
        weaponY = (float)(playerWorldCenterY + orbitRadius * Math.sin(angle));

        if (shootCooldown > 0) shootCooldown--;

        if (isShooting) {
            spriteCounter++;
            int framesPerSprite = (currentType == WeaponType.AK47) ? 3 : 5;
            if (spriteCounter > framesPerSprite) {
                spriteNum++;
                spriteCounter = 0;
                int maxSprite = (currentType == WeaponType.AK47) ? 2 : 3;
                if (spriteNum > maxSprite) {
                    spriteNum  = 1;
                    isShooting = false;
                }
            }
        }
    }

    public void addAmmo(int amount) {
        this.ammo = Math.min(this.ammo + amount, maxAmmo);
    }

    public void draw(Graphics2D g2) {
        // Pilih gambar sesuai senjata & state
        BufferedImage image;
        if (currentType == WeaponType.AK47) {
            if (isShooting) {
                image = (spriteNum == 1) ? ak_shoot1 : ak_shoot2;
            } else {
                image = ak_idle;
            }
        } else {
            if (isShooting) {
                if (spriteNum == 1)      image = dg_shoot1;
                else if (spriteNum == 2) image = dg_shoot2;
                else                     image = dg_shoot3;
            } else {
                image = dg_idle;
            }
        }

        if (image == null) image = dg_idle; // fallback

        if (image != null) {
            AffineTransform oldTransform = g2.getTransform();

            float screenWeaponX = weaponX - gp.getPlayer().x + gp.getPlayer().screenX;
            float screenWeaponY = weaponY - gp.getPlayer().y + gp.getPlayer().screenY;

            g2.translate(screenWeaponX, screenWeaponY);
            g2.rotate(angle);
            g2.scale(-1, 1);

            if (Math.abs(angle) > Math.PI / 2) {
                g2.scale(1, -1);
            }

            float scale  = 0.4f;
            int   width  = (int)(image.getWidth()  * scale);
            int   height = (int)(image.getHeight() * scale);

            if (isReloading) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }

            g2.drawImage(image, -width / 2, -height / 2, width, height, null);

            if (isReloading) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            g2.setTransform(oldTransform);
        }
    }
}
