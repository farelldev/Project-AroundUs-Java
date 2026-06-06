package item;

import combat.Damageable;
import entity.Zombie;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * ExplosiveBarrel — tong meledak saat ditembak 3 kali.
 *
 * PERUBAHAN:
 *  - Sound ledakan diputar via GamePanel.soundManager.
 *  - Sound hit dinding (hit_wall) diputar saat tong kena peluru.
 */
public class ExplosiveBarrel implements Damageable {
    GamePanel gp;
    public int x, y;
    public Rectangle solidArea;

    private int hp = 90; // 3 tembakan (damage 30 per peluru)
    public boolean isExploding  = false;
    public boolean isDestroyed  = false;
    private boolean soundPlayed = false; // Guard agar sound ledakan cuma sekali

    private BufferedImage idleImg;
    private BufferedImage[] explodeImgs;
    private int spriteCounter = 0;
    private int spriteNum     = 0;

    public ExplosiveBarrel(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x  = x;
        this.y  = y;
        this.solidArea = new Rectangle(x, y, gp.tileSize, gp.tileSize);
        loadImages();
    }

    private void loadImages() {
        try {
            idleImg = ImageIO.read(getClass().getResourceAsStream("/items/barrel/barrel.png"));

            explodeImgs    = new BufferedImage[7];
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
        if (isExploding || isDestroyed) return;
        hp -= dmg;
        // Suara peluru mengenai tong (metal hit)
        gp.soundManager.playSFX("hit_wall");
        if (hp <= 0) {
            isExploding = true;
            explode();
        }
    }

    private void explode() {
        System.out.println("[Barrel] BBOOMM!");

        // Suara ledakan
        if (!soundPlayed) {
            gp.soundManager.playSFX("player_hurt"); // Pakai player_hurt sbg sfx impact ledakan
            soundPlayed = true;
        }

        int radius          = gp.tileSize;
        Rectangle blastArea = new Rectangle(x - radius, y - radius, gp.tileSize * 3, gp.tileSize * 3);

        // Damage player
        Rectangle playerBox = new Rectangle(gp.getPlayer().x, gp.getPlayer().y, gp.tileSize, gp.tileSize);
        if (blastArea.intersects(playerBox)) {
            gp.getPlayer().takeDmg(30);
            gp.soundManager.playSFX("player_hurt");
            System.out.println("[Barrel] Player kena ledakan!");
        }

        // Damage zombie (hanya floor aktif)
        for (Zombie z : gp.getActiveZombies()) {
            if (!z.isDead()) {
                Rectangle zombieBox = new Rectangle(
                        z.x + z.solidArea.x, z.y + z.solidArea.y,
                        z.solidArea.width,   z.solidArea.height);
                if (blastArea.intersects(zombieBox)) {
                    z.takeDmg(30);
                }
            }
        }
    }

    // Skala tiap frame ledakan — ease-out agar natural (cepat awal, melambat di puncak)
    // Index cocok dengan explodeImgs[]: 0=explode1 ... 5=explode6 ... 6=explodeEnd
    private static final float[] SPRITE_SCALE = {
        1.0f,   // explode1 — ukuran normal
        1.35f,  // explode2 — mulai membesar cepat
        1.70f,  // explode3
        2.05f,  // explode4
        2.30f,  // explode5 — mulai melambat
        2.45f,  // explode6 — puncak, hampir berhenti
        1.80f   // explodeEnd — sedikit menyusut saat asap
    };

    public void update() {
        if (isExploding && !isDestroyed) {
            spriteCounter++;
            if (spriteCounter > 5) {
                spriteNum++;
                spriteCounter = 0;
                if (spriteNum >= explodeImgs.length) {
                    isDestroyed = true;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = x - gp.getPlayer().x + gp.getPlayer().screenX;
        int screenY = y - gp.getPlayer().y + gp.getPlayer().screenY;

        if (!isExploding) {
            if (idleImg != null)
                g2.drawImage(idleImg, screenX, screenY, gp.tileSize, gp.tileSize, null);
        } else if (!isDestroyed) {
            if (explodeImgs[spriteNum] != null) {
                float scale = (spriteNum < SPRITE_SCALE.length)
                        ? SPRITE_SCALE[spriteNum]
                        : SPRITE_SCALE[SPRITE_SCALE.length - 1];

                int drawW = (int)(gp.tileSize * scale);
                int drawH = (int)(gp.tileSize * scale);

                // Shake hanya di awal ledakan (sprite 0-4), berhenti di puncak
                int shakeX = (spriteNum < 5) ? (int)(Math.random() * 6 - 3) : 0;
                int shakeY = (spriteNum < 5) ? (int)(Math.random() * 6 - 3) : 0;

                // Tengahkan gambar ke posisi tong asli
                int drawX = screenX + (gp.tileSize / 2) - (drawW / 2) + shakeX;
                int drawY = screenY + (gp.tileSize / 2) - (drawH / 2) + shakeY;

                g2.drawImage(explodeImgs[spriteNum], drawX, drawY, drawW, drawH, null);
            }
        }
    }
}
