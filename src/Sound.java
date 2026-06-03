package ui;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sound {

    // Simpan clip audio
    private Clip clip;


    public void playBGM(String filePath) {

        stop();

        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Waduh, gagal muter musik di path: " + filePath);
            e.printStackTrace();
        }
    }


    public void playSFX(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            Clip sfxClip = AudioSystem.getClip();
            sfxClip.open(audioStream);
            sfxClip.start(); // Langsung jalan sekali saja

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Gagal muter SFX: " + filePath);
        }
    }


    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close(); // Dikosongkan biar RAM laptop gak jebol
        }
    }

    // Sound GamePlay

    // 1. Senjata & Pertempuran
    public void playShootDeagle() {
        playSFX("assets/shoot_deagle.wav");
    }

    public void playEmptyClick() {
        playSFX("assets/empty_click.wav");
    }

    public void playReloadMag() {
        playSFX("assets/reload_mag.wav");
    }

    public void playHitFlesh() {
        playSFX("assets/hit_flesh.wav");
    }

    public void playHitWall() {
        playSFX("assets/hit_wall.wav");
    }

    // 2. Player
    public void playPlayerHurt() {
        playSFX("assets/player_hurt.wav");
    }

    public void playPlayerDeath() {
        playSFX("assets/player_death.wav");
    }

    // 3. Zombies
    public void playZombieIdle() {
        playSFX("assets/zombie_idle.wav");
    }

    public void playZombieAlert() {
        playSFX("assets/zombie_alert.wav");
    }

    public void playZombieHurt() {
        playSFX("assets/zombie_hurt.wav");
    }

    public void playZombieDeath() {
        playSFX("assets/zombie_death.wav");
    }

    // 4. Sistem, UI & Lingkungan
    public void playWaveStart() {
        playSFX("assets/wave_start.wav");
    }

    public void playUiClick() {
        playSFX("assets/ui_click.wav");
    }

    public void playStepGrass() {
        playSFX("assets/step_grass.wav");
    }

    public void playBgmArena() {
        playBGM("assets/bgm_arena.wav"); // Menggunakan playBGM agar otomatis looping tanpa henti
    }

}