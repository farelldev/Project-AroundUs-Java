package item;

import Entity.Player;
import java.util.Random;

public class Chest {
    private float x;
    private float y;
    private Items content;
    private boolean isOpened;
    private static final int INTERACTION_RANGE = 50;

    public Chest(float x, float y) {
        this.x = x;
        this.y = y;
        this.isOpened = false;
        this.content = generateRandomItem();
    }

    private Items generateRandomItem() {
        Random random = new Random();
        int roll = random.nextInt(3);

        if (roll == 0) {
            return new Bandage(20);
        } else if (roll == 1) {
            return new Bandage(10);
        } else {
            return new Ammo();
        }
    }

    public boolean isPlayerNearby(Player player) {
        float dx = player.x - this.x;
        float dy = player.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= INTERACTION_RANGE;
    }

    public void open(Player player) {
        if (!isOpened) {
            if (isPlayerNearby(player)) {
                content.use(player);
                isOpened = true;
                System.out.println("Chest dibuka! Kamu mendapat: "
                        + content.name);
            } else {
                System.out.println("Kamu terlalu jauh dari chest!");
            }
        } else {
            System.out.println("Chest sudah dibuka!");
        }
    }

    public void appear() {
        isOpened = false;
        content = generateRandomItem();
    }

    public void disappear() {
        isOpened = true;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isOpened() { return isOpened; }
    public Items getContent() { return content; }
}