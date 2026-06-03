package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class UIaroundUS extends JPanel implements KeyListener {

    // 1. Variabel Data
    int hp = 100;
    int maxHp = 100;
    int skor = 60;
    int halaman = 1; // 1 untuk menu, 2 untuk game, 3 untuk mati

    // Variabel Gambar
    Image imgMenu;

    ui.Sound Musik = new ui.Sound();

    public UIaroundUS() {
        this.setFocusable(true);
        this.addKeyListener(this);

        // PANGGIL GAMBAR DI SINI
        imgMenu = new ImageIcon("assets/DarkForest.jpg").getImage();

        Musik.playBGM("assets/Before Play.wav");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (halaman == 1) {
            // Gambar Background Menu
            if (imgMenu != null) {
                g2d.drawImage(imgMenu, 0, 0, getWidth(), getHeight(), null);
            }

            Font fontJudul = new Font("Arial", Font.BOLD, 30);
            g2d.setFont(fontJudul);
            g2d.setColor(Color.RED);
            FontMetrics metrics = g2d.getFontMetrics(fontJudul);
            String teksJudul = "AROUND US";

            int x = (getWidth() - metrics.stringWidth(teksJudul)) / 2;
            int y = (getHeight() / 2);
            g2d.drawString(teksJudul, x, y);

            g2d.setFont(new Font("Arial", Font.PLAIN, 15));
            g2d.setColor(Color.WHITE);
            String teksEnter = "Press ENTER to Start";
            FontMetrics metricsEnter = g2d.getFontMetrics(g2d.getFont());
            int xEnter = (getWidth() - metricsEnter.stringWidth(teksEnter)) / 2;
            g2d.drawString(teksEnter, xEnter, y + 40);

        } else if (halaman == 2) {
            // GAMBAR GAMEPLAY
            g2d.setColor(Color.WHITE);
            g2d.drawRect(20, 20, 200, 25);
            g2d.setColor(Color.RED);
            g2d.fillRect(20, 20, 200, 25);
            g2d.setColor(Color.GREEN);
            int barWidth = (int) ((double) hp / maxHp * 200);
            g2d.fillRect(20, 20, barWidth, 25);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("HP: " + hp + " / " + maxHp, 20, 60);
            g2d.drawString("A: Damage | S: Heal", 20, 90);

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("SKOR: " + skor, 250, 50);

        } else if (halaman == 3) {
            // GAMBAR GAME OVER
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            String teksMati = "GAME OVER";
            int xMati = (getWidth() - g2d.getFontMetrics().stringWidth(teksMati)) / 2;
            g2d.drawString(teksMati, xMati, getHeight() / 2 - 50);

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 25));
            String teksSkor = "FINAL SCORE: " + skor;
            int xSkor = (getWidth() - g2d.getFontMetrics().stringWidth(teksSkor)) / 2;
            g2d.drawString(teksSkor, xSkor, getHeight() / 2 + 20);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 15));
            String teksRestart = "Press 'R' for Restart";
            int xRestart = (getWidth() - g2d.getFontMetrics().stringWidth(teksRestart)) / 2;
            g2d.drawString(teksRestart, xRestart, getHeight() / 2 + 80);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 1. PINDAH DARI MENU (HALAMAN 1) KE GAMEPLAY (HALAMAN 2)
        if (halaman == 1 && key == KeyEvent.VK_ENTER) {
            // Ganti halaman
            halaman = 2;
            hp = 100; // Reset HP pas mulai main

            // Pindah Musik!
            Musik.stop(); // Matikan lagu menu
            Musik.playBGM("assets/bgm_arena.wav"); // Putar lagu gameplay yang tegang!
        }

        // 2. LOGIKA SELAMA MAIN (HALAMAN 2)
        else if (halaman == 2) {
            if (key == KeyEvent.VK_A) hp -= 20;
            if (key == KeyEvent.VK_S) {
                hp += 10;
                if (hp > maxHp) hp = maxHp;
            }

            // CEK KONDISI MATI -> PINDAH KE GAME OVER (HALAMAN 3)
            if (hp <= 0) {
                halaman = 3;

                // Pindah Musik!
                Musik.stop(); // Matikan lagu gameplay
                Musik.playBGM("assets/Game Over.wav");

            }
        }

        // 3. RESTART DARI GAME OVER (HALAMAN 3) BALIK KE MENU (HALAMAN 1)
        else if (halaman == 3) {
            if (key == KeyEvent.VK_R) {
                halaman = 1; // Balik ke menu
                hp = 100;
                skor = 0;

                // Pindah Musik!
                Musik.stop(); // Matikan suara game over
                Musik.playBGM("assets/Before Play.wav"); // Putar lagi lagu menu utama
            }
        }

        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // MAIN METHOD HARUS DI SINI (DI DALAM CLASS)
    public static void main(String[] args) {
        JFrame frame = new JFrame("Around Us - Lab 5");
        UIaroundUS panel = new UIaroundUS();
        frame.add(panel);
        frame.setSize(800, 600); // Ukuran lebih besar biar lega
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}