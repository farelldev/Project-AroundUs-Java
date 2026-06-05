package main;

import entity.Zombie;
import item.Chest;
import java.util.Random;

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
        // Spawn peti pertama kali saat game dimulai (Level 1)
        spawnChests();
    }

    public void update() {
        if (!isLevelActive) return;

        spawnTimer++;

        // Spawn zombie di floor aktif
        if (spawnTimer >= nextSpawnTime && zombiesSpawnedThisLevel < maxZombiesPerLevel) {
            spawnZombie();
            spawnTimer = 0;
            zombiesSpawnedThisLevel++;
        }

        // Naik level
        boolean allDead = gp.zombiesFloor1.isEmpty() && gp.zombiesFloor2.isEmpty();
        if (zombiesSpawnedThisLevel >= maxZombiesPerLevel && allDead) {
            levelUp();
        }
    }

    private void spawnChests() {
        gp.chests.clear();

        // Rumus: Wave 1-2 = 2 Peti, Wave 3-4 = 4 Peti, dst.
        int chestCount = ((currentLevel + 1) / 2) * 2;
        System.out.println("[LevelManager] Memunculkan " + chestCount + " peti baru!");

        for (int i = 0; i < chestCount; i++) {
            boolean validTile = false;
            int randCol = 0, randRow = 0;

            // Cari ubin aman di dalam bangunan
            while (!validTile) {
                randCol = random.nextInt(gp.maxWorldCol);
                randRow = random.nextInt(gp.maxWorldRow);

                int tileNum = gp.tileM.mapTileNum[randCol][randRow];

                // Syarat aman: BUKAN tembok, BUKAN rumput (0), BUKAN tangga (16/57)
                if (tileNum != -1 && !gp.tileM.tile[tileNum].collision && tileNum != 0 && tileNum != 16 && tileNum != 57) {
                    validTile = true;
                }
            }

            int spawnX = randCol * gp.tileSize;
            int spawnY = randRow * gp.tileSize;

            // Masukkan peti ke list GamePanel
            gp.chests.add(new Chest(gp, spawnX, spawnY));
        }
    }

    private void spawnZombie() {
        int px     = gp.getPlayer().x;
        int py     = gp.getPlayer().y;
        int margin = gp.tileSize * 10;

        for (int attempt = 0; attempt < 20; attempt++) {
            int spawnX, spawnY;
            int side = random.nextInt(4);

            switch (side) {
                case 0: // atas
                    spawnX = px + random.nextInt(gp.screenWidht) - gp.screenWidht / 2;
                    spawnY = py - margin;
                    break;
                case 1: // bawah
                    spawnX = px + random.nextInt(gp.screenWidht) - gp.screenWidht / 2;
                    spawnY = py + margin;
                    break;
                case 2: // kiri
                    spawnX = px - margin;
                    spawnY = py + random.nextInt(gp.screenHeight) - gp.screenHeight / 2;
                    break;
                default: // kanan
                    spawnX = px + margin;
                    spawnY = py + random.nextInt(gp.screenHeight) - gp.screenHeight / 2;
                    break;
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

    private void levelUp() {
        currentLevel++;
        zombiesSpawnedThisLevel = 0;
        maxZombiesPerLevel += 3;
        if (nextSpawnTime > 30) nextSpawnTime -= 15;
        System.out.println("--- NAIK KE LEVEL " + currentLevel + " ---");

        // Panggil peti di wave baru
        spawnChests();
    }
}