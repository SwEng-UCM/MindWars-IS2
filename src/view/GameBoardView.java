package view;

import controller.GameController;
import model.AnswerResult;
import model.GameModel;
import player.Player;
import trivia.Question;
import trivia.QuestionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * The main gameplay screen. Addresses #66: shows the grid map, both
 * players' scores, the current question, a live 15-second timer bar, and
 * the answer input, all on a single screen.
 *
 * <p>
 * Also hosts the Undo button introduced in #82 (delegates to the
 * command history set up by #81).
 *
 * <p>
 * Used in two modes: regular question phase and invasion battle
 * (constructor's {@code invasionMode} flag). In invasion mode the attacker
 * answers first, then the defender answers, and finally the controller
 * resolves the battle.
 */
public class GameBoardView extends JPanel {

    private final GameController controller;
    private final boolean invasionMode;

    // Header
    private final JLabel roundLabel;
    private final JLabel playerLabel;
    private final JLabel p1ScoreLabel;
    private final JLabel p2ScoreLabel;

    // Timer
    private final JProgressBar timerBar;
    private final JLabel timerLabel;
    private Timer swingTimer;
    private long timerStartMs;

    // Grid
    private final JPanel gridPanel;

    // Question card
    private final JLabel categoryLabel;
    private final JLabel promptLabel;
    private final JPanel answerPanel;
    private final JPanel choicesPanel;
    private final JTextField textInput;
    private final ButtonGroup choiceGroup = new ButtonGroup();
    private java.util.List<JToggleButton> choiceButtons = new java.util.ArrayList<>();

    // Buttons row
    private final JButton submitButton;
    private final JButton undoButton;

    // Feedback overlay
    private final JLabel feedbackLabel;

    // Invasion battle state
    private String invasionAttackerAnswer = null;
    private boolean invasionDefenderTurn = false;

    public GameBoardView(GameController controller, boolean invasionMode) {
        this.controller = controller;
        this.invasionMode = invasionMode;

        setLayout(new BorderLayout());
        setBackground(MindWarsTheme.DARK_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Header: round + player + scores ──
        JPanel header = new JPanel(new GridLayout(1, 3, 8, 0));
        header.setOpaque(false);

        roundLabel = new JLabel("", SwingConstants.LEFT);
        roundLabel.setForeground(MindWarsTheme.GRAY_LIGHT);
        roundLabel.setFont(MindWarsTheme.SMALL_FONT);

        playerLabel = new JLabel("", SwingConstants.CENTER);
        playerLabel.setForeground(MindWarsTheme.WHITE);
        playerLabel.setFont(MindWarsTheme.BODY_BOLD);

        JPanel scores = new JPanel(new GridLayout(1, 2, 12, 0));
        scores.setOpaque(false);
        p1ScoreLabel = new JLabel("", SwingConstants.RIGHT);
        p1ScoreLabel.setForeground(MindWarsTheme.PLAYER_X);
        p1ScoreLabel.setFont(MindWarsTheme.BODY_BOLD);
        p2ScoreLabel = new JLabel("", SwingConstants.RIGHT);
        p2ScoreLabel.setForeground(MindWarsTheme.PLAYER_O);
        p2ScoreLabel.setFont(MindWarsTheme.BODY_BOLD);
        scores.add(p1ScoreLabel);
        scores.add(p2ScoreLabel);

        header.add(roundLabel);
        header.add(playerLabel);
        header.add(scores);

        // ── Timer row ──
        JPanel timerRow = new JPanel(new BorderLayout(8, 0));
        timerRow.setOpaque(false);
        timerRow.setBorder(new EmptyBorder(10, 0, 6, 0));
        timerBar = MindWarsTheme.createTimerBar();
        timerLabel = new JLabel("15s");
        timerLabel.setForeground(MindWarsTheme.WHITE);
        timerLabel.setFont(MindWarsTheme.BODY_BOLD);
        timerRow.add(timerBar, BorderLayout.CENTER);
        timerRow.add(timerLabel, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(timerRow, BorderLayout.SOUTH);

        // ── Grid map ──
        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        // ── Question card ──
        JPanel qCard = MindWarsTheme.createDarkCard();
        qCard.setLayout(new BoxLayout(qCard, BoxLayout.Y_AXIS));

        categoryLabel = new JLabel("");
        categoryLabel.setForeground(MindWarsTheme.PINK);
        categoryLabel.setFont(MindWarsTheme.SMALL_FONT);
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        promptLabel = new JLabel("<html></html>");
        promptLabel.setForeground(MindWarsTheme.WHITE);
        promptLabel.setFont(MindWarsTheme.HEADING_FONT);
        promptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        answerPanel = new JPanel(new CardLayout());
        answerPanel.setOpaque(false);
        answerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        choicesPanel = new JPanel();
        choicesPanel.setOpaque(false);
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));
        answerPanel.add(choicesPanel, "choices");

        textInput = MindWarsTheme.createTextField("Your answer");
        textInput.addActionListener(this::onSubmit);
        JPanel textWrap = new JPanel(new BorderLayout());
        textWrap.setOpaque(false);
        textWrap.add(textInput, BorderLayout.NORTH);
        answerPanel.add(textWrap, "text");

        qCard.add(categoryLabel);
        qCard.add(Box.createVerticalStrut(6));
        qCard.add(promptLabel);
        qCard.add(Box.createVerticalStrut(14));
        qCard.add(answerPanel);

        // ── Bottom: submit + undo ──
        submitButton = MindWarsTheme.createGradientButton("Submit");
        submitButton.addActionListener(this::onSubmit);

        undoButton = MindWarsTheme.createPinkButton("Undo");
        undoButton.addActionListener(e -> onUndo());
        undoButton.setEnabled(false);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttons.add(undoButton);
        buttons.add(submitButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(qCard, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        // ── Feedback overlay label (stacked at bottom) ──
        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(MindWarsTheme.HEADING_FONT);
        feedbackLabel.setOpaque(true);
        feedbackLabel.setVisible(false);

        // ── Assemble ──
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(gridPanel, BorderLayout.NORTH);
        center.add(bottom, BorderLayout.CENTER);
        center.add(feedbackLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    // ── Public API used by MainFrame ──

    /** Refreshes header, grid and question card against the current model. */
    public void refresh() {
        GameModel model = controller.getModel();
        List<Player> players = model.getPlayers();

        // Header
        roundLabel.setText("Round " + model.getRoundNumber() + " / " + model.getTotalRounds());
        Player p = invasionMode
                ? (invasionDefenderTurn ? model.getDefender() : model.getInvader())
                : model.getCurrentPlayer();
        playerLabel.setText((invasionMode
                ? (invasionDefenderTurn ? "Defender: " : "Attacker: ")
                : "Turn: ") + p.getName());
        p1ScoreLabel.setText(players.get(0).getName() + ": " + players.get(0).getScore());
        p2ScoreLabel.setText(players.get(1).getName() + ": " + players.get(1).getScore());

        // Grid
        rebuildGrid(model);

        // Question
        Question q = model.getCurrentQuestion();
        if (q == null) {
            categoryLabel.setText("");
            promptLabel.setText("<html>(no question)</html>");
            return;
        }
        categoryLabel.setText((q.getCategory() == null ? "" : q.getCategory().toUpperCase())
                + "  •  " + (q.getDifficulty() == null ? "" : q.getDifficulty()));
        promptLabel.setText("<html><body style='width: 380px'>" + escape(q.getPrompt()) + "</body></html>");
        populateAnswerInput(q);

        // Reset per-question UI
        feedbackLabel.setVisible(false);
        submitButton.setEnabled(true);
        undoButton.setEnabled(controller.canUndo());

        revalidate();
        repaint();
    }

    /** Starts the 15 s countdown. Called by MainFrame when this screen is shown. */
    public void startTimer() {
        if (swingTimer != null)
            swingTimer.stop();
        timerStartMs = System.currentTimeMillis();
        timerBar.setValue(100);
        timerLabel.setText("15s");

        swingTimer = new Timer(100, e -> {
            long elapsed = System.currentTimeMillis() - timerStartMs;
            long remaining = Math.max(0, GameModel.TIME_LIMIT_MS - elapsed);
            int pct = (int) ((remaining * 100L) / GameModel.TIME_LIMIT_MS);
            timerBar.setValue(pct);
            timerLabel.setText((remaining / 1000) + "s");
            if (remaining <= 0) {
                swingTimer.stop();
                onTimeout();
            }
        });
        swingTimer.start();
    }

    // ── Grid rendering ──

    private void rebuildGrid(GameModel model) {
        gridPanel.removeAll();
        if (model.getMap() == null)
            return;
        int size = model.getMap().getSize();
        gridPanel.setLayout(new GridLayout(size, size, 4, 4));
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                gridPanel.add(buildCell(model, r, c));
            }
        }
    }

    private JComponent buildCell(GameModel model, int r, int c) {
        char owner = model.getMap().getOwner(r, c);
        JLabel cell = new JLabel("", SwingConstants.CENTER);
        cell.setOpaque(true);
        cell.setFont(MindWarsTheme.BODY_BOLD);
        cell.setPreferredSize(new Dimension(44, 44));
        cell.setBorder(BorderFactory.createLineBorder(MindWarsTheme.DARK_BORDER));
        if (owner == 'X') {
            cell.setBackground(MindWarsTheme.PLAYER_X);
            cell.setForeground(MindWarsTheme.WHITE);
            cell.setText("X");
        } else if (owner == 'O') {
            cell.setBackground(MindWarsTheme.PLAYER_O);
            cell.setForeground(MindWarsTheme.WHITE);
            cell.setText("O");
        } else {
            cell.setBackground(MindWarsTheme.DARK_CARD);
        }
        return cell;
    }

    // ── Answer input ──

    private void populateAnswerInput(Question q) {
        CardLayout cl = (CardLayout) answerPanel.getLayout();
        choicesPanel.removeAll();
        choiceButtons.clear();
        // Rebuild the button group
        for (java.util.Enumeration<AbstractButton> en = choiceGroup.getElements(); en.hasMoreElements();) {
            choiceGroup.remove(en.nextElement());
        }

        QuestionType type = q.getType();
        if (type == QuestionType.MULTIPLE_CHOICE && q.getChoices() != null) {
            char label = 'A';
            for (String choice : q.getChoices()) {
                JToggleButton tb = new JToggleButton(label + ") " + choice);
                tb.setFont(MindWarsTheme.BODY_FONT);
                tb.setFocusPainted(false);
                tb.setBackground(MindWarsTheme.DARK_CARD);
                tb.setForeground(MindWarsTheme.WHITE);
                tb.setAlignmentX(Component.LEFT_ALIGNMENT);
                tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                choiceGroup.add(tb);
                choiceButtons.add(tb);
                choicesPanel.add(tb);
                choicesPanel.add(Box.createVerticalStrut(6));
                label++;
            }
            cl.show(answerPanel, "choices");
        } else if (type == QuestionType.TRUE_FALSE) {
            for (String s : new String[] { "True", "False" }) {
                JToggleButton tb = new JToggleButton(s);
                tb.setFont(MindWarsTheme.BODY_FONT);
                tb.setFocusPainted(false);
                tb.setBackground(MindWarsTheme.DARK_CARD);
                tb.setForeground(MindWarsTheme.WHITE);
                tb.setAlignmentX(Component.LEFT_ALIGNMENT);
                tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                choiceGroup.add(tb);
                choiceButtons.add(tb);
                choicesPanel.add(tb);
                choicesPanel.add(Box.createVerticalStrut(6));
            }
            cl.show(answerPanel, "choices");
        } else {
            // NUMERIC, OPEN_ENDED, ORDERING → free text
            textInput.setText("");
            cl.show(answerPanel, "text");
        }
    }

    private String readAnswer() {
        if (!choiceButtons.isEmpty()) {
            for (int i = 0; i < choiceButtons.size(); i++) {
                if (choiceButtons.get(i).isSelected()) {
                    Question q = controller.getModel().getCurrentQuestion();
                    // JToggleButton tb = choiceButtons.get(i);
                    // String txt = tb.getText();
                    // Strip "A) " prefix for MCQ
                    if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
                        return String.valueOf((char) ('A' + i));
                    }
                    // int paren = txt.indexOf(") ");
                    // return paren > 0 ? txt.substring(paren + 2) : txt;
                    return choiceButtons.get(i).getText();
                }
            }
            return null;
        }
        String t = textInput.getText();
        return t == null || t.isBlank() ? null : t.trim();
    }

    // ── Actions ──

    private void onSubmit(ActionEvent e) {
        if (!submitButton.isEnabled())
            return;
        String answer = readAnswer();
        if (answer == null) {
            // Require a selection / text before submitting (unless timeout).
            return;
        }
        if (swingTimer != null)
            swingTimer.stop();
        long elapsed = System.currentTimeMillis() - timerStartMs;

        if (invasionMode) {
            handleInvasionSubmit(answer, elapsed);
            return;
        }

        AnswerResult result = controller.onAnswerSubmitted(answer, elapsed);
        showFeedback(result);
        submitButton.setEnabled(false);
        undoButton.setEnabled(controller.canUndo());

        // After feedback delay, advance.
        Timer done = new Timer(1600, ev -> controller.onAnswerAcknowledged());
        done.setRepeats(false);
        done.start();
    }

    private void onTimeout() {
        if (!submitButton.isEnabled())
            return;
        long elapsed = GameModel.TIME_LIMIT_MS;

        if (invasionMode) {
            handleInvasionSubmit(null, elapsed);
            return;
        }

        AnswerResult result = controller.onAnswerSubmitted(null, elapsed);
        showFeedback(result);
        submitButton.setEnabled(false);

        Timer done = new Timer(1600, ev -> controller.onAnswerAcknowledged());
        done.setRepeats(false);
        done.start();
    }

    private void handleInvasionSubmit(String answer, long elapsed) {
        if (!invasionDefenderTurn) {
            // Attacker just answered; switch to defender.
            invasionAttackerAnswer = answer;
            invasionDefenderTurn = true;
            refresh();
            startTimer();
        } else {
            String defAnswer = answer;
            String attAnswer = invasionAttackerAnswer;
            invasionAttackerAnswer = null;
            invasionDefenderTurn = false;
            submitButton.setEnabled(false);
            controller.onInvasionResolved(attAnswer, defAnswer);
        }
    }

    private void onUndo() {
        if (controller.undoLast()) {
            refresh();
        }
    }

    private void showFeedback(AnswerResult result) {
        String text;
        Color bg;
        if (result.timedOut) {
            text = "Time's up! Answer: " + result.correctAnswer;
            bg = MindWarsTheme.WRONG_RED;
        } else if (result.correct) {
            text = "Correct! +" + result.pointsDelta;
            bg = MindWarsTheme.CORRECT_GREEN;
        } else {
            text = "Wrong — " + result.correctAnswer;
            bg = MindWarsTheme.WRONG_RED;
        }
        feedbackLabel.setText(text);
        feedbackLabel.setBackground(bg);
        feedbackLabel.setForeground(MindWarsTheme.WHITE);
        feedbackLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        feedbackLabel.setVisible(true);
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
