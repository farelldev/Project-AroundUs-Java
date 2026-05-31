package entity;

import main.GamePanel;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Zombie extends Character {
    GamePanel gp;

    public int maxHp;
    private int rewardXP;

    // State machine
    public enum State { IDLE, WALK, ATTACK, HURT, DEAD }
    private State state = State.IDLE;

    private int attackCooldown    = 0;
    private static final int MAX_ATTACK_CD = 60;
    private static final float ATTACK_RANGE = 40f;

    // Animasi
    private int spriteCounter = 0;
    private int spriteNum     = 1;

    // State timer
    private int hurtTimer  = 0;
    private int deadTimer  = 0;
    private boolean xpGiven = false;

    // Posisi presisi untuk gerakan mulus
    private float preciseX, preciseY;

    // Sprites
    private BufferedImage idle1, idle2;
    private BufferedImage walk1, walk2, walk3;
    private BufferedImage attack1, attack2;
    private BufferedImage hurt1, hurt2;
    private BufferedImage dead;

    public Zombie(GamePanel gp, int startX, int startY, int currentLevel) {
        super(10 + (currentLevel * 5), 10 + (currentLevel * 2));

        this.gp       = gp;
        this.x        = startX;
        this.y        = startY;
        this.preciseX = startX;
        this.preciseY = startY;
        this.maxHp    = hp;

        this.rewardXP = 20 + (currentLevel * 10);
        this.speed    = 1 + (currentLevel / 2);
        this.direction = "down";

        solidArea        = new Rectangle();
        solidArea.x      = 8;
        solidArea.y      = 16;
        solidArea.width  = gp.tileSize - 16;
        solidArea.height = gp.tileSize - 16;

        loadImages();
    }

    private void loadImages() {
        try {
            idle1   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_idle1.png"));
            idle2   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_idle2.png"));
            walk1   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_walk1.png"));
            walk2   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_walk2.png"));
            walk3   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_walk3.png"));
            attack1 = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_attack1.png"));
            attack2 = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_attack2.png"));
            hurt1   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_hurt1.png"));
            hurt2   = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_hurt2.png"));
            dead    = ImageIO.read(getClass().getResourceAsStream("/zombie/zombie_dead.png"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[Zombie] Gagal memuat sprite zombie!");
        }
    }

    @Override
    public void takeDmg(int dmg) {
        if (state == State.DEAD) return;
        super.takeDmg(dmg);
        if (hp <= 0) {
            state = State.DEAD;
            deadTimer = 0;
        } else {
            state = State.HURT;
            hurtTimer = 20; // 20 frame animasi hurt
            spriteNum = 1;
            spriteCounter = 0;
        }
    }

    public void update() {
        spriteCounter++;

        switch (state) {
            case DEAD:
                deadTimer++;
                // Biarkan zombie tetap "ada" selama 90 frame (animasi mati)
                // GamePanel akan cek isDoneWithDeadAnim() untuk hapus
                if (!xpGiven) {
                    giveRewardXP(gp.getPlayer());
                    xpGiven = true;
                }
                break;

            case HURT:
                // Animasi hurt: flip antara hurt1-hurt2
                if (spriteCounter > 6) {
                    spriteNum = (spriteNum == 1) ? 2 : 1;
                    spriteCounter = 0;
                }
                hurtTimer--;
                if (hurtTimer <= 0) {
                    state = State.WALK;
                    spriteNum = 1;
                    spriteCounter = 0;
                }
                break;

            case IDLE:
            case WALK:
            case ATTACK:
                if (attackCooldown > 0) attackCooldown--;
                chasePlayer(gp.getPlayer());
                break;
        }
    }

    public void draw(Graphics2D g2) {
        // Rumus kamera (sama dengan player & bullet)
        int screenX = x - gp.getPlayer().x + gp.getPlayer().screenX;
        int screenY = y - gp.getPlayer().y + gp.getPlayer().screenY;

        // Culling: hanya gambar kalau dalam layar
        if (screenX + gp.tileSize < 0 || screenX - gp.tileSize > gp.screenWidht ||
                screenY + gp.tileSize < 0 || screenY - gp.tileSize > gp.screenHeight) {
            return;
        }

        BufferedImage img = getFrameImage();

        // Flip horizontal jika zombie menghadap kiri
        if (img != null) {
            java.awt.geom.AffineTransform old = g2.getTransform();
            if (direction.equals("left")) {
                g2.translate(screenX + gp.tileSize, screenY);
                g2.scale(-1, 1);
                g2.drawImage(img, 0, 0, gp.tileSize, gp.tileSize, null);
            } else {
                g2.drawImage(img, screenX, screenY, gp.tileSize, gp.tileSize, null);
            }
            g2.setTransform(old);
        } else {
            // Fallback kotak hijau kalau sprite gagal muat
            g2.setColor(new Color(60, 150, 60));
            g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
        }


    }

    private BufferedImage getFrameImage() {
        switch (state) {
            case DEAD:
                return dead;

            case HURT:
                return (spriteNum == 1) ? hurt1 : hurt2;

            case ATTACK:
                if (spriteCounter > 8) {
                    spriteNum = (spriteNum == 1) ? 2 : 1;
                    spriteCounter = 0;
                }
                return (spriteNum == 1) ? attack1 : attack2;

            case IDLE:
                if (spriteCounter > 30) {
                    spriteNum = (spriteNum == 1) ? 2 : 1;
                    spriteCounter = 0;
                }
                return (spriteNum == 1) ? idle1 : idle2;

            case WALK:
            default:
                if (spriteCounter > 10) {
                    spriteNum++;
                    if (spriteNum > 3) spriteNum = 1;
                    spriteCounter = 0;
                }
                if (spriteNum == 1) return walk1;
                if (spriteNum == 2) return walk2;
                return walk3;
        }
    }

    public void chasePlayer(Player player) {
        float dx       = player.x - this.x;
        float dy       = player.y - this.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Update arah hadap berdasarkan vektor ke player
        if (Math.abs(dx) >= Math.abs(dy))
            direction = (dx > 0) ? "right" : "left";
        else
            direction = (dy > 0) ? "down" : "up";

        if (distance <= ATTACK_RANGE) {
            state = State.ATTACK;
            attack(player);
            return;
        }

        state = State.WALK;

        if (distance == 0) return;

        // Vektor arah dinormalisasi
        float nx = dx / distance;
        float ny = dy / distance;

        // Gerak sumbu X — cek collision sendiri
        collisionOn = false;
        direction   = (nx > 0) ? "right" : "left";
        gp.cChecker.checkTile(this);
        if (!collisionOn) {
            preciseX += nx * speed;
        }

        // Gerak sumbu Y — cek collision sendiri
        collisionOn = false;
        direction   = (ny > 0) ? "down" : "up";
        gp.cChecker.checkTile(this);
        if (!collisionOn) {
            preciseY += ny * speed;
        }

        // Sinkronisasi ke int x,y
        this.x = (int) preciseX;
        this.y = (int) preciseY;

        // Kembalikan direction ke arah dominan untuk animasi & draw
        if (Math.abs(dx) >= Math.abs(dy))
            direction = (dx > 0) ? "right" : "left";
        else
            direction = (dy > 0) ? "down" : "up";
    }

    @Override
    public void attack() { }

    public void attack(Player player) {
        if (attackCooldown > 0) return;
        player.takeDmg(baseDmg);
        attackCooldown = MAX_ATTACK_CD;
    }

    public void giveRewardXP(Player player) {
        player.addXP(rewardXP);
        System.out.println("[Zombie] Mati! +" + rewardXP + " XP");
    }

    /** Kembalikan true kalau animasi mati sudah selesai dan zombie bisa dihapus */
    public boolean isDoneWithDeadAnim() {
        return state == State.DEAD && deadTimer >= 90;
    }

    public boolean isDead()   { return state == State.DEAD; }
    public int getRewardXP()  { return rewardXP; }
    public int getAttackCooldown() { return attackCooldown; }
}