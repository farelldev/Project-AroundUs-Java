package entity;

import combat.Bullet;
import combat.Weapon;
import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import item.Items;

public class Player extends Character {
    GamePanel gp;
    KeyHandler keyH;

    // -- Ukuran Layar untuk Player -------------------
    public final int screenX;
    public final int screenY;

    // -- Statisik player -----------------------------
    private static final int XP_PER_LEVEL   = 100;
    private static final int HP_PER_LEVEL   = 20;
    private static final int DMG_PER_LEVEL  = 5;
    private static final int BASE_MAX_HP    = 100;
    private static final int GRENADE_DAMAGE = 80;
    private static final int GRENADE_RADIUS = 150;

    // -- Atribut Player ------------------------------
    private int        xp;
    private int        level;
    private int        maxHp;
    private int        currHp;
    private int        bandageBrought;
    private int        ammo;
    private List<Items> inventory;
    private final Weapon      weapon;

    // -- Variabel untuk mengatur kecepatan animasi ---
    int spriteCounter = 0;
    int spriteNum = 1;

    // -- Konstruktor ---------------------------------
    public Player(GamePanel gp, KeyHandler keyH) {
        super(BASE_MAX_HP, 15);

        this.gp = gp;
        this.keyH = keyH;
        this.xp             = 0;
        this.level          = 1;
        this.maxHp          = BASE_MAX_HP;
        this.bandageBrought = 3;
        this.ammo           = 30;
        this.weapon         = new Weapon(this.gp);
        this.inventory      = new ArrayList<>();

        setDevaultValues();
        getPlayerImage();

        // Mengunci posisi render karakter tepat di titik tengah layar
        screenX = (gp.screenWidht / 2) - (gp.tileSize / 2);
        screenY = (gp.screenHeight / 2) - (gp.tileSize / 2);

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = gp.tileSize - 16;
        solidArea.height = gp.tileSize - 16;

        getPlayerImage();
    }

