package view;

import controller.GameController;
import player.Player;
import java.awt.*;
import javax.swing.*;
import java.util.List;

public class EstimationResultsView extends JPanel {
    private final JPanel resultsPanel;
    private final JLabel winnerLabel;

    public EstimationResultsView(GameController controller) {
        setLayout(new BorderLayout());
        setBackground(MindWarsTheme.DARK_BG);

        winnerLabel = new JLabel("Winner: ...", SwingConstants.CENTER);
        winnerLabel.setFont(MindWarsTheme.HEADING_FONT);
        winnerLabel.setForeground(MindWarsTheme.CORRECT_GREEN);

        resultsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        resultsPanel.setOpaque(false);

        JButton continueBtn = MindWarsTheme.createGradientButton("Continue to Board");
        continueBtn.addActionListener(e -> controller.onResultsAcknowledged());

        add(winnerLabel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(continueBtn, BorderLayout.SOUTH);
    }

    public void displayResults(double correctAns, List<NumericResponse> data, Player winner) {
        resultsPanel.removeAll();
        winnerLabel.setText("WINNER: " + (winner != null ? winner.getName() : "Draw"));

        for (NumericResponse res : data) {
            int diff = (int) Math.abs(res.value - correctAns);
            JLabel lbl = new JLabel(String.format("%s: Guess %d | Diff %d | Time %.2fs",
                    res.player.getName(), (int) res.value, diff, res.timeTaken / 1000.0));
            lbl.setForeground(Color.WHITE);
            resultsPanel.add(lbl);
        }
    }
}
