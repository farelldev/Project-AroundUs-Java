package main;

import entity.Zombie;
import item.Chest;
import item.ExplosiveBarrel;
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
        boolean allDead = gp.floor[1].zombies.isEmpty() && gp.floor[2].zombies.isEmpty();
        if (zombiesSpawnedThisLevel >= maxZombiesPerLevel && allDead) levelUp();
    }

    // ── Spawn chest per lantai ────────────────────────────────────────────────

    private void spawnChestsForFloor(int floorNum) {
        FloorState fs = gp.floor[floorNum];
        fs.chests.clear();

        int count = ((currentLevel + 1) / 2) * 2;
        System.out.println("[LevelManager] Floor " + floorNum + ": spawn " + count + " peti.");

        for (int i = 0; i < count; i++) {
            int[] pos = findValidTile();
            if (pos == null) continue;
            Chest c = new Chest(gp, pos[0] * gp.tileSize, pos[1] * gp.tileSize);
            if (currentLevel >= 3) c.setLootPool(currentLevel);
            fs.chests.add(c);
        }
    }

    // ── Spawn barrel per lantai ───────────────────────────────────────────────

    private void spawnBarrelsForFloor(int floorNum) {
        FloorState fs = gp.floor[floorNum];
        fs.barrels.clear();

        int count = ((currentLevel - 1) / 3 + 1) * 3;
        System.out.println("[LevelManager] Floor " + floorNum + ": spawn " + count + " tong.");

        for (int i = 0; i < count; i++) {
            int[] pos = findValidTile();
            if (pos == null) continue;
            fs.barrels.add(new ExplosiveBarrel(gp, pos[0] * gp.tileSize, pos[1] * gp.tileSize));
        }
    }

    // ── Cari tile valid untuk spawn ───────────────────────────────────────────

    private int[] findValidTile() {
        for (int attempt = 0; attempt < 200; attempt++) {
            int col = random.nextInt(gp.maxWorldCol);
            int row = random.nextInt(gp.maxWorldRow);
            if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) continue;
            int tileNum = gp.tileM.mapTileNum[col][row];
            if (tileNum != -1
                    && gp.tileM.tile[tileNum] != null
                    && !gp.tileM.tile[tileNum].collision
                    && tileNum != 0 && tileNum != 16 && tileNum != 57) {
                return new int[]{col, row};
            }
        }
        return null;
    }

    // ── Spawn zombie di floor aktif ───────────────────────────────────────────

    private void spawnZombie() {
        int px = gp.getPlayer().x, py = gp.getPlayer().y;
        int margin = gp.tileSize * 10;

        for (int attempt = 0; attempt < 20; attempt++) {
            int spawnX, spawnY;
            switch (random.nextInt(4)) {
                case 0: spawnX = px + random.nextInt(gp.screenWidht) - gp.screenWidht / 2; spawnY = py - margin; break;
                case 1: spawnX = px + random.nextInt(gp.screenWidht) - gp.screenWidht / 2; spawnY = py + margin; break;
                case 2: spawnX = px - margin; spawnY = py + random.nextInt(gp.screenHeight) - gp.screenHeight / 2; break;
                default: spawnX = px + margin; spawnY = py + random.nextInt(gp.screenHeight) - gp.screenHeight / 2; break;
            }
            spawnX = Math.max(gp.tileSize, Math.min(spawnX, (gp.maxWorldCol - 2) * gp.tileSize));
            spawnY = Math.max(gp.tileSize, Math.min(spawnY, (gp.maxWorldRow - 2) * gp.tileSize));

            int col = spawnX / gp.tileSize, row = spawnY / gp.tileSize;
            if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) continue;
            int tileNum = gp.tileM.mapTileNum[col][row];
            if (tileNum == -1 || gp.tileM.tile[tileNum] == null || gp.tileM.tile[tileNum].collision) continue;

            gp.getActiveZombies().add(new Zombie(gp, spawnX, spawnY, currentLevel));
            return;
        }
    }

    // ── Level up ──────────────────────────────────────────────────────────────

    private void levelUp() {
        currentLevel++;
        zombiesSpawnedThisLevel = 0;
        maxZombiesPerLevel += 3;
        if (nextSpawnTime > 30) nextSpawnTime -= 15;
        System.out.println("--- NAIK KE LEVEL " + currentLevel + " ---");

        spawnChestsForFloor(1);
        spawnChestsForFloor(2);
        spawnBarrelsForFloor(1);
        spawnBarrelsForFloor(2);
    }
}
