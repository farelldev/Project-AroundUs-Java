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

            CardLayout cardLayout = new CardLayout();
            JPanel root = new JPanel(cardLayout);

            SoundManager soundManager = new SoundManager();
            GamePanel gamePanel = new GamePanel(soundManager);

            MainMenuPanel menuPanel = new MainMenuPanel(() -> {
                cardLayout.show(root, "GAME");
                gamePanel.requestFocusInWindow();
                gamePanel.startGameThread();
            }, soundManager);

            // Inject callback "kembali ke menu" ke GamePanel
            gamePanel.setOnBackToMainMenu(() -> {
                cardLayout.show(root, "MENU");
                menuPanel.requestFocusInWindow();
                menuPanel.stopLoop();
                menuPanel.resetToStart();
                menuPanel.startLoop();
                menuPanel.restartBGM();
            });

            root.add(menuPanel, "MENU");
            root.add(gamePanel, "GAME");

            window.add(root);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            cardLayout.show(root, "MENU");
            menuPanel.requestFocusInWindow();
            menuPanel.startLoop();
        });
    }
}