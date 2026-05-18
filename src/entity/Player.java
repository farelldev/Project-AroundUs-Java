package entity;

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
    private Weapon      weapon;

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
        this.inventory      = new ArrayList<>();

        setDevaultValues();
        getPlayerImage();

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

    // -- 3 varian attack -----------------------------
    @Override
    public void attack() {
        System.out.println("[Player] Serangan jarak dekat! Dmg: " + baseDmg);
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
        x = 100;
        y = 100;
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
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

            if (!keyH.directionList.isEmpty()) {
                direction = keyH.directionList.get(keyH.directionList.size() - 1);
            }

            // Cek collision dengan tile
            collisionOn = false;
            gp.cChecker.checkTile(this);

            // Gerakkan Player hanya jika tidak nabrak tile solid
            if (!collisionOn) {
                switch (direction) {
                    case "up"   : y -= speed; break;
                    case "down" : y += speed; break;
                    case "left" : x -= speed; break;
                    case "right": x += speed; break;
                }
            }

            // Animasi: ganti sprite setiap 10 frame
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum     = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
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

        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
    }
}

