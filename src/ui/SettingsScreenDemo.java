package ui;

import javax.swing.SwingUtilities;

public class SettingsScreenDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameSettings settings = new GameSettings();
            SoundManager soundManager = new SoundManager(settings);

            SettingsScreen screen = new SettingsScreen(settings, soundManager);
            screen.setVisible(true);
        });
    }
}