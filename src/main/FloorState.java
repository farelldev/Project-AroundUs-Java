package main;

import combat.Bullet;
import entity.Zombie;
import item.Chest;
import item.ExplosiveBarrel;
import item.Items;

import java.util.ArrayList;

/**
 * FloorState — menyimpan SEMUA entity milik satu lantai.
 *
 * Keuntungan: kalau nanti mau tambah entity baru (misalnya NPC, trap, dll),
 * cukup tambah field di sini — GamePanel langsung ikut tanpa harus buat
 * list Floor1/Floor2 baru satu-satu.
 *
 * GamePanel tinggal pegang dua objek: floor[1] dan floor[2],
 * lalu akses via getActiveFloor().
 */
public class FloorState {

    public final int floorNumber;

    // ── Entity yang hidup di lantai ini ──────────────────────────────────────
    public final ArrayList<Zombie>          zombies      = new ArrayList<>();
    public final ArrayList<Chest>           chests       = new ArrayList<>();
    public final ArrayList<ExplosiveBarrel> barrels      = new ArrayList<>();
    public final ArrayList<Items>           droppedItems = new ArrayList<>();
    public final ArrayList<Bullet>          bullets      = new ArrayList<>();

    // ── Tambahkan field lain di sini jika nanti ada entity baru ──────────────
    // public final ArrayList<NPC>   npcs   = new ArrayList<>();
    // public final ArrayList<Trap>  traps  = new ArrayList<>();

    public FloorState(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    /** Bersihkan semua entity — dipanggil saat wave baru (kecuali bullets) */
    public void clearSpawnables() {
        chests.clear();
        barrels.clear();
        // droppedItems & bullets dibiarkan karena masih mungkin ada di lantai
    }

    /** Bersihkan semuanya — untuk reset total */
    public void clearAll() {
        zombies.clear();
        chests.clear();
        barrels.clear();
        droppedItems.clear();
        bullets.clear();
    }
}
