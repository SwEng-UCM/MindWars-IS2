package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder game board view. The real implementation will show the map
 * grid, the current question, the timer bar, and score panels for both
 * players (addresses #66). For now this is a stub so the CardLayout wiring
 * compiles; gameplay logic will land in a follow-up.
 */
public class GameBoardView extends JPanel {

    private final JLabel status;

    public GameBoardView(GameController controller, boolean invasionMode) {
        setLayout(new BorderLayout());
        setBackground(MindWarsTheme.DARK_BG);

        status = MindWarsTheme.centeredLabel(
                invasionMode ? "Invasion battle (stub)" : "Question screen (stub)",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE);
        add(status, BorderLayout.CENTER);
    }

    /** Refreshes all widgets against the current model state. */
    public void refresh() {
        // Stub: real implementation pulls question, scores, map from model.
    }

    /** Starts the 15-second countdown Swing timer. */
    public void startTimer() {
        // Stub: real implementation will drive the progress bar.
    }
}
