package ui;

import javax.sound.sampled.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SoundManager — menggunakan pola loadClip() yang sama persis dengan MainMenuPanel.
 * Coba getResourceAsStream dulu, fallback ke new File("soundEffects", name+".wav").
 *
 * Tambahan: setMasterVolume(float) untuk mengatur volume BGM secara real-time.
 */
public class SoundManager {

    private final Map<String, byte[]> audioCache = new HashMap<>();
    private Clip bgmClip;

    private final Map<String, Long> sfxCooldown = new HashMap<>();
    private static final long SFX_MIN_INTERVAL_MS = 80;

    /** Volume master saat ini, 0.0 (senyap) – 1.0 (penuh). */
    private float masterVolume = 1.0f;

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
        preload("gameOver");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VOLUME
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Atur master volume BGM secara real-time.
     * @param volume 0.0 (senyap) sampai 1.0 (penuh)
     */
    public void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        applyVolumeToBGM();
        System.out.printf("[Sound] Master volume: %.0f%%%n", masterVolume * 100);
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    /** Terapkan masterVolume ke bgmClip yang sedang berjalan. */
    private void applyVolumeToBGM() {
        if (bgmClip == null) return;
        applyVolumeToClip(bgmClip, masterVolume);
    }

    private void applyVolumeToClip(Clip clip, float volume) {
        if (clip == null) return;
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float safeVolume = Math.max(0.0001f, Math.min(1.0f, volume));
            float db = volume <= 0.0f ? fc.getMinimum() : (float) (20.0 * Math.log10(safeVolume));
            fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), db)));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PRELOAD
    // ─────────────────────────────────────────────────────────────────────────

    private void preload(String name) {
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

    // ─────────────────────────────────────────────────────────────────────────
    //  SFX
    // ─────────────────────────────────────────────────────────────────────────

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
        final float vol = masterVolume;
        Thread t = new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                        new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(finalData)));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                applyVolumeToClip(clip, vol);
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Mainkan SFX dengan volume dikecilkan (0.0 = senyap, 1.0 = normal).
     */
    public void playSFXQuiet(String name, float volume) {
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
        final float vol = Math.max(0.0f, Math.min(1f, volume)) * masterVolume;
        Thread t = new Thread(() -> {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                        new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(finalData)));
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                applyVolumeToClip(clip, vol);
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BGM
    // ─────────────────────────────────────────────────────────────────────────

    public void playBGM(String name) {
        stopBGM();
        Thread t = new Thread(() -> {
            // Coba dari cache dulu (sudah di-preload)
            byte[] cached = audioCache.get(name);
            if (cached != null) {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(
                            new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(cached)));
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(ais);
                    applyVolumeToBGM();
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                    System.out.println("[Sound] BGM mulai (cache): " + name);
                    return;
                } catch (Exception ignored) {}
            }

            // Fallback: stream dari resource
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
                    applyVolumeToBGM();
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                    System.out.println("[Sound] BGM mulai (resource): " + name);
                    return;
                } catch (Exception ignored) {}
            }

            File f = new File("soundEffects", name + ".wav");
            if (f.exists()) {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(ais);
                    applyVolumeToBGM();
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