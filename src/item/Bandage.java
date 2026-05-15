package item;

import Entity.Player;

public class Bandage extends Items {
    private int healAmount;

    public Bandage(int healAmount) {
        setName("Bandage");
        this.healAmount = healAmount;
    }

    @Override
    public void use(Player player) {
//        player.heal(healAmount);
    }
}