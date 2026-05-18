package entity;

import main.GamePanel;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;

public class Zombie extends Character {
    GamePanel gp;

    public int maxHp;
    private int rewardXP;

    private int attackCooldown    = 0;
    private static final int MAX_ATTACK_CD = 60;

    private static final float ATTACK_RANGE = 40f;

    public Zombie(GamePanel gp, int startX, int startY, int currentLevel) {
        super(10 + (currentLevel * 5), 10 + (currentLevel * 2));

        this.gp       = gp;
        this.x        = startX;
        this.y        = startY;
        this.maxHp    = 10 + (currentLevel * 5);

        this.rewardXP = 20 + (currentLevel * 10);
        this.speed    = 1 + (currentLevel / 2);
        this.direction = "down";

        solidArea        = new Rectangle();
        solidArea.x      = 8;
        solidArea.y      = 16;
        solidArea.width  = gp.tileSize - 16;
        solidArea.height = gp.tileSize - 16;
    }

    public void update() {
        if (!isAlive()) return;

        // Kurangi cooldown serangan setiap frame
        if (attackCooldown > 0) attackCooldown--;

        chasePlayer(gp.player);
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(60, 150, 60));
        g2.fillRect(x, y, gp.tileSize, gp.tileSize);

        // HP bar di atas zombie
        int barWidth = gp.tileSize - 8;
        int filled   = (int)((float) hp / maxHp * barWidth);
        g2.setColor(Color.RED);
        g2.fillRect(x + 4, y - 8, barWidth, 5);
        g2.setColor(Color.GREEN);
        g2.fillRect(x + 4, y - 8, filled, 5);
    }
    public void chasePlayer(Player player) {
        if (!isAlive()) return;
        float dx       = player.x - this.x;
        float dy       = player.y - this.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= ATTACK_RANGE) {
            attack(player);
            return;
        }

        if (distance > 0) {
            this.x += (int)((dx / distance) * speed);
            this.y += (int)((dy / distance) * speed);
        }


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
        System.out.println("[Zombie] GIGIT! -" + baseDmg + " HP ke Player");

        // Reset cooldown
        attackCooldown = MAX_ATTACK_CD;

    }

    public void giveRewardXP(Player player) {
        player.addXP(rewardXP);
        System.out.println("[Zombie] Mati! +" + rewardXP + " XP");
    }
    public int getRewardXP()       { return rewardXP; }
    public int getAttackCooldown() { return attackCooldown; }

}