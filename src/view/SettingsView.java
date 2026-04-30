package view;

import util.SoundManager;

import javax.swing.*;
import java.awt.*;
import controller.NavigationController;

public class SettingsView extends JPanel {

    public SettingsView(NavigationController nav, SoundManager soundManager) {
        setLayout(new BorderLayout());
        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(440, 600));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        SettingsPanel settingsPanel = new SettingsPanel(soundManager.getSettings(), soundManager);
        settingsPanel.getBackButton().addActionListener(e -> nav.showMainMenu());
        card.add(settingsPanel, BorderLayout.CENTER);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }
}