package view;

import controller.GameController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Placeholder invasion selection screen. Will let the invader pick one of
 * their own cells to attack from and an enemy-adjacent target cell.
 */
public class InvasionSelectView extends JPanel {

    public InvasionSelectView(GameController controller) {
        setLayout(new BorderLayout(0, 12));
        setBackground(MindWarsTheme.DARK_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel label = MindWarsTheme.centeredLabel(
                "Invasion Select (stub)",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE);
        add(label, BorderLayout.CENTER);

        JButton backButton = MindWarsTheme.createPinkButton("Back to Menu");
        backButton.addActionListener(e -> controller.getNav().showMainMenu());

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(backButton);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        /* stub */ }
}
