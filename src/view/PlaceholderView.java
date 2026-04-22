package view;

import controller.NavigationController;

import javax.swing.*;
import java.awt.*;

/**
 * Minimal screen used as a stub for features not yet implemented. Shows a
 * title, a subtitle and a Back button that returns to the main menu.
 */

public class PlaceholderView extends JPanel {

    public PlaceholderView(String title, String subtitle, NavigationController nav) {
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 320));

        card.add(MindWarsTheme.centeredLabel(title,
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(12));
        card.add(MindWarsTheme.centeredLabel(subtitle,
                MindWarsTheme.BODY_FONT, MindWarsTheme.GRAY_TEXT));
        card.add(Box.createVerticalStrut(24));

        JButton back = MindWarsTheme.createPinkButton("Back to Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> nav.showMainMenu());
        card.add(back);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }
}
