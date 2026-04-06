package view;

import controller.GameController;
import player.Player;

import javax.swing.*;
import java.awt.*;

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

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 320));

        card.add(MindWarsTheme.centeredLabel("Game Over",
                MindWarsTheme.TITLE_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(16));

        winnerLabel = MindWarsTheme.centeredLabel("",
                MindWarsTheme.HEADING_FONT, Color.BLACK);
        card.add(winnerLabel);
        card.add(Box.createVerticalStrut(28));

        JButton back = MindWarsTheme.createGradientButton("Back to Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> controller.onGameOverAcknowledged());
        card.add(back);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    /** Refreshes the winner label against the current model state. */
    public void refresh() {
        Player winner = controller.getModel().computeWinner();
        winnerLabel.setText(winner == null ? "It's a draw!" : winner.getName() + " wins!");
    }
}
