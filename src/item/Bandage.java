package item;

import entity.Player;

public class Bandage extends Items {
    private int healAmount;
    private static final int DEFAULT_HEAL = 20;

    public Bandage() {
        setName("Bandage");
        this.healAmount = DEFAULT_HEAL;
    }

    public Bandage(int healAmount) {
        setName("Bandage");
        this.healAmount = healAmount;
    }

    @Override
    public void use(Player player) {
        if (player.getHp() < player.getMaxHp()) {
            player.heal(healAmount);
            System.out.println("Kamu menggunakan Bandage! HP +"
                    + healAmount);
        } else {
            System.out.println("HP kamu sudah penuh!");
        }
    }

    public int getHealAmount() {
        return healAmount;
    }
}