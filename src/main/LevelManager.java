package main;

import entity.Zombie;
import item.AK47Item;
import item.Ammo;
import item.Bandage;
import item.Chest;
import java.util.Random;

/**
 * LevelManager — mengelola spawn zombie, chest, dan barrel.
 *
 * PERUBAHAN UTAMA:
 *  - Chest & barrel di-spawn TERPISAH per lantai (floor1 & floor2).
 *  - Tidak ada lagi satu list global yang di-share antar lantai.
 *  - AK47 masuk pool loot chest mulai wave 3.
 */
public class LevelManager {
    GamePanel gp;

    public int currentLevel    = 1;
    public boolean isLevelActive = true;

    int spawnTimer              = 0;
    int nextSpawnTime           = 120;

    int zombiesSpawnedThisLevel = 0;
    int maxZombiesPerLevel      = 5;

    private final Random random = new Random();

    public LevelManager(GamePanel gp) {
        this.gp = gp;
        // Spawn awal untuk KEDUA lantai agar tidak kosong saat player naik tangga
        spawnChestsForFloor(1);
        spawnChestsForFloor(2);
        spawnBarrelsForFloor(1);
        spawnBarrelsForFloor(2);
    }

    public void update() {
        if (!isLevelActive) return;

        spawnTimer++;

        if (spawnTimer >= nextSpawnTime && zombiesSpawnedThisLevel < maxZombiesPerLevel) {
            spawnZombie();
            spawnTimer = 0;
            zombiesSpawnedThisLevel++;
        }

        boolean allDead = gp.zombiesFloor1.isEmpty() && gp.zombiesFloor2.isEmpty();
        if (zombiesSpawnedThisLevel >= maxZombiesPerLevel && allDead) {
            levelUp();
        }
    }

    // ======================================================
    //  SPAWN CHEST — per lantai, independen satu sama lain
    // ======================================================

    private void spawnChestsForFloor(int floor) {
        java.util.ArrayList<Chest> target =
                (floor == 1) ? gp.chestsFloor1 : gp.chestsFloor2;
        target.clear();

        int chestCount = ((currentLevel + 1) / 2) * 2;
        System.out.println("[LevelManager] Floor " + floor + ": spawn " + chestCount + " peti.");

        for (int i = 0; i < chestCount; i++) {
            int[] pos = findValidTile(floor);
            if (pos == null) continue;

            Chest c = new Chest(gp, pos[0] * gp.tileSize, pos[1] * gp.tileSize);
            // Tambahkan AK47 ke pool loot mulai wave 3
            if (currentLevel >= 3) {
                c.setLootPool(currentLevel);
            }
            target.add(c);
        }
    }

    // ======================================================
    //  SPAWN BARREL — per lantai, independen satu sama lain
    // ======================================================

    private void spawnBarrelsForFloor(int floor) {
        java.util.ArrayList<item.ExplosiveBarrel> target =
                (floor == 1) ? gp.barrelsFloor1 : gp.barrelsFloor2;
        target.clear();

        int barrelCount = ((currentLevel - 1) / 3 + 1) * 3;
        System.out.println("[LevelManager] Floor " + floor + ": spawn " + barrelCount + " tong.");

        for (int i = 0; i < barrelCount; i++) {
            int[] pos = findValidTile(floor);
            if (pos == null) continue;
            target.add(new item.ExplosiveBarrel(gp, pos[0] * gp.tileSize, pos[1] * gp.tileSize));
        }
    }

    /**
     * Cari tile lantai yang valid untuk spawn object.
     * Untuk floor 2, kita cari tile yang aktif di map lantai 2
     * (TileManager mengelola ini via currentFloor).
     * Mengembalikan {col, row} atau null jika gagal.
     */
    private int[] findValidTile(int floor) {
        // Simpan floor aktif, ganti sementara jika perlu membaca map floor lain
        int savedFloor = gp.tileM.currentFloor;
        if (floor != savedFloor) {
            gp.tileM.currentFloor = floor;
            // Muat tile map yang sesuai agar mapTileNum benar
            // Catatan: TileManager harus mendukung ini — fallback: pakai floor 1 map
        }

        int[] result = null;
        for (int attempt = 0; attempt < 200; attempt++) {
            int col = random.nextInt(gp.maxWorldCol);
            int row = random.nextInt(gp.maxWorldRow);

            if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) continue;

            int tileNum = gp.tileM.mapTileNum[col][row];
            // Valid: bukan tembok, bukan rumput luar (0), bukan tangga (16/57)
            if (tileNum != -1
                    && gp.tileM.tile[tileNum] != null
                    && !gp.tileM.tile[tileNum].collision
                    && tileNum != 0
                    && tileNum != 16
                    && tileNum != 57) {
                result = new int[]{col, row};
                break;
            }
        }

        // Kembalikan floor map ke semula
        if (floor != savedFloor) {
            gp.tileM.currentFloor = savedFloor;
        }

        return result;
    }

    // ======================================================
    //  SPAWN ZOMBIE
    // ======================================================

    private void spawnZombie() {
        int px     = gp.getPlayer().x;
        int py     = gp.getPlayer().y;
        int margin = gp.tileSize * 10;

        for (int attempt = 0; attempt < 20; attempt++) {
            int spawnX, spawnY;
            int side = random.nextInt(4);

            switch (side) {
                case 0: spawnX = px + random.nextInt(gp.screenWidht) - gp.screenWidht / 2;  spawnY = py - margin; break;
                case 1: spawnX = px + random.nextInt(gp.screenWidht) - gp.screenWidht / 2;  spawnY = py + margin; break;
                case 2: spawnX = px - margin; spawnY = py + random.nextInt(gp.screenHeight) - gp.screenHeight / 2; break;
                default: spawnX = px + margin; spawnY = py + random.nextInt(gp.screenHeight) - gp.screenHeight / 2; break;
            }

            spawnX = Math.max(gp.tileSize, Math.min(spawnX, (gp.maxWorldCol - 2) * gp.tileSize));
            spawnY = Math.max(gp.tileSize, Math.min(spawnY, (gp.maxWorldRow - 2) * gp.tileSize));

            int col = spawnX / gp.tileSize;
            int row = spawnY / gp.tileSize;

            if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) continue;

            int tileNum = gp.tileM.mapTileNum[col][row];
            if (tileNum == -1 || gp.tileM.tile[tileNum] == null || gp.tileM.tile[tileNum].collision) continue;

            Zombie z = new Zombie(gp, spawnX, spawnY, currentLevel);
            gp.getActiveZombies().add(z);
            return;
        }
    }

    // ======================================================
    //  LEVEL UP
    // ======================================================

    private void levelUp() {
        currentLevel++;
        zombiesSpawnedThisLevel = 0;
        maxZombiesPerLevel += 3;
        if (nextSpawnTime > 30) nextSpawnTime -= 15;
        System.out.println("--- NAIK KE LEVEL " + currentLevel + " ---");

        // Spawn ulang chest & barrel untuk KEDUA lantai
        spawnChestsForFloor(1);
        spawnChestsForFloor(2);
        spawnBarrelsForFloor(1);
        spawnBarrelsForFloor(2);
    }
}
