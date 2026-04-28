package view;

import controller.GameController;
import java.awt.*;
import javax.swing.*;
import model.GameModel;
import player.Player;

/**
 * "Pass the device" screen. Used both between questions and before each
 * invasion attacker takes their turn. Shows whose turn it is and a ready
 * button.
 */
public class HotSeatView extends JPanel {

    private final GameController controller;
    private final boolean invasionMode;
    private final JLabel nameLabel;
    private final JLabel subLabel;
    private JButton diffBtn;

    public HotSeatView(GameController controller, boolean invasionMode) {
        this.controller = controller;
        this.invasionMode = invasionMode;

        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(380, 340));

        card.add(
                MindWarsTheme.centeredLabel(
                        invasionMode ? "Invasion Time" : "Pass the Device",
                        MindWarsTheme.HEADING_FONT,
                        MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(18));

        nameLabel = MindWarsTheme.centeredLabel(
                "",
                MindWarsTheme.TITLE_FONT,
                Color.BLACK);
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(8));

        subLabel = MindWarsTheme.centeredLabel(
                "",
                MindWarsTheme.BODY_FONT,
                MindWarsTheme.GRAY_TEXT);
        card.add(subLabel);
        card.add(Box.createVerticalStrut(24));

        this.diffBtn = MindWarsTheme.createPinkButton("Change Bot Difficulty");
        this.diffBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.diffBtn.addActionListener(e -> showBotDifficultyDialog());
        this.diffBtn.setVisible(false);
        card.add(this.diffBtn);
        card.add(Box.createVerticalStrut(10));

        JButton ready = MindWarsTheme.createGradientButton("I'm Ready");
        ready.setAlignmentX(Component.CENTER_ALIGNMENT);
        ready.addActionListener(e -> controller.onHotSeatReady());
        card.add(ready);

        if (!invasionMode) {
            card.add(Box.createVerticalStrut(10));
            JButton save = MindWarsTheme.createPinkButton("Save Game");
            save.setAlignmentX(Component.CENTER_ALIGNMENT);
            save.addActionListener(e -> onSave());
            card.add(save);
        }

        JButton quit = MindWarsTheme.createPinkButton("Quit");
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);
        quit.addActionListener(e -> System.exit(0));
        card.add(Box.createVerticalStrut(10));
        card.add(quit);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    private void showBotDifficultyDialog() {
        String[] options = { "Easy", "Medium", "Hard" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Bot Difficulty:",
                "Bot Settings",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice != JOptionPane.CLOSED_OPTION) {
            controller.onBotDifficultyChanged(options[choice]);
        }
    }

    private void onSave() {
        try {
            controller.saveGame();
            JOptionPane.showMessageDialog(
                    this,
                    "Game saved.",
                    "Save Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save: " + ex.getMessage(),
                    "Save Game",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Updates the display to reflect the current model state. */
    public void refresh() {
        GameModel model = controller.getModel();
        Player p = invasionMode ? model.getInvader() : model.getCurrentPlayer();
        nameLabel.setText(p.getName());
        subLabel.setText(
                invasionMode
                        ? "Prepare to attack"
                        : "Round " +
                                model.getRoundNumber() +
                                " of " +
                                model.getTotalRounds());

        if (model.getSettings() != null) {
            // button should only show in non-invasion vs bot mode, and for humans
            boolean showButton = !invasionMode && model.getSettings().vsBot && !p.isBot();
            this.diffBtn.setVisible(showButton);
        }
        // Bot players don't need to press Ready — skip automatically.
        if (!invasionMode && p.isBot()) {
            Timer skip = new Timer(600, e -> controller.onHotSeatReady());
            skip.setRepeats(false);
            skip.start();
        }
    }
}
