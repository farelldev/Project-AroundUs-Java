package main;

public class LevelManager {
    GamePanel gp;

    // Status Level
    public int currentLevel = 1;
    public boolean isLevelActive = true;

    // Sistem Waktu (Mengandalkan FPS, 60 frame = 1 detik)
    int spawnTimer = 0;
    int nextSpawnTime = 120; // Default: Spawn zombie setiap 2 detik

    // Sistem Target Level
    int zombiesSpawnedThisLevel = 0;
    int maxZombiesPerLevel = 5; // Level 1 butuh spawn 5 zombie

    public LevelManager(GamePanel gp) {
        this.gp = gp;
    }

    public void update() {
        if (!isLevelActive) return;

        spawnTimer++;

        // 1. Logika Spawn Zombie
        if (spawnTimer >= nextSpawnTime && zombiesSpawnedThisLevel < maxZombiesPerLevel) {
            spawnZombie();
            spawnTimer = 0; // Reset timer setelah spawn
            zombiesSpawnedThisLevel++;
        }

        // 2. Logika Naik Level
        // Cek jika semua target zombie sudah di-spawn DAN semua zombie di layar sudah mati
        // (Asumsi kamu nanti punya ArrayList bernama zombieList di GamePanel)
        if (zombiesSpawnedThisLevel >= maxZombiesPerLevel /* && gp.zombieList.isEmpty() */) {
            levelUp();
        }
    }

    private void spawnZombie() {
        // Logika sederhana untuk spawn di luar area layar kiri atau kanan
        int spawnX = (Math.random() < 0.5) ? -50 : gp.screenWidht + 50;
        int spawnY = (int)(Math.random() * gp.screenHeight); // Y acak

        System.out.println("Zombie muncul! Level: " + currentLevel);
    }

    private void levelUp() {
        currentLevel++;
        zombiesSpawnedThisLevel = 0;

        maxZombiesPerLevel += 3; // Level 2: 8 zombie, Level 3: 11 zombie

        if(nextSpawnTime > 30) {
            nextSpawnTime -= 15;
        }

        System.out.println("--- NAIK KE LEVEL " + currentLevel + " ---");
    }
}