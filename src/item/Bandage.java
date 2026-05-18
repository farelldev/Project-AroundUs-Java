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
        player.heal(healAmount);
    }

    public int getHealAmount() {
        return healAmount;
    }
}