package view;

import controller.GameController;
import player.Player;
import javax.swing.*;
import java.awt.*;

public class BettingView extends JPanel {
    private final GameController controller;
    private final JSlider wagerSlider;
    private final JLabel wagerLabel;
    private final JLabel infoLabel;

    public BettingView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 480));

        card.add(MindWarsTheme.centeredLabel("SPECIAL BET", MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(10));

        infoLabel = MindWarsTheme.centeredLabel("Final Round Opportunity", MindWarsTheme.BODY_FONT, Color.GRAY);
        card.add(infoLabel);
        card.add(Box.createVerticalStrut(25));

        wagerLabel = MindWarsTheme.centeredLabel("Wager: 0 points", MindWarsTheme.TITLE_FONT, Color.BLACK);
        card.add(wagerLabel);
        card.add(Box.createVerticalStrut(15));

        wagerSlider = new JSlider(0, 100, 0);
        wagerSlider.setOpaque(false);
        wagerSlider.setPaintTicks(true);
        wagerSlider.addChangeListener(e -> wagerLabel.setText("Wager: " + wagerSlider.getValue() + " points"));
        card.add(wagerSlider);

        card.add(Box.createVerticalStrut(35));

        JButton confirm = MindWarsTheme.createGradientButton("Confirm Bet");
        confirm.addActionListener(e -> controller.onWagerConfirmed(wagerSlider.getValue()));
        card.add(confirm);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    public void refresh() {
        Player p = controller.getModel().getCurrentPlayer();
        int score = p.getScore();
        wagerSlider.setMaximum(score);
        wagerSlider.setValue(0);
        wagerLabel.setText("Wager: 0 points");
        infoLabel.setText(p.getName() + ", you have " + score + " points available.");
    }
}