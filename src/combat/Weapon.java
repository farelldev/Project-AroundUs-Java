package combat;

public class Weapon {
    private int ammo = 30; // Contoh amunisi awal
    private int damage = 10;

    public Bullet shoot(float startX, float startY, float targetX, float targetY) {
        if (ammo > 0) {
            ammo--;
            return new Bullet(startX, startY, targetX, targetY, damage);
        }
        // Kalau peluru habis, bisa return null atau lempar suara "klik"
        return null;
    }

    // Jangan lupa bikin setter/getter buat nambah ammo nanti
    public void addAmmo(int amount) {
        this.ammo += amount;
    }
}
