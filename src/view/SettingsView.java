package view;

import util.AudioSettings;
import util.SoundManager;

import javax.swing.*;
import java.awt.*;
import controller.NavigationController;

public class SettingsView extends JPanel {

    public SettingsView(NavigationController nav) {
        setLayout(new BorderLayout());

        AudioSettings audioSettings = new AudioSettings();
        SoundManager soundManager = new SoundManager(audioSettings);
        SettingsPanel settingsPanel = new SettingsPanel(audioSettings, soundManager);

        settingsPanel.getBackButton().addActionListener(e -> nav.showMainMenu());

        add(settingsPanel, BorderLayout.CENTER);
    }
}