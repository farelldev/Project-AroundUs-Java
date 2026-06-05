package ui;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sound {

    // Tempat menyimpan klip audio yang sedang berjalan
    private Clip clip;

    /**
     * Method untuk memutar Musik Latar (BGM) secara terus-menerus
     * @param filePath Alamat file audio .wav di folder assets
     */
    public void playBGM(String filePath) {
        // Kita stop dulu musik sebelumnya jika ada yang masih menyala
        stop();

        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Loop terus-menerus tanpa henti cocok untuk BGM
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Waduh, gagal muter musik di path: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Method untuk memutar Efek Suara (SFX) - Cuma bunyi sekali tanpa looping
     * Cocok untuk suara ketembak, klik menu, atau pas mati.
     */
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

    /**
     * Method untuk mematikan musik yang sedang berjalan
     */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close(); // Dikosongkan biar RAM laptop gak jebol
        }
    }
}