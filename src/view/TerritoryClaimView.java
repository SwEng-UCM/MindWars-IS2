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
 * Territory claim screen shown after each round's Q&A phase.
 *
 * <p>
 * Rules:
 * <ul>
 * <li>The round winner gets {@link #WINNER_CLAIMS} cell picks first.</li>
 * <li>The round loser (or the tied second player) gets {@link #LOSER_CLAIMS}
 * pick.</li>
 * <li>If scores are tied, each player gets {@link #LOSER_CLAIMS} pick.</li>
 * <li>Picks alternate: winner picks one → loser picks one → winner picks second
 * (if any).</li>
 * <li>A cell can only be claimed if it is currently empty ('.').</li>
 * </ul>
 *
 * <p>
 * The grid is rebuilt via {@link #refresh()} whenever the model changes.
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

        // ── Centre: grid ──
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

    private void buildPickOrder(int score0, int score1) {
        if (score0 == score1) {
            // Tied → each gets LOSER_CLAIMS picks, alternating
            int total = LOSER_CLAIMS * 2;
            pickOrder = new int[total];
            for (int i = 0; i < total; i++) {
                pickOrder[i] = i % 2; // 0,1,0,1,…
            }
            return;
        }

        int winner = score0 >= score1 ? 0 : 1;
        int loser = 1 - winner;

        // Interleave: winner, loser, winner (for WINNER=2, LOSER=1)
        // Generalised: distribute loser picks as evenly as possible between winner
        // picks
        int total = WINNER_CLAIMS + LOSER_CLAIMS;
        pickOrder = new int[total];
        int wi = 0, li = 0, idx = 0;
        // Interleave winner first, then loser, repeating
        boolean winnerTurn = true;
        while (wi < WINNER_CLAIMS || li < LOSER_CLAIMS) {
            if (winnerTurn && wi < WINNER_CLAIMS) {
                pickOrder[idx++] = winner;
                wi++;
                // After every winner pick, let loser go once
                if (li < LOSER_CLAIMS) {
                    pickOrder[idx++] = loser;
                    li++;
                }
            } else if (li < LOSER_CLAIMS) {
                pickOrder[idx++] = loser;
                li++;
            } else {
                pickOrder[idx++] = winner;
                wi++;
            }
            // Prevent infinite loop guard
            if (idx >= total)
                break;
        }
    }

    private void rebuildGrid(GameModel model) {
        gridPanel.removeAll();
        MapGrid map = model.getMap();
        if (map == null)
            return;

        int size = map.getSize();
        gridPanel.setLayout(new GridLayout(size, size, 6, 6));
        cellButtons = new JButton[size][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                char owner = map.getOwner(r, c);
                JButton btn = buildCellButton(owner, r, c, model);
                cellButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton buildCellButton(char owner, int row, int col, GameModel model) {
        JButton btn = new JButton();
        btn.setFont(MindWarsTheme.BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(MindWarsTheme.DARK_BORDER, 2));
        btn.setPreferredSize(new Dimension(72, 72));

        applyOwnerStyle(btn, owner);

        if (owner == '.') {
            // Empty cell → clickable
            final int fr = row, fc = col;
            btn.addActionListener(e -> onCellClicked(fr, fc));
        } else {
            btn.setEnabled(false);
        }

        return btn;
    }

    private void applyOwnerStyle(JButton btn, char owner) {
        switch (owner) {
            case 'X' -> {
                btn.setBackground(MindWarsTheme.PLAYER_X);
                btn.setForeground(MindWarsTheme.WHITE);
                btn.setText("X");
                btn.setEnabled(false);
            }
            case 'O' -> {
                btn.setBackground(MindWarsTheme.PLAYER_O);
                btn.setForeground(MindWarsTheme.WHITE);
                btn.setText("O");
                btn.setEnabled(false);
            }
            default -> {
                btn.setBackground(MindWarsTheme.DARK_CARD);
                btn.setForeground(MindWarsTheme.GRAY_LIGHT);
                btn.setText("");
            }
        }
    }

    private void onCellClicked(int row, int col) {
        if (pickIndex >= pickOrder.length)
            return; // all picks done

        int playerIndex = pickOrder[pickIndex];
        boolean accepted = controller.onCellClaimed(playerIndex, row, col);

        if (!accepted)
            return; // cell already taken (race condition guard)

        pickIndex++;

        // Animate and update the clicked button immediately
        GameModel model = controller.getModel();
        char newOwner = model.getMap().getOwner(row, col);
        JButton btn = cellButtons[row][col];
        applyOwnerStyle(btn, newOwner);
        btn.setEnabled(false);

        // Flash animation for the claimed cell
        Color flash = (playerIndex == 0) ? MindWarsTheme.PLAYER_X : MindWarsTheme.PLAYER_O;
        Color target = flash;
        AnimationHelper.flashBackground(btn, flash.brighter(), target, 6, 60);

        // Advance UI state
        List<Player> players = model.getPlayers();
        if (pickIndex >= pickOrder.length || model.getMap().isMapFull()) {
            // All picks exhausted → enable Finish Round
            disableAllEmptyCells();
            instructionLabel.setText("All territories claimed! Press Finish Round.");
            instructionLabel.setForeground(MindWarsTheme.PINK);
            finishButton.setEnabled(true);
        } else {
            updateInstruction(players);
        }
    }

    /** Greys out remaining empty cells once picks are exhausted. */
    private void disableAllEmptyCells() {
        if (cellButtons == null)
            return;
        for (JButton[] row : cellButtons) {
            for (JButton btn : row) {
                if (btn != null && btn.isEnabled()) {
                    btn.setEnabled(false);
                }
            }
        }
    }

    private void updateInstruction(List<Player> players) {
        if (pickIndex >= pickOrder.length)
            return;
        int currentPlayerIndex = pickOrder[pickIndex];
        Player current = players.get(currentPlayerIndex);
        int picksLeft = countRemainingPicksFor(currentPlayerIndex);

        // Highlight whose turn it is
        Color playerColor = (currentPlayerIndex == 0) ? MindWarsTheme.PLAYER_X : MindWarsTheme.PLAYER_O;
        instructionLabel.setForeground(playerColor);
        instructionLabel.setText(
                current.getName() + " — choose " + picksLeft
                        + (picksLeft == 1 ? " territory" : " territories"));
    }

    /** Counts how many picks remain for the given player index in pickOrder. */
    private int countRemainingPicksFor(int playerIndex) {
        int count = 0;
        for (int i = pickIndex; i < pickOrder.length; i++) {
            if (pickOrder[i] == playerIndex)
                count++;
        }
        return count;
    }
}