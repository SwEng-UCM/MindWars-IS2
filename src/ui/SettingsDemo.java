package ui;

import javax.swing.*;
import java.awt.*;


public class SettingsDemo {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            // Shared settings object
            GameSettings settings = new GameSettings();

            // Shared sound manager that reads those settings
            SoundManager soundManager = new SoundManager(settings);

            // Main test window
            JFrame frame = new JFrame("MindWars Settings Live Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 900);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Settings UI in the center
            SettingsPanel settingsPanel = new SettingsPanel(settings, soundManager);
            frame.add(settingsPanel, BorderLayout.CENTER);

            JLabel infoLabel = new JLabel(
                    "Music starts automatically. A sample sound effect plays every 4 seconds.",
                    SwingConstants.CENTER
            );
            infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.add(infoLabel, BorderLayout.SOUTH);

            frame.setVisible(true);

            
            soundManager.startBackground();

        
            Timer effectsTimer = new Timer(4000, e -> {
                System.out.println("Trying sample sound effect. Enabled = " + settings.isSoundEffectsEnabled());
                soundManager.play(SoundManager.CORRECT);
            });

            effectsTimer.start();
        });
    }
}