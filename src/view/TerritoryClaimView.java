package view;

import controller.GameController;
import game.MapGrid;
import model.GameModel;
import player.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class TerritoryClaimView extends JPanel {

    private static final int WINNER_CLAIMS = 2;
    private static final int LOSER_CLAIMS = 1;

    private int[] pickOrder;
    private int pickIndex;

    private final GameController controller;

    private final JLabel instructionLabel;
    private JLabel[] scoreLabels;
    private final JPanel scoreRow;
    private final JPanel gridPanel;
    private final JButton finishButton;

    private JButton[][] cellButtons;

    // ── Colors ──────────────────────────────────────────────────────────
    private static final Color[] PLAYER_COLORS = {
            new Color(233, 30, 140), // X – hot pink
            new Color(200, 113, 55), // O – orange
            new Color(0, 200, 100), // A – green
            new Color(255, 180, 0) // B – yellow
    };
    private static final Color EMPTY_BG = new Color(42, 42, 52);
    private static final Color EMPTY_HOVER = new Color(60, 60, 72);
    private static final Color CELL_BORDER = new Color(55, 55, 65);

    // ── Constructor ──────────────────────────────────────────────────────
    public TerritoryClaimView(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 12));
        setBackground(MindWarsTheme.DARK_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(MindWarsTheme.centeredLabel("Territory Claim",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.WHITE), BorderLayout.NORTH);
        scoreRow = new JPanel();
        scoreRow.setOpaque(false);
        top.add(scoreRow, BorderLayout.CENTER);
        instructionLabel = MindWarsTheme.centeredLabel("",
                MindWarsTheme.BODY_FONT, MindWarsTheme.GRAY_LIGHT);
        top.add(instructionLabel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setOpaque(true);
        gridPanel.setBackground(MindWarsTheme.DARK_BG);
        gridPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(gridPanel, BorderLayout.CENTER);

        finishButton = createFinishButton();
        finishButton.addActionListener(e -> controller.onTerritoryPhaseFinished());
        finishButton.setEnabled(false);
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.add(finishButton);
        add(south, BorderLayout.SOUTH);
    }

    // ── Public refresh ───────────────────────────────────────────────────
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
            JLabel lbl = new JLabel(p.getName() + ": " + p.getScore() + " pts",
                    SwingConstants.CENTER);
            lbl.setFont(MindWarsTheme.BODY_BOLD);
            lbl.setForeground(playerColor(i));
            scoreLabels[i] = lbl;
            scoreRow.add(lbl);
        }

        buildPickOrder(model);
        pickIndex = 0;
        rebuildGrid(model, shouldEnableClaiming(model));
        updateInstruction(players);
        finishButton.setEnabled(false);
        revalidate();
        repaint();
        triggerBotPickIfNeeded();
    }

    // ── Pick-order ───────────────────────────────────────────────────────
    private void buildPickOrder(GameModel model) {
        int winnerIdx = model.determineRoundWinnerIndex();
        int n = model.getPlayers().size();
        int total = WINNER_CLAIMS + LOSER_CLAIMS * (n - 1);
        pickOrder = new int[total];
        int idx = 0;
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++)
            remaining[i] = (i == winnerIdx) ? WINNER_CLAIMS : LOSER_CLAIMS;
        if (model.getSettings() != null && model.getSettings().vsBot) {
            int humanIndex = -1;
            int botIndex = -1;
            for (int i = 0; i < n; i++) {
                if (model.getPlayers().get(i).isBot()) {
                    botIndex = i;
                } else {
                    humanIndex = i;
                }
            }
            if (humanIndex >= 0 && botIndex >= 0) {
                while (remaining[humanIndex]-- > 0) {
                    pickOrder[idx++] = humanIndex;
                }
                while (remaining[botIndex]-- > 0) {
                    pickOrder[idx++] = botIndex;
                }
                return;
            }
        }
        while (remaining[winnerIdx]-- > 0) {
            pickOrder[idx++] = winnerIdx;
        }
        for (int i = 0; i < n; i++) {
            if (i == winnerIdx)
                continue;
            while (remaining[i]-- > 0) {
                pickOrder[idx++] = i;
            }
        }
    }

    // ── Grid ─────────────────────────────────────────────────────────────
    private void rebuildGrid(GameModel model, boolean enableEmpty) {
        gridPanel.removeAll();
        MapGrid map = model.getMap();
        if (map == null)
            return;
        int size = map.getSize();
        gridPanel.setLayout(new GridLayout(size, size, 6, 6));
        cellButtons = new JButton[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                char owner = map.getOwner(r, c);
                JButton btn = buildCellButton(owner, r, c, model, enableEmpty);
                cellButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    /**
     * Returns a JButton whose paintComponent draws a solid fill so the color
     * is always correct regardless of OS Look-and-Feel.
     */
    private JButton buildCellButton(char owner, int row, int col, GameModel model, boolean enableEmpty) {
        final boolean isEmpty = (owner == '.');

        final Color fill;
        final String txt;
        if (isEmpty) {
            fill = EMPTY_BG;
            txt = "";
        } else {
            Color c = playerColor(playerIndexForSymbol(owner, model));
            fill = new Color(c.getRed(), c.getGreen(), c.getBlue());
            txt = String.valueOf(owner);
        }

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Decide background: hover only for empty enabled cells
                boolean hovered = Boolean.TRUE.equals(
                        getClientProperty("hovered"));
                g2.setColor((isEmpty && hovered && isEnabled()) ? EMPTY_HOVER : fill);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw label
                if (!txt.isEmpty()) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(txt)) / 2;
                    int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(txt, tx, ty);
                }
                g2.dispose();
            }
        };

        btn.setFont(new Font("SansSerif", Font.BOLD, 22));
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(CELL_BORDER, 2));
        btn.setPreferredSize(new Dimension(110, 110));

        if (isEmpty && enableEmpty) {
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.putClientProperty("hovered", true);
                    btn.repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.putClientProperty("hovered", false);
                    btn.repaint();
                }
            });
            final int fr = row, fc = col;
            btn.addActionListener(e -> onCellClicked(fr, fc));
        } else {
            btn.setEnabled(false);
            btn.setCursor(Cursor.getDefaultCursor());
        }

        return btn;
    }

    private void onCellClicked(int row, int col) {
        if (pickIndex >= pickOrder.length)
            return;

        int playerIndex = pickOrder[pickIndex];
        boolean accepted = controller.onCellClaimed(playerIndex, row, col);
        if (!accepted)
            return;

        pickIndex++;

        // Rebuild so the clicked cell gets a fresh colored button
        GameModel model = controller.getModel();
        rebuildGrid(model, shouldEnableClaiming(model));

        // Flash the newly-colored cell
        Color flash = playerColor(playerIndex);
        JButton btn = cellButtons[row][col];
        AnimationHelper.flashBackground(btn, flash.brighter(), flash, 6, 60);

        List<Player> players = model.getPlayers();
        if (pickIndex >= pickOrder.length || model.getMap().isMapFull()) {
            disableAllEmptyCells();
            instructionLabel.setText("All territories claimed! Press Finish Round.");
            instructionLabel.setForeground(MindWarsTheme.PINK);
            finishButton.setEnabled(true);
        } else {
            if (!shouldEnableClaiming(model)) {
                disableAllEmptyCells();
            }
            updateInstruction(players);
            triggerBotPickIfNeeded();
        }
    }

    private boolean shouldEnableClaiming(GameModel model) {
        if (pickOrder == null || pickIndex >= pickOrder.length) {
            return false;
        }
        int current = pickOrder[pickIndex];
        Player p = model.getPlayers().get(current);
        return !p.isBot();
    }

    // ── Bot ──────────────────────────────────────────────────────────────
    private void triggerBotPickIfNeeded() {
        if (pickOrder == null || pickIndex >= pickOrder.length)
            return;
        GameModel model = controller.getModel();
        Player cur = model.getPlayers().get(pickOrder[pickIndex]);
        if (!cur.isBot())
            return;
        javax.swing.Timer t = new javax.swing.Timer(700, e -> {
            if (pickIndex >= pickOrder.length)
                return;
            int[] target = pickRandomEmptyCell(model.getMap());
            if (target == null)
                return;
            onCellClicked(target[0], target[1]);
        });
        t.setRepeats(false);
        t.start();
    }

    private int[] pickRandomEmptyCell(MapGrid map) {
        int size = map.getSize();
        java.util.List<int[]> empties = new java.util.ArrayList<>();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (map.getOwner(r, c) == '.')
                    empties.add(new int[] { r, c });
        if (empties.isEmpty())
            return null;
        return empties.get(new java.util.Random().nextInt(empties.size()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private void disableAllEmptyCells() {
        if (cellButtons == null)
            return;
        for (JButton[] row : cellButtons)
            for (JButton btn : row)
                if (btn != null && btn.isEnabled()) {
                    btn.setEnabled(false);
                    btn.setCursor(Cursor.getDefaultCursor());
                    btn.putClientProperty("hovered", false);
                    btn.repaint();
                }
    }

    private void updateInstruction(List<Player> players) {
        if (pickIndex >= pickOrder.length)
            return;
        int cpi = pickOrder[pickIndex];
        Player current = players.get(cpi);
        int left = countRemainingPicksFor(cpi);
        instructionLabel.setForeground(playerColor(cpi));
        instructionLabel.setText(current.getName() + " — choose " + left
                + (left == 1 ? " territory" : " territories"));
    }

    private JButton createFinishButton() {
        JButton btn = new JButton("Finish Round") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int arc = 12;

                if (isEnabled()) {
                    GradientPaint gp = new GradientPaint(0, 0, MindWarsTheme.PINK, w, 0, MindWarsTheme.ORANGE);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(MindWarsTheme.WHITE);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(240, 240, 240),
                            w, 0, new Color(190, 190, 190));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(Color.BLACK);
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        btn.setFont(MindWarsTheme.BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(360, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setCursor(Cursor.getDefaultCursor());
        btn.addPropertyChangeListener("enabled", evt -> {
            boolean enabled = Boolean.TRUE.equals(evt.getNewValue());
            btn.setCursor(enabled
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
        });
        return btn;
    }

    private int countRemainingPicksFor(int playerIndex) {
        int count = 0;
        for (int i = pickIndex; i < pickOrder.length; i++)
            if (pickOrder[i] == playerIndex)
                count++;
        return count;
    }

    private Color playerColor(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < PLAYER_COLORS.length)
            return PLAYER_COLORS[playerIndex];
        return MindWarsTheme.GRAY_LIGHT;
    }

    private int playerIndexForSymbol(char symbol, GameModel model) {
        List<Player> players = model.getPlayers();
        for (int i = 0; i < players.size(); i++)
            if (players.get(i).getSymbol() == symbol)
                return i;
        return 0;
    }
}
