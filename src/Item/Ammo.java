package Item;

import Entity.Player;

public class Ammo extends Item{
    private int ammoAmount;

    public Ammo(int ammoAmount) {
        super("Ammo");
        this.ammoAmount = ammoAmount;
    }

    @Override
    public void use(Player player) {
//        player.addAmmount = ammoAmount;
    }
}
