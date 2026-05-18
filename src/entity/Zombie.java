package entity;

import main.GamePanel;

public class Zombie extends Entity {
    GamePanel gp;

    public int hp;
    public int maxHp;

    // Tambahkan parameter level saat zombie diciptakan
    public Zombie(GamePanel gp, int startX, int startY, int currentLevel) {
        this.gp = gp;
        this.x = startX;
        this.y = startY;

        // Logika Scaling HP (Makin tinggi level, HP makin tebal)
        // Level 1: 10 + (1*5) = 15 HP
        // Level 5: 10 + (5*5) = 35 HP
        this.maxHp = 10 + (currentLevel * 5);
        this.hp = maxHp;

        // Logika Scaling Kecepatan
        // Jangan terlalu drastis agar player masih bisa menghindar
        // Level 1-2: speed 2 | Level 3-4: speed 3
        this.speed = 1 + (currentLevel / 2);

        // Jangan lupa set hitbox solidArea seperti di Player
        // ...
    }

    public void update() {
        // Logika AI Zombie untuk mengejar player (Pathfinding dasar)
        // ...
    }
}