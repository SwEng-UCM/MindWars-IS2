package view;

import ui.GameSettings;
import ui.SettingsPanel;
import ui.SoundManager;

import javax.swing.*;
import java.awt.*;
import controller.NavigationController;

public class SettingsView extends JPanel {

    public SettingsView(NavigationController nav) {
        setLayout(new BorderLayout());

        GameSettings gameSettings = new GameSettings();
        SoundManager soundManager = new SoundManager(gameSettings);
        SettingsPanel settingsPanel = new SettingsPanel(gameSettings, soundManager);

        settingsPanel.getBackButton().addActionListener(e -> nav.showMainMenu());

        add(settingsPanel, BorderLayout.CENTER);
    }
}