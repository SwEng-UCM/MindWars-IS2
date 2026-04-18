package view;

import controller.GameController;
import game.MapGrid;
import model.GameModel;
import player.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TerritoryClaimView extends JPanel {

    private static final int WINNER_CLAIMS = 2;
    private static final int LOSER_CLAIMS = 1;

    private int[] pickOrder;
    private int pickIndex;

    private final GameController controller;

    // ── UI ──
    private final JLabel instructionLabel;
    private JLabel[] scoreLabels;
    private final JPanel scoreRow;
    private final JPanel gridPanel;
    private final JButton finishButton;

    private JButton[][] cellButtons;

    private static final char[] SYMBOLS = { 'X', 'O', 'A', 'B' };
    private static final Color[] PLAYER_COLORS = {
            MindWarsTheme.PLAYER_X,
            MindWarsTheme.PLAYER_O,
            new Color(0, 200, 100),
            new Color(255, 180, 0)
    };

    public TerritoryClaimView(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 12));
        setBackground(MindWarsTheme.DARK_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Top ──
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);

        JLabel title = MindWarsTheme.centeredLabel(
                "Territory Claim",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE);
        top.add(title, BorderLayout.NORTH);

        scoreRow = new JPanel();
        scoreRow.setOpaque(false);
        top.add(scoreRow, BorderLayout.CENTER);

        instructionLabel = MindWarsTheme.centeredLabel(
                "", MindWarsTheme.BODY_FONT, MindWarsTheme.GRAY_LIGHT);
        top.add(instructionLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        add(gridPanel, BorderLayout.CENTER);

        finishButton = MindWarsTheme.createGradientButton("Finish Round");
        finishButton.addActionListener(e -> controller.onTerritoryPhaseFinished());
        finishButton.setEnabled(false);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(finishButton);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        GameModel model = controller.getModel();
        List<Player> players = model.getPlayers();
        if (players == null || players.isEmpty())
            return;

        scoreRow.removeAll();
        scoreRow.setLayout(new GridLayout(1, players.size(), 16, 0));
        scoreLabels = new JLabel[players.size()];
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            JLabel lbl = new JLabel(
                    p.getName() + ": " + p.getScore() + " pts",
                    SwingConstants.CENTER);
            lbl.setFont(MindWarsTheme.BODY_BOLD);
            lbl.setForeground(playerColor(i));
            scoreLabels[i] = lbl;
            scoreRow.add(lbl);
        }

        buildPickOrder(model);
        pickIndex = 0;

        rebuildGrid(model);

        updateInstruction(players);
        finishButton.setEnabled(false);

        revalidate();
        repaint();
    }

    private void buildPickOrder(GameModel model) {
        int winnerIdx = model.determineRoundWinnerIndex();
        int n = model.getPlayers().size();

        int total = WINNER_CLAIMS + LOSER_CLAIMS * (n - 1);
        pickOrder = new int[total];

        int idx = 0;
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) {
            remaining[i] = (i == winnerIdx) ? WINNER_CLAIMS : LOSER_CLAIMS;
        }

        int current = winnerIdx;
        while (idx < total) {
            if (remaining[current] > 0) {
                pickOrder[idx++] = current;
                remaining[current]--;
            }
            int next = (current + 1) % n;
            int checked = 0;
            while (remaining[next] == 0 && checked < n) {
                next = (next + 1) % n;
                checked++;
            }
            current = next;
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

        applyOwnerStyle(btn, owner, model);

        if (owner == '.') {
            final int fr = row, fc = col;
            btn.addActionListener(e -> onCellClicked(fr, fc));
        } else {
            btn.setEnabled(false);
        }

        return btn;
    }

    private void applyOwnerStyle(JButton btn, char owner, GameModel model) {
        if (owner == '.') {
            btn.setBackground(MindWarsTheme.DARK_CARD);
            btn.setForeground(MindWarsTheme.GRAY_LIGHT);
            btn.setText("");
            btn.setEnabled(true);
            return;
        }

        int playerIdx = playerIndexForSymbol(owner, model);
        Color color = playerColor(playerIdx);

        btn.setBackground(color);
        btn.setForeground(MindWarsTheme.WHITE);
        btn.setText(String.valueOf(owner));
        btn.setEnabled(false);
    }

    private void onCellClicked(int row, int col) {
        if (pickIndex >= pickOrder.length)
            return;

        int playerIndex = pickOrder[pickIndex];
        boolean accepted = controller.onCellClaimed(playerIndex, row, col);
        if (!accepted)
            return;

        pickIndex++;

        GameModel model = controller.getModel();
        char newOwner = model.getMap().getOwner(row, col);
        JButton btn = cellButtons[row][col];
        applyOwnerStyle(btn, newOwner, model);
        btn.setEnabled(false);

        Color flash = playerColor(playerIndex);
        AnimationHelper.flashBackground(btn, flash.brighter(), flash, 6, 60);

        List<Player> players = model.getPlayers();
        if (pickIndex >= pickOrder.length || model.getMap().isMapFull()) {
            disableAllEmptyCells();
            instructionLabel.setText("All territories claimed! Press Finish Round.");
            instructionLabel.setForeground(MindWarsTheme.PINK);
            finishButton.setEnabled(true);
        } else {
            updateInstruction(players);
        }
    }

    private void disableAllEmptyCells() {
        if (cellButtons == null)
            return;
        for (JButton[] row : cellButtons)
            for (JButton btn : row)
                if (btn != null && btn.isEnabled())
                    btn.setEnabled(false);
    }

    private void updateInstruction(List<Player> players) {
        if (pickIndex >= pickOrder.length)
            return;
        int currentPlayerIndex = pickOrder[pickIndex];
        Player current = players.get(currentPlayerIndex);
        int picksLeft = countRemainingPicksFor(currentPlayerIndex);

        instructionLabel.setForeground(playerColor(currentPlayerIndex));
        instructionLabel.setText(
                current.getName() + " — choose " + picksLeft
                        + (picksLeft == 1 ? " territory" : " territories"));
    }

    private int countRemainingPicksFor(int playerIndex) {
        int count = 0;
        for (int i = pickIndex; i < pickOrder.length; i++)
            if (pickOrder[i] == playerIndex)
                count++;
        return count;
    }

    // ── Helpers ──

    private Color playerColor(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < PLAYER_COLORS.length)
            return PLAYER_COLORS[playerIndex];
        return MindWarsTheme.GRAY_LIGHT;
    }

    private int playerIndexForSymbol(char symbol, GameModel model) {
        List<Player> players = model.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getSymbol() == symbol)
                return i;
        }
        return 0;
    }
}