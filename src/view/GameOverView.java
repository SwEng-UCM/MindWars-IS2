package view;

import controller.GameController;
import java.awt.*;
import javax.swing.*;
import player.Player;

/**
 * End-of-game screen. Shows the winner (or a draw) and a button to return
 * to the main menu.
 */
public class GameOverView extends JPanel {

    private final GameController controller;
    private final JLabel winnerLabel;

    public GameOverView(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());

        // Background
        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        // Main card
        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 320));

        // Title
        card.add(
            MindWarsTheme.centeredLabel(
                "Game Over",
                MindWarsTheme.TITLE_FONT,
                MindWarsTheme.PINK
            )
        );

        card.add(Box.createVerticalStrut(16));

        // Winner label
        winnerLabel = MindWarsTheme.centeredLabel(
            "",
            MindWarsTheme.HEADING_FONT,
            Color.BLACK
        );
        card.add(winnerLabel);

        card.add(Box.createVerticalStrut(28));

        // Buttons panel
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playAgain = MindWarsTheme.createGradientButton("Play Again");
        playAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgain.addActionListener(e -> controller.restartGame());

        JButton back = MindWarsTheme.createGradientButton("Back to Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> controller.onGameOverAcknowledged());

        buttons.add(playAgain);
        buttons.add(Box.createVerticalStrut(12));
        buttons.add(back);

        card.add(buttons);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    /** Refreshes the winner label against the current model state. */
    public void refresh() {
        // Persist the result to the leaderboard exactly once (#89).
        controller.recordGameOnLeaderboard();
        Player winner = controller.getModel().computeWinner();
        winnerLabel.setText(
            winner == null ? "It's a draw!" : winner.getName() + " wins!"
        );
    }
}
