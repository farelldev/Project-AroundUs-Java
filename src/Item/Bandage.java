package Item;

import Entity.Player;

public class Bandage extends Item{
    private int healAmount;

    public Bandage(int healAmount) {
        super("Bandage");
        this.healAmount = healAmount;
    }

    @Override
    public void use(Player player) {
//        player.heal(healAmount);
    }
}
