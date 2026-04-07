package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder invasion selection screen. Will let the invader pick one of
 * their own cells to attack from and an enemy-adjacent target cell.
 */
public class InvasionSelectView extends JPanel {

    public InvasionSelectView(GameController controller) {
        setLayout(new BorderLayout());
        setBackground(MindWarsTheme.DARK_BG);

        JLabel label = MindWarsTheme.centeredLabel(
                "Invasion Select (stub)",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE);
        add(label, BorderLayout.CENTER);
    }

    public void refresh() { /* stub */ }
}