    // -- Getter --------------------------------------
    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAmmo() {
        return ammo;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    // -- Heal dari bandage ---------------------------
    public void heal(int healAmount) {
        if (bandageBrought <= 0) { System.out.println("Bandage habis!"); return; }
        if (hp >= maxHp)         { System.out.println("HP penuh!"); return; }
        bandageBrought--;
        hp = Math.min(hp + healAmount, maxHp);
        System.out.println("[Player] Heal +" + healAmount + " HP → " + hp + "/" + maxHp);
    }

    // --Ambil peluru dari ammo -----------------------
    public void addAmmo(int amount) {
        this.ammo += amount;
        System.out.println("Ammo bertambah " + amount +
                "! Ammo sekarang: " + ammo);
    }

    @Override
    public void takeDmg(int dmg) {
        if (dmg < 0) return;
        this.hp = Math.max(0, this.hp - dmg);
        System.out.printf("[Player] -%d HP  →  HP: %d%n", dmg, this.hp);
        // Tampilkan bloodstain di layar sesuai damage
        gp.bloodStain.onPlayerHit(dmg);
        // Sound player hurt
        gp.soundManager.playSFX("player_hurt");
    }

    // -- 3 varian attack -----------------------------
    @Override
    public void attack() {
        float targetX = gp.getKeyH().mouseX;
        float targetY = gp.getKeyH().mouseY;

        Bullet newBullet = weapon.shoot(weapon.getX(), weapon.getY(), targetX, targetY);

        if (newBullet != null) {
            System.out.println("[Player] Serangan biasa! Dmg: " + baseDmg);
            gp.getActiveBullets().add(newBullet);
        }
    }

    public void attack(String skill) {
        switch (skill.toLowerCase()) {
            case "grenade": System.out.println("[Player] GRANAT! Dmg: 80"); break;
            case "charge":  System.out.println("[Player] CHARGE! Dmg: " + (int)(baseDmg*1.5f)); break;
            default:        System.out.println("[Player] Skill tidak dikenal."); break;
        }
    }

    public void attack(int multiplier) {
        int finalDmg = baseDmg * Math.max(1, multiplier);
        System.out.println("[Player] POWER STRIKE! Dmg: " + finalDmg
                + " (baseDmg x" + multiplier + ")");
    }

    // -- Dapet XP ------------------------------------
    public void addXP(int xp) {
        this.xp += xp;
        while (this.xp >= 100 * this.level) {
            this.xp   -= 100 * this.level;
            this.level++;
            this.maxHp   += 20;
            this.hp       = Math.min(hp + 20, maxHp);
            this.baseDmg += 5;
            System.out.println("[Player] LEVEL UP → Lv." + level);
        }
    }

    public void setDevaultValues(){
        x = gp.tileSize * 2;
        y = gp.tileSize * 2;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage(){
        try{
            up1 = ImageIO.read(Player.class.getResourceAsStream("/player/up_1.png"));
            up2 = ImageIO.read(Player.class.getResourceAsStream("/player/up_2.png"));
            down1 = ImageIO.read(Player.class.getResourceAsStream("/player/down_1.png"));
            down2 = ImageIO.read(Player.class.getResourceAsStream("/player/down_2.png"));
            right1 = ImageIO.read(Player.class.getResourceAsStream("/player/right_1.png"));
            right2 = ImageIO.read(Player.class.getResourceAsStream("/player/right_2.png"));
            left1 = ImageIO.read(Player.class.getResourceAsStream("/player/left_1.png"));
            left2 = ImageIO.read(Player.class.getResourceAsStream("/player/left_2.png"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void update(){
        if (weapon != null) {
            weapon.update(this);

            // Baca sudut senjata untuk menentukan arah hadap Player
            double angleDegree = Math.toDegrees(weapon.getAngle());

            if (angleDegree > -45 && angleDegree <= 45) {
                direction = "right";
            } else if (angleDegree > 45 && angleDegree <= 135) {
                direction = "down";
            } else if (angleDegree > 135 || angleDegree <= -135) {
                direction = "left";
            } else {
                direction = "up";
            }
        }

        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

            String tempDirection = direction; // Simpan arah hadap kursor sementara

            // Cek tabrakan dan gerak per tombol (Bisa jalan diagonal)
            if (keyH.upPressed) {
                direction = "up"; // Tipu CollisionCheck sebentar
                collisionOn = false;
                gp.cChecker.checkTile(this);
                if (!collisionOn) y -= speed;
            }
            if (keyH.downPressed) {
                direction = "down";
                collisionOn = false;
                gp.cChecker.checkTile(this);
                if (!collisionOn) y += speed;
            }
            if (keyH.leftPressed) {
                direction = "left";
                collisionOn = false;
                gp.cChecker.checkTile(this);
                if (!collisionOn) x -= speed;
            }
            if (keyH.rightPressed) {
                direction = "right";
                collisionOn = false;
                gp.cChecker.checkTile(this);
                if (!collisionOn) x += speed;
            }

            direction = tempDirection; // Kembalikan animasi menghadap kursor

            // Animasi langkah kaki
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 1;
        }

        // == ANIMASI PISTOL ============================
        if(keyH.leftMousePressed){
            attack();
        }

        // Reload saat tekan R
        if (keyH.reloadPressed && weapon != null) {
            weapon.startReload();
        }

        // Update posisi pistol
        if (weapon != null) {
            int playerCenterX = this.x + (gp.tileSize / 2);
            int playerCenterY = this.y + (gp.tileSize / 2);
            weapon.update(this);
        }
    }

    public void draw(Graphics2D g2){
        BufferedImage image = null;

        switch (direction) {
            case "up":
                if (spriteNum == 1) image = up1;
                if (spriteNum == 2) image = up2;
                break;
            case "down":
                if (spriteNum == 1) image = down1;
                if (spriteNum == 2) image = down2;
                break;
            case "left":
                if (spriteNum == 1) image = left1;
                if (spriteNum == 2) image = left2;
                break;
            case "right":
                if (spriteNum == 1) image = right1;
                if (spriteNum == 2) image = right2;
                break;
        }

        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);

        if (weapon != null) {
            weapon.draw(g2);
        }
    }
}
