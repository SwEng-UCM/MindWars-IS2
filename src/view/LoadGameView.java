package view;

import controller.GameController;
import controller.NavigationController;
import model.GameMemento;
import model.GameMementoStore;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Load-game screen. Shows the metadata of the single save slot (timestamp,
 * players, round) and lets the user load it, delete it, or cancel.
 */
public class LoadGameView extends JPanel {

    private final GameController controller;
    private final JLabel infoLabel;
    private final JButton loadButton;
    private final JButton deleteButton;

    public LoadGameView(GameController controller, NavigationController nav) {
        this.controller = controller;

        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 360));

        card.add(MindWarsTheme.centeredLabel("Load Game",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(18));

        infoLabel = MindWarsTheme.centeredLabel(" ",
                MindWarsTheme.BODY_FONT, MindWarsTheme.GRAY_TEXT);
        card.add(infoLabel);
        card.add(Box.createVerticalStrut(24));

        loadButton = MindWarsTheme.createGradientButton("Load");
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.addActionListener(e -> onLoad());
        card.add(loadButton);
        card.add(Box.createVerticalStrut(10));

        deleteButton = MindWarsTheme.createPinkButton("Delete Save");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(e -> onDelete());
        card.add(deleteButton);
        card.add(Box.createVerticalStrut(10));

        JButton back = MindWarsTheme.createPinkButton("Back to Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> nav.showMainMenu());
        card.add(back);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    /** Re-reads slot metadata from disk. Call before showing the screen. */
    public void refresh() {
        GameMementoStore store = controller.getMementoStore();
        if (!store.hasSave()) {
            infoLabel.setText("<html><center>No saved game found.</center></html>");
            loadButton.setEnabled(false);
            deleteButton.setEnabled(false);
            return;
        }
        try {
            GameMemento m = store.load();
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < m.players.size(); i++) {
                if (i > 0) names.append(" vs ");
                names.append(m.players.get(i).name);
            }
            infoLabel.setText("<html><center>"
                    + "Saved: " + escape(m.savedAt) + "<br>"
                    + escape(names.toString()) + "<br>"
                    + "Round " + (m.roundIndex + 1)
                    + "</center></html>");
            loadButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } catch (IOException ex) {
            infoLabel.setText("<html><center>Save file is unreadable:<br>"
                    + escape(ex.getMessage()) + "</center></html>");
            loadButton.setEnabled(false);
            deleteButton.setEnabled(true);
        }
    }

    private void onLoad() {
        try {
            controller.loadGame();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load: " + ex.getMessage(),
                    "Load Game",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the saved game?",
                "Delete Save",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            controller.getMementoStore().delete();
            refresh();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete: " + ex.getMessage(),
                    "Delete Save",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
