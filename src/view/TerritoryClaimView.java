package view;

import controller.GameController;
import game.MapGrid;
import model.GameModel;
import player.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Placeholder territory claim screen. The real implementation will show the
 * map and let the round winner/loser click cells in the correct order.
 */
public class TerritoryClaimView extends JPanel {
    private static final int WINNER_CLAIMS = 2;
    private static final int LOSER_CLAIMS = 1;

    // pick order: index into players list, repeated WINNER_CLAIMS / LOSER_CLAIMS
    // times
    private int[] pickOrder; // e.g. [0, 1, 0] → player0 picks, then player1, then player0 again
    private int pickIndex; // current position in pickOrder

    private final GameController controller;

    // ── UI ──
    private final JLabel instructionLabel;
    private final JLabel p1Label;
    private final JLabel p2Label;
    private final JPanel gridPanel;
    private final JButton finishButton;

    // Keep references so we can re-enable / re-color cells when refresh() is called
    private JButton[][] cellButtons;

    public TerritoryClaimView(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 12));
        setBackground(MindWarsTheme.DARK_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Top: title + player score row ──
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);

        JLabel title = MindWarsTheme.centeredLabel(
                "Territory Claim",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE);
        top.add(title, BorderLayout.NORTH);

        JPanel scoreRow = new JPanel(new GridLayout(1, 2, 16, 0));
        scoreRow.setOpaque(false);
        p1Label = new JLabel("", SwingConstants.CENTER);
        p1Label.setFont(MindWarsTheme.BODY_BOLD);
        p1Label.setForeground(MindWarsTheme.PLAYER_X);
        p2Label = new JLabel("", SwingConstants.CENTER);
        p2Label.setFont(MindWarsTheme.BODY_BOLD);
        p2Label.setForeground(MindWarsTheme.PLAYER_O);
        scoreRow.add(p1Label);
        scoreRow.add(p2Label);
        top.add(scoreRow, BorderLayout.CENTER);

        instructionLabel = MindWarsTheme.centeredLabel(
                "", MindWarsTheme.BODY_FONT, MindWarsTheme.GRAY_LIGHT);
        top.add(instructionLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        add(gridPanel, BorderLayout.CENTER);

        // ── Bottom: finish button ──
        finishButton = MindWarsTheme.createGradientButton("Finish Round");
        finishButton.addActionListener(e -> controller.onTerritoryPhaseFinished());
        finishButton.setEnabled(false); // enabled only when all picks are done

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(finishButton);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        GameModel model = controller.getModel();
        List<Player> players = model.getPlayers();
        if (players == null || players.size() < 2)
            return;

        Player p0 = players.get(0);
        Player p1 = players.get(1);

        // ── Score labels ──
        p1Label.setText(p0.getName() + ": " + p0.getScore() + " pts");
        p2Label.setText(p1.getName() + ": " + p1.getScore() + " pts");

        // ── Determine pick order ──
        buildPickOrder(p0.getScore(), p1.getScore());
        pickIndex = 0;

        // ── Rebuild map grid ──
        rebuildGrid(model);

        updateInstruction(players);
        finishButton.setEnabled(false);

        revalidate();
        repaint();
    }
}
