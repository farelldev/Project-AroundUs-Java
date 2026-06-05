package ui;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SoundManager — menggunakan pola loadClip() yang sama persis dengan MainMenuPanel.
 * Coba getResourceAsStream dulu, fallback ke new File("soundEffects", name+".wav").
 */
public class SoundManager {

    private final Map<String, byte[]> audioCache = new HashMap<>();
    private Clip bgmClip;

    private final Map<String, Long> sfxCooldown = new HashMap<>();
    private static final long SFX_MIN_INTERVAL_MS = 80;

    public SoundManager() {
        preload("ak_shot");
        preload("deagle_shot");
        preload("empty_mag");
        preload("flesh_hit");
        preload("hit_wall");
        preload("player_hurt");
        preload("player_death");
        preload("reload");
        preload("zombie_alert");
        preload("zombie_idle");
        preload("uiClick");
    }

    /**
     * Load audio ke memory. Coba resource path dulu, fallback ke File — sama seperti MainMenuPanel.loadClip().
     */
    private void preload(String name) {
        // 1. Coba via getResourceAsStream (beberapa path kandidat)
        String[] resourcePaths = {
            "/soundEffects/" + name + ".wav",
            "/" + name + ".wav"
        };
        for (String path : resourcePaths) {
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is == null) continue;
                audioCache.put(name, is.readAllBytes());
                System.out.println("[Sound] Loaded (resource): " + name);
                return;
            } catch (Exception ignored) {}
        }

        // 2. Fallback: new File("soundEffects", name+".wav") — cara yang berhasil di MainMenuPanel
        File f = new File("soundEffects", name + ".wav");
        if (f.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                audioCache.put(name, fis.readAllBytes());
                System.out.println("[Sound] Loaded (file): " + name);
                return;
            } catch (Exception e) {
                System.out.println("[Sound] Gagal baca file: " + f.getPath());
            }
        }

        System.out.println("[Sound] Tidak ditemukan: " + name + ".wav");
    }

    public void playSFX(String name) {
        long now  = System.currentTimeMillis();
        Long last = sfxCooldown.get(name);
        if (last != null && now - last < SFX_MIN_INTERVAL_MS) return;
        sfxCooldown.put(name, now);

        byte[] data = audioCache.get(name);
        if (data == null) {
            preload(name);
            data = audioCache.get(name);
            if (data == null) return;
        }

        final byte[] finalData = data;
        Thread t = new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(finalData)));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();
    }

    public void playBGM(String name) {
        stopBGM();
        Thread t = new Thread(() -> {
            // 1. Coba resource
            String[] resourcePaths = {
                "/soundEffects/" + name + ".wav",
                "/" + name + ".wav"
            };
            for (String path : resourcePaths) {
                try {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is == null) continue;
                    AudioInputStream ais = AudioSystem.getAudioInputStream(is);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(ais);
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                    System.out.println("[Sound] BGM mulai (resource): " + name);
                    return;
                } catch (Exception ignored) {}
            }

            // 2. Fallback ke File
            File f = new File("soundEffects", name + ".wav");
            if (f.exists()) {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(ais);
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                    System.out.println("[Sound] BGM mulai (file): " + name);
                } catch (Exception e) {
                    System.out.println("[Sound] Gagal main BGM: " + name);
                }
            } else {
                System.out.println("[Sound] BGM tidak ditemukan: " + name);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }
}
