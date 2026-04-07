package ui;

import game.*;
import player.Player;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * The primary in-game screen shown while a match is in progress.
 *
 * <p>Layout (left → right):
 * <ul>
 *   <li><b>Left sidebar</b>  – player score cards and streak indicators.</li>
 *   <li><b>Centre panel</b>  – live map grid.</li>
 *   <li><b>Right sidebar</b> – current round info, coin balances and action buttons
 *       (including the <em>Save Game</em> button).</li>
 * </ul>
 *
 * <p>The <b>Save Game</b> button collects the complete runtime state through
 * {@link SavedGameData#from(GameState, MapGrid)} and delegates persistence to
 * {@link SaveGameManager#saveWithDialog(SavedGameData, Component)}.
 */
public class GamePanel extends JPanel {

    // -----------------------------------------------------------------------
    // Colour palette (matches GradientButton / SummaryPanel style)
    // -----------------------------------------------------------------------
    private static final Color BG_DARK        = new Color(18, 14, 28);
    private static final Color BG_CARD        = new Color(28, 22, 42);
    private static final Color BG_CARD_LIGHT  = new Color(38, 30, 58);
    private static final Color ACCENT_PINK    = new Color(255, 55, 160);
    private static final Color ACCENT_ORANGE  = new Color(214, 104, 0);
    private static final Color PLAYER1_COLOR  = new Color(255, 45, 170);
    private static final Color PLAYER2_COLOR  = new Color(80, 210, 255);
    private static final Color NEUTRAL_CELL   = new Color(48, 42, 68);
    private static final Color TEXT_PRIMARY   = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(180, 170, 210);
    private static final Color GOLD           = new Color(255, 215, 0);

    // -----------------------------------------------------------------------
    // Component IDs (useful for automated testing and screen-readers)
    // -----------------------------------------------------------------------
    public static final String ID_SAVE_BUTTON         = "btn-save-game";
    public static final String ID_ROUND_LABEL         = "lbl-round";
    public static final String ID_P1_SCORE_LABEL      = "lbl-p1-score";
    public static final String ID_P2_SCORE_LABEL      = "lbl-p2-score";
    public static final String ID_P1_COINS_LABEL      = "lbl-p1-coins";
    public static final String ID_P2_COINS_LABEL      = "lbl-p2-coins";
    public static final String ID_P1_STREAK_LABEL     = "lbl-p1-streak";
    public static final String ID_P2_STREAK_LABEL     = "lbl-p2-streak";
    public static final String ID_MAP_PANEL            = "pnl-map-grid";
    public static final String ID_ACTIVE_PLAYER_LABEL = "lbl-active-player";

    // -----------------------------------------------------------------------
    // Live data
    // -----------------------------------------------------------------------
    private final GameState gameState;
    private final MapGrid   map;

    // -----------------------------------------------------------------------
    // Dynamic labels (updated by refreshUI())
    // -----------------------------------------------------------------------
    private JLabel roundLabel;
    private JLabel activePlayerLabel;

    private JLabel[] scoreLabels;
    private JLabel[] streakLabels;
    private JLabel[] coinLabels;

    private MapGridPanel mapGridPanel;

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Creates a GamePanel bound to the given runtime objects.
     *
     * @param gameState the live {@link GameState} (players + round number)
     * @param map       the live {@link MapGrid}
     */
    public GamePanel(GameState gameState, MapGrid map) {
        this.gameState = gameState;
        this.map       = map;

        setOpaque(true);
        setLayout(new BorderLayout(16, 8));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildLeftSidebar(),  BorderLayout.WEST);
        add(buildCentreMap(),    BorderLayout.CENTER);
        add(buildRightSidebar(), BorderLayout.EAST);

        refreshUI();
    }

    // =======================================================================
    // Section builders
    // =======================================================================

    // -----------------------------------------------------------------------
    // Header – round number and active player indicator
    // -----------------------------------------------------------------------
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Title
        JLabel title = new JLabel("⚔  MINDWARS", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);

        // Round badge
        roundLabel = new JLabel("Round 1", SwingConstants.CENTER);
        roundLabel.setName(ID_ROUND_LABEL);
        roundLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        roundLabel.setForeground(GOLD);
        roundLabel.setOpaque(true);
        roundLabel.setBackground(BG_CARD);
        roundLabel.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

        // Active player
        activePlayerLabel = new JLabel("", SwingConstants.RIGHT);
        activePlayerLabel.setName(ID_ACTIVE_PLAYER_LABEL);
        activePlayerLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
        activePlayerLabel.setForeground(TEXT_SECONDARY);

        header.add(title,             BorderLayout.WEST);
        header.add(roundLabel,        BorderLayout.CENTER);
        header.add(activePlayerLabel, BorderLayout.EAST);

        return header;
    }

    // -----------------------------------------------------------------------
    // Left sidebar – player score cards
    // -----------------------------------------------------------------------
    private JPanel buildLeftSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 14));

        List<Player> players = gameState.getPlayers();
        int n = players.size();
        scoreLabels  = new JLabel[n];
        streakLabels = new JLabel[n];
        coinLabels   = new JLabel[n];

        for (int i = 0; i < n; i++) {
            Player p = players.get(i);
            Color  c = playerColor(p);

            JPanel card = buildCard();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(14, 16, 14, 16));

            // Player name header
            JLabel nameLabel = new JLabel(p.getName() + (p.isBot() ? " 🤖" : ""));
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            nameLabel.setForeground(c);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Symbol badge
            JLabel symbolLabel = new JLabel("Symbol: " + p.getSymbol());
            symbolLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            symbolLabel.setForeground(TEXT_SECONDARY);
            symbolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Score
            scoreLabels[i] = new JLabel("Score: " + p.getScore() + " pts");
            scoreLabels[i].setName(i == 0 ? ID_P1_SCORE_LABEL : ID_P2_SCORE_LABEL);
            scoreLabels[i].setFont(new Font("SansSerif", Font.BOLD, 20));
            scoreLabels[i].setForeground(TEXT_PRIMARY);
            scoreLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            // Streak
            streakLabels[i] = new JLabel(streakText(p.getStreak()));
            streakLabels[i].setName(i == 0 ? ID_P1_STREAK_LABEL : ID_P2_STREAK_LABEL);
            streakLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            streakLabels[i].setForeground(new Color(255, 190, 50));
            streakLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            // Coins
            coinLabels[i] = new JLabel("💰 " + p.getCoins() + " coins");
            coinLabels[i].setName(i == 0 ? ID_P1_COINS_LABEL : ID_P2_COINS_LABEL);
            coinLabels[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            coinLabels[i].setForeground(GOLD);
            coinLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);

            // Bonus tokens
            JLabel tokenLabel = new JLabel("🎫 " + p.getBonusTokens() + " lifeline(s)");
            tokenLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            tokenLabel.setForeground(new Color(130, 220, 160));
            tokenLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Divider
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(60, 50, 90));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

            card.add(nameLabel);
            card.add(symbolLabel);
            card.add(Box.createVerticalStrut(8));
            card.add(sep);
            card.add(Box.createVerticalStrut(8));
            card.add(scoreLabels[i]);
            card.add(streakLabels[i]);
            card.add(coinLabels[i]);
            card.add(tokenLabel);

            sidebar.add(card);
            sidebar.add(Box.createVerticalStrut(14));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    // -----------------------------------------------------------------------
    // Centre – map grid
    // -----------------------------------------------------------------------
    private JPanel buildCentreMap() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JLabel mapTitle = new JLabel("🗺  BATTLE MAP", SwingConstants.CENTER);
        mapTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        mapTitle.setForeground(TEXT_SECONDARY);
        mapTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        mapGridPanel = new MapGridPanel(map, gameState.getPlayers());
        mapGridPanel.setName(ID_MAP_PANEL);

        wrapper.add(mapTitle,    BorderLayout.NORTH);
        wrapper.add(mapGridPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // -----------------------------------------------------------------------
    // Right sidebar – round info + action buttons
    // -----------------------------------------------------------------------
    private JPanel buildRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(0, 14, 0, 0));

        // ── Info card ────────────────────────────────────────────────────────────
        JPanel infoCard = buildCard();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel infoTitle = sectionLabel("ℹ  Game Info");
        JLabel mapSizeLabel = plainLabel("Map: " + map.getSize() + "×" + map.getSize());
        JLabel totalRoundsLabel = plainLabel("Rounds: " + map.getSize()); // N questions for NxN map

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(6));
        infoCard.add(mapSizeLabel);
        infoCard.add(totalRoundsLabel);

        sidebar.add(infoCard);
        sidebar.add(Box.createVerticalStrut(14));

        // ── Save Game button ──────────────────────────────────────────────────────
        JButton saveBtn = buildGradientButton("💾  Save Game", ACCENT_PINK, ACCENT_ORANGE);
        saveBtn.setName(ID_SAVE_BUTTON);
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> handleSaveGame());

        sidebar.add(saveBtn);
        sidebar.add(Box.createVerticalStrut(10));

        // ── Refresh button (dev / demo helper) ───────────────────────────────────
        JButton refreshBtn = buildOutlineButton("↻  Refresh View");
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> refreshUI());

        sidebar.add(refreshBtn);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    // =======================================================================
    // Save Game logic
    // =======================================================================

    /**
     * Captures the full game state and delegates to {@link SaveGameManager}
     * which opens a file-chooser dialog before writing the JSON.
     */
    public void handleSaveGame() {
        SavedGameData snapshot = SavedGameData.from(gameState, map);
        boolean ok = SaveGameManager.saveWithDialog(snapshot, this);

        if (ok) {
            showSaveFeedback(true, "Game saved!\n" + snapshot.savedAt);
        }
        // On cancel, no-op.  Errors are shown by SaveGameManager itself.
    }

    // -----------------------------------------------------------------------
    // Visual feedback overlay after a save
    // -----------------------------------------------------------------------
    private void showSaveFeedback(boolean success, String message) {
        // Brief non-blocking toast notification
        JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(this));
        JLabel label  = new JLabel((success ? "✅ " : "❌ ") + message, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(10, 24, 10, 24));
        label.setOpaque(true);
        label.setBackground(success ? new Color(30, 70, 50) : new Color(80, 20, 20));

        toast.getContentPane().add(label);
        toast.pack();

        // Centre above this panel
        Point loc = getLocationOnScreen();
        int tx = loc.x + (getWidth() - toast.getWidth()) / 2;
        int ty = loc.y + 60;
        toast.setLocation(tx, ty);
        toast.setVisible(true);

        Timer timer = new Timer(2500, ev -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    // =======================================================================
    // Public refresh
    // =======================================================================

    /**
     * Re-reads live data from {@link GameState} / {@link Player} objects and
     * updates every dynamic label + the map grid.
     * Call this after any game-state mutation (score change, territory claim, etc.)
     */
    public void refreshUI() {
        List<Player> players = gameState.getPlayers();

        roundLabel.setText("Round " + gameState.getRoundNumber());

        if (!players.isEmpty()) {
            int idx = gameState.getCurrentPlayerIndex();
            if (idx >= 0 && idx < players.size()) {
                activePlayerLabel.setText("🎮  " + players.get(idx).getName() + "'s turn");
            }
        }

        for (int i = 0; i < players.size() && i < scoreLabels.length; i++) {
            Player p = players.get(i);
            scoreLabels[i].setText("Score: " + p.getScore() + " pts");
            streakLabels[i].setText(streakText(p.getStreak()));
            coinLabels[i].setText("💰 " + p.getCoins() + " coins");
        }

        if (mapGridPanel != null) {
            mapGridPanel.repaint();
        }
    }

    // =======================================================================
    // Inner component: Map grid renderer
    // =======================================================================

    /**
     * Custom component that renders the {@link MapGrid} as a coloured grid.
     */
    private static class MapGridPanel extends JPanel {

        private final MapGrid       map;
        private final List<Player>  players;

        MapGridPanel(MapGrid map, List<Player> players) {
            this.map     = map;
            this.players = players;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size  = map.getSize();
            int w     = getWidth();
            int h     = getHeight();
            int pad   = 8;
            int cellW = (w - pad * 2) / size;
            int cellH = (h - pad * 2) / size;
            int arc   = 10;

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    char owner = map.getCell(r, c);
                    Color bg   = cellColor(owner);
                    Color text = TEXT_PRIMARY;

                    int x = pad + c * cellW;
                    int y = pad + r * cellH;

                    // Cell background
                    g2.setColor(bg);
                    g2.fillRoundRect(x + 2, y + 2, cellW - 4, cellH - 4, arc, arc);

                    // Subtle inner border
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.drawRoundRect(x + 2, y + 2, cellW - 4, cellH - 4, arc, arc);

                    // Bonus cell indicator
                    if (map.hasBonus(r, c) && owner == '.') {
                        g2.setColor(GOLD);
                        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(8, cellH / 3)));
                        FontMetrics fm = g2.getFontMetrics();
                        String star = "★";
                        int tx = x + (cellW - fm.stringWidth(star)) / 2;
                        int ty = y + (cellH + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(star, tx, ty);
                    } else if (owner != '.') {
                        // Owner symbol
                        g2.setColor(text);
                        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(8, cellH / 3)));
                        FontMetrics fm = g2.getFontMetrics();
                        String sym = String.valueOf(owner);
                        int tx = x + (cellW - fm.stringWidth(sym)) / 2;
                        int ty = y + (cellH + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(sym, tx, ty);
                    }
                }
            }

            g2.dispose();
        }

        private Color cellColor(char owner) {
            for (Player p : players) {
                if (p.getSymbol() == owner) {
                    return p.getSymbol() == 'X' ? PLAYER1_COLOR.darker() : PLAYER2_COLOR.darker();
                }
            }
            return NEUTRAL_CELL;
        }
    }

    // =======================================================================
    // UI helpers
    // =======================================================================

    @Override
    protected void paintComponent(Graphics g) {
        // Full dark background
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(BG_DARK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JButton buildGradientButton(String text, Color from, Color to) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, from, getWidth(), 0, to);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return btn;
    }

    private JButton buildOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD_LIGHT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
                g2.setColor(new Color(255, 255, 255, 40));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 18, 18));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setForeground(TEXT_SECONDARY);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return btn;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel plainLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static String streakText(int streak) {
        if (streak == 0) return "No streak";
        return "🔥 Streak ×" + streak;
    }

    private static Color playerColor(Player p) {
        return p.getSymbol() == 'X' ? PLAYER1_COLOR : PLAYER2_COLOR;
    }
}
