package view;

import controller.GameController;
import model.GameSettings;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder setup view. Will become a multi-step wizard (addresses #74).
 * For now it just launches a game with sensible defaults so the rest of the
 * MVC wiring can be exercised.
 */
public class GameSetupView extends JPanel {

    public GameSetupView(GameController controller) {
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 420));

        card.add(MindWarsTheme.centeredLabel("New Game",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(16));
        card.add(MindWarsTheme.centeredLabel("(setup wizard – coming soon)",
                MindWarsTheme.SMALL_FONT, MindWarsTheme.GRAY_TEXT));
        card.add(Box.createVerticalStrut(24));

        JButton start = MindWarsTheme.createGradientButton("Quick Start (4x4, random)");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.addActionListener(e -> controller.startNewGame(
                new GameSettings(4, false, "Player 1", "Player 2", true, null, null)));
        card.add(start);

        card.add(Box.createVerticalStrut(12));

        JButton back = MindWarsTheme.createPinkButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> controller.returnToMenu());
        card.add(back);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    /** Called by the navigation layer before showing the view. */
    public void reset() { /* no-op for placeholder */ }
}
