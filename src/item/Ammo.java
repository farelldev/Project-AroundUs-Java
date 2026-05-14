package item;

import Entity.Player;

public class Ammo extends Items {
    private int ammoAmount = 30;

    public Ammo() {
        setName("Ammo Box");
    }

    @Override
    public void use(Player player) {
        if (player.getWeapon() != null) {
            player.getWeapon().addAmmo(this.ammoAmount);
            System.out.println("Berhasil reload! Amunisi bertambah: " + ammoAmount);
        }
    }
}