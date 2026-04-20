package view;

import controller.GameController;
import model.GameSettings;
import trivia.QuestionBank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Setup wizard — addresses #74. Lets the player choose map size, game mode
 * (vs another player or vs bot), player names, and category/difficulty
 * (or random) before starting a new game.
 *
 * <p>
 * Single-panel form rather than multi-step for now; every option is
 * visible at once so the player can tweak and hit "Start".
 */
public class GameSetupView extends JPanel {

    private final GameController controller;

    // Form state
    private int mapSize = 4;
    private boolean vsBot = false;
    private boolean randomMode = true;

    // Widgets
    private final JTextField player1Field;
    private final JTextField player2Field;
    private final JTextField player3Field;
    private final JTextField player4Field;
    private int numPlayers = 2;
    private final List<JToggleButton> sizeButtons = new ArrayList<>();
    private final List<JToggleButton> modeButtons = new ArrayList<>();
    private final JCheckBox randomCheck;
    private final JComboBox<String> categoryCombo;
    private final JComboBox<String> difficultyCombo;

    public GameSetupView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 640));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        card.add(MindWarsTheme.centeredLabel("New Game",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(18));

        // ── Player names ──
        card.add(sectionLabel("Players"));
        card.add(Box.createVerticalStrut(6));
        player1Field = MindWarsTheme.createTextField("Player 1 name");
        player1Field.setText("Player 1");
        player2Field = MindWarsTheme.createTextField("Player 2 name");
        player2Field.setText("Player 2");
        player3Field = MindWarsTheme.createTextField("Player 3 name");
        player4Field = MindWarsTheme.createTextField("Player 4 name");

        card.add(player1Field);
        card.add(Box.createVerticalStrut(8));
        card.add(player2Field);
        card.add(Box.createVerticalStrut(8));
        card.add(player3Field);
        card.add(Box.createVerticalStrut(8));
        card.add(player4Field);
        card.add(Box.createVerticalStrut(16));

        // ── Game mode ──
        card.add(sectionLabel("Mode"));
        card.add(Box.createVerticalStrut(6));
        JPanel modeRow = new JPanel(new GridLayout(1, 2, 10, 0));
        modeRow.setOpaque(false);
        modeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JToggleButton vsPlayer = optionToggle("vs Player", true);
        JToggleButton vsBotBtn = optionToggle("vs Bot", false);
        vsPlayer.addActionListener(e -> selectMode(vsPlayer, vsBotBtn, false));
        vsBotBtn.addActionListener(e -> selectMode(vsPlayer, vsBotBtn, true));
        modeButtons.add(vsPlayer);
        modeButtons.add(vsBotBtn);
        modeRow.add(vsPlayer);
        modeRow.add(vsBotBtn);
        card.add(modeRow);
        card.add(Box.createVerticalStrut(16));

        // ── Map size ──
        card.add(sectionLabel("Map size"));
        card.add(Box.createVerticalStrut(6));
        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 10, 0));
        sizeRow.setOpaque(false);
        sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
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
        categoryCombo.addActionListener(e -> rebuildDifficulties());
        card.add(categoryCombo);
        card.add(Box.createVerticalStrut(8));

        difficultyCombo = new JComboBox<>();
        difficultyCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        difficultyCombo.setFont(MindWarsTheme.BODY_FONT);
        card.add(difficultyCombo);
        card.add(Box.createVerticalStrut(20));

        // Populate from the question bank.
        loadCategoriesFromBank();
        onRandomToggled();

        // ── Buttons ──
        JButton start = MindWarsTheme.createGradientButton("Start Game");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.addActionListener(e -> onStart());
        card.add(start);
        card.add(Box.createVerticalStrut(10));

        JButton back = MindWarsTheme.createPinkButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> controller.returnToMenu());
        card.add(back);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
    }

    /** Called by the nav layer before showing this view. */
    public void reset() {
        // Preserve form state between visits — nothing to do.
    }

    // ── Form helpers ──

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

    private void restyleToggle(JToggleButton tb, boolean selected) {
        tb.setSelected(selected);
        tb.setBackground(selected ? MindWarsTheme.PINK_BG : MindWarsTheme.WHITE);
        tb.setForeground(selected ? MindWarsTheme.PINK : Color.DARK_GRAY);
        tb.setBorder(BorderFactory.createLineBorder(
                selected ? MindWarsTheme.PINK : MindWarsTheme.GRAY_LIGHT, 2, true));
    }

    private void selectMode(JToggleButton vsPlayer, JToggleButton vsBotBtn, boolean bot) {
        this.vsBot = bot;
        restyleToggle(vsPlayer, !bot);
        restyleToggle(vsBotBtn, bot);
        player2Field.setText(bot ? "Bot" : "Player 2");
        player2Field.setEnabled(!bot);
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
        String p2 = player2Field.getText().isBlank() ? (vsBot ? "Bot" : "Player 2") : player2Field.getText().trim();
        String p3 = player3Field.getText().trim();
        String p4 = player4Field.getText().trim();

        int finalNumPlayers = 2;
        if (!p4.isEmpty())
            finalNumPlayers = 4;
        else if (!p3.isEmpty())
            finalNumPlayers = 3;

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
                randomMode, category, difficulty, finalNumPlayers);

        controller.startNewGame(settings);
    }
}
