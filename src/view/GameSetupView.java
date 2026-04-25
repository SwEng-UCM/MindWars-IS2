package view;

import controller.GameController;
import model.GameSettings;
import trivia.QuestionBank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Scrollable card: tells JScrollPane not to compress the panel vertically.
class ScrollableCard extends JPanel implements Scrollable {
    ScrollableCard() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}

public class GameSetupView extends JPanel {

    private final GameController controller;

    // Form state
    private int mapSize = 4;
    private boolean randomMode = true;

    // Widgets
    private final JTextField player1Field;
    private final JTextField player2Field;
    private final JTextField player3Field;
    private final JTextField player4Field;
    private int numPlayers = 1;
    private final JComboBox<String> numPlayersCombo;
    private final JPanel playerFieldsPanel;
    private final List<JToggleButton> sizeButtons = new ArrayList<>();
    private JScrollPane scroller;
    private final JCheckBox randomCheck;
    private final JComboBox<String> categoryCombo;
    private final JComboBox<String> difficultyCombo;

    private final JPanel botDifficultyPanel;
    private final List<JToggleButton> botDiffButtons = new ArrayList<>();
    private String selectedBotDifficulty = "Easy";

    public GameSetupView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        ScrollableCard card = new ScrollableCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = MindWarsTheme.centeredLabel("New Game",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(18));

        // ── Number of players dropdown ──
        card.add(sectionLabel("Players"));
        card.add(Box.createVerticalStrut(6));

        numPlayersCombo = new JComboBox<>(new String[] {
                "1 Player (vs Bot)", "2 Players", "3 Players", "4 Players" });
        numPlayersCombo.setSelectedIndex(0);
        numPlayersCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        numPlayersCombo.setFont(MindWarsTheme.BODY_FONT);
        numPlayersCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(numPlayersCombo);
        card.add(Box.createVerticalStrut(8));

        // ── Player name fields (rebuilt on selection change) ──
        player1Field = MindWarsTheme.createTextField("Player 1 name");
        player1Field.setText("Player 1");
        player1Field.setAlignmentX(Component.LEFT_ALIGNMENT);
        player2Field = MindWarsTheme.createTextField("Player 2 name");
        player2Field.setText("Player 2");
        player2Field.setAlignmentX(Component.LEFT_ALIGNMENT);
        player3Field = MindWarsTheme.createTextField("Player 3 name");
        player3Field.setText("Player 3");
        player3Field.setAlignmentX(Component.LEFT_ALIGNMENT);
        player4Field = MindWarsTheme.createTextField("Player 4 name");
        player4Field.setText("Player 4");
        player4Field.setAlignmentX(Component.LEFT_ALIGNMENT);

        playerFieldsPanel = new JPanel();
        playerFieldsPanel.setLayout(new BoxLayout(playerFieldsPanel, BoxLayout.Y_AXIS));
        playerFieldsPanel.setOpaque(false);
        playerFieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(playerFieldsPanel);
        card.add(Box.createVerticalStrut(16));

        // bot difficulty (visible for 1 player vs bot)
        botDifficultyPanel = new JPanel();
        botDifficultyPanel.setLayout(new BoxLayout(botDifficultyPanel, BoxLayout.Y_AXIS));
        botDifficultyPanel.setOpaque(false);
        botDifficultyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        botDifficultyPanel.add(sectionLabel("Bot Difficulty"));
        botDifficultyPanel.add(Box.createVerticalStrut(6));

