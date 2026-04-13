package view;

import controller.NavigationController;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu screen. Six gradient buttons that route to other screens
 * via the {@link NavigationController}.
 */
public class MainMenuView extends JPanel {

    public MainMenuView(NavigationController nav) {
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel title = MindWarsTheme.centeredLabel("MindWars",
                MindWarsTheme.TITLE_FONT, MindWarsTheme.PINK);
        JLabel subtitle = MindWarsTheme.centeredLabel("Trivia Battle",
                MindWarsTheme.SUBTITLE_FONT, MindWarsTheme.GRAY_TEXT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));

        card.add(menuButton("New Game", nav::showGameSetup));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton("Load Game", nav::showLoadGame));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton("Leaderboard", nav::showLeaderboard));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton("Rules", nav::showRules));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton("Settings", nav::showSettings));
        card.add(Box.createVerticalStrut(12));
        card.add(menuButton("Quit", () -> System.exit(0)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        card.setPreferredSize(new Dimension(380, 560));
        bg.add(card, gbc);

        add(bg, BorderLayout.CENTER);
    }

    private JButton menuButton(String text, Runnable action) {
        JButton btn = MindWarsTheme.createGradientButton(text);
        btn.addActionListener(e -> action.run());
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
