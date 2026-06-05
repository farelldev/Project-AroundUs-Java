package item;

import entity.Player;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Item AK47 yang bisa di-loot dari chest.
 * Saat dipungut, senjata player berganti ke AK47 dengan ammo penuh.
 */
public class AK47Item extends Items {

    private GamePanel gp;

    public AK47Item(GamePanel gp) {
        this.gp = gp;
        setName("AK47");
        loadAK47Image();
    }

    private void loadAK47Image() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/weapon/ak47/ak_1.png"));
        } catch (Exception e) {
            System.out.println("[AK47Item] Gagal memuat gambar AK47!");
        }
    }

    @Override
    public void use(Player player) {
        // Pindahkan ke AK47 dan isi ammo penuh
        player.getWeapon().switchToAK47();
        System.out.println("[AK47Item] Player mendapatkan AK47!");

        // Mainkan sound pickup jika ada
        if (gp != null) {
            gp.soundManager.playSFX("reload");
        }
    }
}
