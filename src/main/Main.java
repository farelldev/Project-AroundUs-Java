package main;

import ui.MainMenuPanel;
import ui.SoundManager;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setTitle("Around Us: Apocalypse");

            // Buat panel kontainer agar bisa ganti scene
            CardLayout cardLayout = new CardLayout();
            JPanel root = new JPanel(cardLayout);

            SoundManager soundManager = new SoundManager();
            GamePanel gamePanel = new GamePanel(soundManager);

            // MainMenuPanel — akan memanggil callback ini saat animasi flicker selesai
            MainMenuPanel menuPanel = new MainMenuPanel(() -> {
                // Ini dijalankan di EDT via SwingUtilities.invokeLater dari MainMenuPanel
                cardLayout.show(root, "GAME");
                gamePanel.requestFocusInWindow();
                gamePanel.startGameThread();
            }, soundManager);

            root.add(menuPanel, "MENU");
            root.add(gamePanel, "GAME");

            window.add(root);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            // Tampilkan menu dulu
            cardLayout.show(root, "MENU");
            menuPanel.requestFocusInWindow();
            menuPanel.startLoop();
        });
    }
}
