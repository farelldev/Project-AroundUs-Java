package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;

    // Variabel untuk mengatur kecepatan animasi
    int spriteCounter = 0;
    int spriteNum = 1;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDevaultValues();
        getPlayerImage(); // Memanggil fungsi gambar saat player dibuat

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = gp.tileSize - 16;
        solidArea.height = gp.tileSize - 16;

        getPlayerImage();
    }







    public void setDevaultValues(){
        x = 100;
        y = 100;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage(){
        try{
            up1 = ImageIO.read(Player.class.getResourceAsStream("/Player/up_1.png"));
            up2 = ImageIO.read(Player.class.getResourceAsStream("/Player/up_2.png"));
            down1 = ImageIO.read(Player.class.getResourceAsStream("/Player/down_1.png"));
            down2 = ImageIO.read(Player.class.getResourceAsStream("/Player/down_2.png"));
            right1 = ImageIO.read(Player.class.getResourceAsStream("/Player/right_1.png"));
            right2 = ImageIO.read(Player.class.getResourceAsStream("/Player/right_2.png"));
            left1 = ImageIO.read(Player.class.getResourceAsStream("/Player/left_1.png"));
            left2 = ImageIO.read(Player.class.getResourceAsStream("/Player/left_2.png"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    public void update(){
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

//            if (keyH.upPressed) {
//                direction = "up";
//            } else if (keyH.downPressed) {
//                direction = "down";
//            } else if (keyH.leftPressed) {
//                direction = "left";
//            } else if (keyH.rightPressed) {
//                direction = "right";
//            }

            if (!keyH.directionList.isEmpty()) {
                // Mengambil elemen terakhir dari list (index size - 1)
                direction = keyH.directionList.get(keyH.directionList.size() - 1);
            }

            collisionOn = false;

            gp.cChecker.checkTile(this);
            if (collisionOn == false) {
                switch (direction) {
                    case "up": y -= speed;
                    break;
                    case "down": y += speed;
                    break;
                    case "left": x -= speed;
                    break;
                    case "right": x += speed;
                    break;
                }
            }

            spriteCounter++;
            if(spriteCounter > 10) {
                if(spriteNum == 1) {
                    spriteNum = 2;
                } else if(spriteNum == 2) {
                    spriteNum = 1;
                }
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
    private int hp = 100;
    private int maxHp = 100;
    private int ammo = 30;

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void heal(int amount) {
        this.hp = Math.min(hp + amount, maxHp);
        System.out.println("HP bertambah " + amount +
                "! HP sekarang: " + hp);
    }

    public void addAmmo(int amount) {
        this.ammo += amount;
        System.out.println("Ammo bertambah " + amount +
                "! Ammo sekarang: " + ammo);
    }

    public int getAmmo() {
        return ammo;
    }
}

