package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder territory claim screen. The real implementation will show the
 * map and let the round winner/loser click cells in the correct order.
 */
public class TerritoryClaimView extends JPanel {

    public TerritoryClaimView(GameController controller) {
        setLayout(new BorderLayout());
        setBackground(MindWarsTheme.DARK_BG);

        JLabel label = MindWarsTheme.centeredLabel(
                "Territory Claim (stub)",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE);
        add(label, BorderLayout.CENTER);

        JButton done = MindWarsTheme.createGradientButton("Finish Round");
        done.addActionListener(e -> controller.onTerritoryPhaseFinished());
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(done);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        /* stub */ }
}
