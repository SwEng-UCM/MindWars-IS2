package view;

import util.SoundManager;

import javax.swing.*;
import java.awt.*;
import controller.NavigationController;

public class SettingsView extends JPanel {

    public SettingsView(NavigationController nav, SoundManager soundManager) {
        setLayout(new BorderLayout());

        SettingsPanel settingsPanel = new SettingsPanel(soundManager.getSettings(), soundManager);
        settingsPanel.getBackButton().addActionListener(e -> nav.showMainMenu());

        add(settingsPanel, BorderLayout.CENTER);
    }
}