        JPanel botBtnRow = new JPanel(new GridLayout(1, 3, 10, 0));
        botBtnRow.setOpaque(false);
        botBtnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        botBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String diff : new String[] { "Easy", "Medium", "Hard" }) {
            boolean sel = diff.equals(selectedBotDifficulty);
            JToggleButton tb = botDiffToggle(diff, sel);
            tb.addActionListener(e -> selectBotDifficulty(diff));
            botDiffButtons.add(tb);
            botBtnRow.add(tb);
        }
        botDifficultyPanel.add(botBtnRow);
        botDifficultyPanel.add(Box.createVerticalStrut(16));

        card.add(botDifficultyPanel);

        // ── Map size ──
        card.add(sectionLabel("Map size"));
        card.add(Box.createVerticalStrut(6));
        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 10, 0));
        sizeRow.setOpaque(false);
        sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sizeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int s : new int[] { 3, 4, 5 }) {
            JToggleButton tb = optionToggle(s + " × " + s, s == mapSize);
            tb.addActionListener(e -> selectSize(s));
            sizeButtons.add(tb);
            sizeRow.add(tb);
        }
        card.add(sizeRow);
        card.add(Box.createVerticalStrut(16));

        // ── Category / difficulty ──
        card.add(sectionLabel("Questions"));
        card.add(Box.createVerticalStrut(6));

        randomCheck = new JCheckBox("Random (mixed categories & difficulties)", true);
        randomCheck.setOpaque(false);
        randomCheck.setFont(MindWarsTheme.BODY_FONT);
        randomCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        randomCheck.addActionListener(e -> onRandomToggled());
        card.add(randomCheck);
        card.add(Box.createVerticalStrut(8));

        categoryCombo = new JComboBox<>();
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        categoryCombo.setFont(MindWarsTheme.BODY_FONT);
        categoryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryCombo.addActionListener(e -> rebuildDifficulties());
        card.add(categoryCombo);
        card.add(Box.createVerticalStrut(8));

        difficultyCombo = new JComboBox<>();
        difficultyCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        difficultyCombo.setFont(MindWarsTheme.BODY_FONT);
        difficultyCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(difficultyCombo);
        card.add(Box.createVerticalStrut(20));

        loadCategoriesFromBank();
        onRandomToggled();

        // ── Buttons ──
        JButton start = MindWarsTheme.createGradientButton("Start Game");
        start.setAlignmentX(Component.LEFT_ALIGNMENT);
        start.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        start.addActionListener(e -> onStart());
        card.add(start);
        card.add(Box.createVerticalStrut(10));

        JButton back = MindWarsTheme.createPinkButton("Back");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        back.addActionListener(e -> controller.returnToMenu());
        card.add(back);

        numPlayersCombo.addActionListener(e -> onNumPlayersChanged());
        onNumPlayersChanged();

        scroller = new JScrollPane(card,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setPreferredSize(new Dimension(420, 600));
        scroller.setBorder(null);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.getViewport().setBackground(new Color(0, 0, 0, 0));
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        bg.add(scroller);

        add(bg, BorderLayout.CENTER);

        // Once the card has been laid out with 1 player, lock the viewport
        // height to the card's natural height so that adding a 2nd player
        // (or more) is what first triggers the scrollbar.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing() && !viewportSized) {
                SwingUtilities.invokeLater(() -> {
                    int h = card.getPreferredSize().height;
                    if (h > 0) {
                        scroller.setPreferredSize(new Dimension(420, h));
                        scroller.revalidate();
                        viewportSized = true;
                    }
                });
            }
        });
    }

    private boolean viewportSized = false;

    public void reset() {
    }

    // ── Helpers ──

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MindWarsTheme.BODY_BOLD);
        l.setForeground(Color.BLACK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JToggleButton optionToggle(String text, boolean selected) {
        JToggleButton tb = new JToggleButton(text, selected);
        tb.setFont(MindWarsTheme.BODY_BOLD);
        tb.setFocusPainted(false);
        tb.setBackground(selected ? MindWarsTheme.PINK_BG : MindWarsTheme.WHITE);
        tb.setForeground(selected ? MindWarsTheme.PINK : Color.DARK_GRAY);
        tb.setBorder(BorderFactory.createLineBorder(
                selected ? MindWarsTheme.PINK : MindWarsTheme.GRAY_LIGHT, 2, true));
        tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return tb;
    }

    private JToggleButton botDiffToggle(String diff, boolean selected) {
        Color accentColor = switch (diff) {
            case "Medium" -> new Color(230, 140, 0);
            case "Hard" -> new Color(210, 40, 40);
            default -> new Color(30, 160, 80); // easy
        };
        Color bgColor = selected
                ? accentColor.brighter()
                : new Color(245, 245, 245);

        JToggleButton tb = new JToggleButton(diff, selected);
        tb.setFont(MindWarsTheme.BODY_BOLD);
        tb.setFocusPainted(false);
        tb.setBackground(bgColor);
        tb.setForeground(selected ? Color.WHITE : accentColor.darker());
        tb.setBorder(BorderFactory.createLineBorder(accentColor, 2, true));
        tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tb.setOpaque(true);

        // redraw state when it changes
        tb.addItemListener(e -> {
            boolean sel = tb.isSelected();
            tb.setBackground(sel ? accentColor.brighter() : new Color(245, 245, 245));
            tb.setForeground(sel ? Color.WHITE : accentColor.darker());
            tb.repaint();
        });
        return tb;
    }

    private void restyleToggle(JToggleButton tb, boolean selected) {
        tb.setSelected(selected);
        tb.setBackground(selected ? MindWarsTheme.PINK_BG : MindWarsTheme.WHITE);
        tb.setForeground(selected ? MindWarsTheme.PINK : Color.DARK_GRAY);
        tb.setBorder(BorderFactory.createLineBorder(
                selected ? MindWarsTheme.PINK : MindWarsTheme.GRAY_LIGHT, 2, true));
    }

    private void selectBotDifficulty(String diff) {
        selectedBotDifficulty = diff;
        String[] diffs = { "Easy", "Medium", "Hard" };
        for (int i = 0; i < botDiffButtons.size(); i++) {
            botDiffButtons.get(i).setSelected(diffs[i].equals(diff));
        }
    }

    private void onNumPlayersChanged() {
        numPlayers = numPlayersCombo.getSelectedIndex() + 1;

        boolean vsBot = (numPlayers == 1);
        botDifficultyPanel.setVisible(vsBot);

        playerFieldsPanel.removeAll();
        JTextField[] fields = { player1Field, player2Field, player3Field, player4Field };
        // 1 player → show only player 1 field (bot opponent is automatic)
        // 2-4 players → show that many human name fields
        for (int i = 0; i < numPlayers; i++) {
            if (i > 0)
                playerFieldsPanel.add(Box.createVerticalStrut(8));
            playerFieldsPanel.add(fields[i]);
        }

        playerFieldsPanel.revalidate();
        playerFieldsPanel.repaint();

        if (scroller != null) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vsb = scroller.getVerticalScrollBar();
                vsb.setValue(vsb.getMaximum());
            });
        }
    }

    private void selectSize(int s) {
        this.mapSize = s;
        for (int i = 0; i < sizeButtons.size(); i++) {
            int val = new int[] { 3, 4, 5 }[i];
            restyleToggle(sizeButtons.get(i), val == s);
        }
    }

    private void onRandomToggled() {
        randomMode = randomCheck.isSelected();
        categoryCombo.setEnabled(!randomMode);
        difficultyCombo.setEnabled(!randomMode);
    }

    private void loadCategoriesFromBank() {
        QuestionBank bank = controller.getModel().getQuestionBank();
        if (bank == null)
            return;
        for (String cat : bank.getCategories()) {
            categoryCombo.addItem(cat);
        }
        rebuildDifficulties();
    }

    private void rebuildDifficulties() {
        difficultyCombo.removeAllItems();
        QuestionBank bank = controller.getModel().getQuestionBank();
        if (bank == null)
            return;
        Object sel = categoryCombo.getSelectedItem();
        if (sel == null)
            return;
        for (String diff : bank.getDifficulties(sel.toString())) {
            difficultyCombo.addItem(diff);
        }
    }

    private void onStart() {
        String p1 = player1Field.getText().isBlank() ? "Player 1" : player1Field.getText().trim();

        // 1 player → vs bot; 2+ → all humans, no bot
        boolean vsBot = (numPlayers == 1);
        String p2 = vsBot ? "Bot"
                : (player2Field.getText().isBlank() ? "Player 2" : player2Field.getText().trim());
        String p3 = numPlayers >= 3
                ? (player3Field.getText().isBlank() ? "Player 3" : player3Field.getText().trim())
                : "";
        String p4 = numPlayers >= 4
                ? (player4Field.getText().isBlank() ? "Player 4" : player4Field.getText().trim())
                : "";

        int totalPlayers = vsBot ? 2 : numPlayers;

        String category = null;
        String difficulty = null;
        if (!randomMode) {
            Object c = categoryCombo.getSelectedItem();
            Object d = difficultyCombo.getSelectedItem();
            category = (c == null) ? null : c.toString();
            difficulty = (d == null) ? null : d.toString();
        }

        GameSettings settings = new GameSettings(
                mapSize, vsBot,
                p1, p2, p3, p4,
                randomMode, category, difficulty, totalPlayers, vsBot ? selectedBotDifficulty : "Easy");

        controller.startNewGame(settings);
    }
}
