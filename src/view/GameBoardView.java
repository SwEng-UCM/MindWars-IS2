package view;

import bot.BotStrategy;
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
import util.SoundManager;

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

    private static final int ANSWER_PANEL_CHOICES_HEIGHT = 200;
    private static final int ANSWER_PANEL_TEXT_HEIGHT = 52;

    private final GameController controller;
    private final boolean invasionMode;

    // Header
    private final JLabel roundLabel;
    private final JLabel playerLabel;
    private final JLabel p1ScoreLabel;
    private final JLabel p2ScoreLabel;
    private JLabel p3ScoreLabel;
    private JLabel p4ScoreLabel;

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
    private final JPanel textWrap;
    private final JTextField textInput;
    private final JTextField orderingInput;
    private final SoundManager soundManager;
    private final ButtonGroup choiceGroup = new ButtonGroup();
    private java.util.List<JToggleButton> choiceButtons = new java.util.ArrayList<>();
    private boolean isTextQuestion = false;

    // Buttons row
    private final JButton submitButton;
    private final JButton undoButton;

    // Feedback overlay
    private final JLabel feedbackLabel;

    // Deferred acknowledgement fired ~1.6s after an answer; cancelled on undo.
    private Timer pendingAck;
    private Timer botPreviewTimer;
    private Timer botSubmitTimer;

    // Invasion battle state
    private String invasionAttackerAnswer = null;
    private boolean invasionDefenderTurn = false;

    // Previous cell ownership snapshot so we can animate cells that just
    // changed owner (territory conquest / invasion capture, #90).
    private char[][] previousOwners;

    private int localPlayerIndex = -1;

    public GameBoardView(GameController controller, boolean invasionMode, SoundManager soundManager) {
        this.controller = controller;
        this.invasionMode = invasionMode;
        this.soundManager = soundManager;

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

        JPanel scores = new JPanel(new GridLayout(1, 4, 12, 0));
        scores.setOpaque(false);
        p1ScoreLabel = new JLabel("", SwingConstants.RIGHT);
        p1ScoreLabel.setForeground(MindWarsTheme.PLAYER_X);
        p1ScoreLabel.setFont(MindWarsTheme.BODY_BOLD);
        p2ScoreLabel = new JLabel("", SwingConstants.RIGHT);
        p2ScoreLabel.setForeground(MindWarsTheme.PLAYER_O);
        p2ScoreLabel.setFont(MindWarsTheme.BODY_BOLD);
        p3ScoreLabel = new JLabel("", SwingConstants.RIGHT);
        p3ScoreLabel.setForeground(Color.CYAN);
        p3ScoreLabel.setFont(MindWarsTheme.BODY_BOLD);
        p4ScoreLabel = new JLabel("", SwingConstants.RIGHT);
        p4ScoreLabel.setForeground(Color.YELLOW);
        p4ScoreLabel.setFont(MindWarsTheme.BODY_BOLD);
        scores.add(p1ScoreLabel);
        scores.add(p2ScoreLabel);
        scores.add(p3ScoreLabel);
        scores.add(p4ScoreLabel);

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
        answerPanel.setMinimumSize(new Dimension(0, ANSWER_PANEL_TEXT_HEIGHT));
        answerPanel.setPreferredSize(new Dimension(300, ANSWER_PANEL_TEXT_HEIGHT));
        answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_TEXT_HEIGHT));

        choicesPanel = new JPanel();
        choicesPanel.setOpaque(false);
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));
        answerPanel.add(choicesPanel, "choices");

        textInput = MindWarsTheme.createTextField("Your answer");
        textInput.addActionListener(this::onSubmit);
        textInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        textInput.setPreferredSize(new Dimension(300, 42));

        orderingInput = MindWarsTheme.createTextField("Example: 2 1 3");
        orderingInput.addActionListener(this::onSubmit);
        orderingInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        orderingInput.setPreferredSize(new Dimension(300, 40));

        textWrap = new JPanel(new BorderLayout());
        textWrap.setOpaque(false);
        textWrap.add(textInput, BorderLayout.NORTH);
        textWrap.setMinimumSize(new Dimension(0, ANSWER_PANEL_TEXT_HEIGHT));
        textWrap.setPreferredSize(new Dimension(300, ANSWER_PANEL_TEXT_HEIGHT));
        textWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_TEXT_HEIGHT));

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

        Player activePlayer = invasionMode
                ? (invasionDefenderTurn ? model.getDefender() : model.getInvader())
                : model.getCurrentPlayer();

        int activeIndex = players.indexOf(activePlayer);
        boolean isBotTurn = !invasionMode && activePlayer != null && activePlayer.isBot();

        boolean isMyTurn = (localPlayerIndex < 0) || (localPlayerIndex == activeIndex);
        boolean canInteract = isMyTurn && !isBotTurn;

        if (isBotTurn) {
            playerLabel.setText("BOT TURN — " + activePlayer.getName() + " is thinking...");
            playerLabel.setForeground(MindWarsTheme.GRAY_LIGHT);
        } else if (isMyTurn) {
            playerLabel.setText("YOUR TURN — " + (invasionMode ? "Battle!" : "Press Ready"));
            playerLabel.setForeground(MindWarsTheme.PINK);
        } else {
            playerLabel.setText("Waiting for " + activePlayer.getName() + "...");
            playerLabel.setForeground(Color.GRAY);
        }

        if (players.size() >= 1)
            p1ScoreLabel.setText(players.get(0).getName() + ": " + players.get(0).getScore());
        if (players.size() >= 2)
            p2ScoreLabel.setText(players.get(1).getName() + ": " + players.get(1).getScore());
        if (players.size() >= 3)
            p3ScoreLabel.setText(players.get(2).getName() + ": " + players.get(2).getScore());
        else
            p3ScoreLabel.setText("");
        if (players.size() >= 4)
            p4ScoreLabel.setText(players.get(3).getName() + ": " + players.get(3).getScore());
        else
            p4ScoreLabel.setText("");

        // Grid
        rebuildGrid(model);

        // Question
        Question q = model.getCurrentQuestion();
        if (q == null) {
            categoryLabel.setText("");
            promptLabel.setText("<html>(no question)</html>");
            submitButton.setEnabled(false);
            return;
        }

        categoryLabel.setText((q.getCategory() == null ? "" : q.getCategory().toUpperCase())
                + "  •  " + (q.getDifficulty() == null ? "" : q.getDifficulty()));
        promptLabel.setText("<html><body style='width: 380px'>" + escape(q.getPrompt()) + "</body></html>");
        populateAnswerInput(q);

        feedbackLabel.setVisible(false);

        submitButton.setEnabled(canInteract);
        undoButton.setEnabled(canInteract);
        setAnswerInputEnabled(canInteract);

        // force swing to redraw
        this.revalidate();
        this.repaint();

    }

    public void setLocalPlayerIndex(int index) {
        this.localPlayerIndex = index;
    }

    /**
     * If the current player is a bot, schedules an auto-answer after the
     * bot's "thinking" delay. No-op in invasion mode (bots do not invade).
     */
    public void triggerBotAnswerIfNeeded() {

        if (invasionMode)
            return;

        GameModel model = controller.getModel();
        Player cur = model.getCurrentPlayer();
        if (cur == null || !cur.isBot() || cur.getStrategy() == null)
            return;

        Question q = model.getCurrentQuestion();
        if (q == null)
            return;

        BotStrategy strat = cur.getStrategy();
        long delay = Math.min(strat.getResponseTime(), GameModel.TIME_LIMIT_MS - 500);
        String botAnswer = resolveBotAnswer(strat, q);

        stopBotAnswerTimers();
        submitButton.setEnabled(false);
        undoButton.setEnabled(false);
        setAnswerInputEnabled(false);
        previewBotAnswer(q, botAnswer, delay);

        botSubmitTimer = new Timer((int) delay, ev -> {
            if (swingTimer != null) {
                swingTimer.stop();
            }

            soundManager.stopTimer();

            AnswerResult result = controller.onAnswerSubmitted(botAnswer, delay);
            if (result != null) {
                showFeedback(result);
            }

            schedulePendingAck();
        });

        botSubmitTimer.setRepeats(false);
        botSubmitTimer.start();
    }

    private void previewBotAnswer(Question q, String botAnswer, long totalDelayMs) {
        if (q == null) {
            return;
        }

        if ((q.getType() == QuestionType.MULTIPLE_CHOICE || q.getType() == QuestionType.TRUE_FALSE)
                && !choiceButtons.isEmpty()) {
            int index = previewChoiceIndex(q, botAnswer);
            if (index < 0 || index >= choiceButtons.size()) {
                return;
            }

            int waitMs = (int) Math.max(220, totalDelayMs / 3);
            botPreviewTimer = new Timer(waitMs, e -> {
                choiceGroup.clearSelection();
                choiceButtons.get(index).setSelected(true);
            });
            botPreviewTimer.setRepeats(false);
            botPreviewTimer.start();
            return;
        }

        JTextField target = (q.getType() == QuestionType.ORDERING) ? orderingInput : textInput;
        String text = (botAnswer == null) ? "" : botAnswer;
        target.setText("");
        if (text.isEmpty()) {
            return;
        }

        int totalChars = text.length();
        int typingBudget = (int) Math.max(250, Math.min(totalDelayMs - 180, totalDelayMs * 3 / 4));
        int stepMs = Math.max(35, typingBudget / totalChars);

        final int[] idx = { 0 };
        botPreviewTimer = new Timer(stepMs, e -> {
            if (idx[0] >= totalChars) {
                ((Timer) e.getSource()).stop();
                return;
            }
            idx[0]++;
            target.setText(text.substring(0, idx[0]));
            target.setCaretPosition(target.getText().length());
        });
        botPreviewTimer.setInitialDelay(Math.max(120, stepMs));
        botPreviewTimer.start();
    }

    private int previewChoiceIndex(Question q, String botAnswer) {
        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            if (botAnswer == null || botAnswer.isBlank()) {
                return -1;
            }
            char letter = Character.toUpperCase(botAnswer.trim().charAt(0));
            int idx = letter - 'A';
            return (idx >= 0 && idx < choiceButtons.size()) ? idx : -1;
        }

        if (q.getType() == QuestionType.TRUE_FALSE) {
            if (botAnswer == null || botAnswer.isBlank()) {
                return -1;
            }
            return Character.toUpperCase(botAnswer.trim().charAt(0)) == 'T' ? 0 : 1;
        }

        return -1;
    }

    private void stopBotAnswerTimers() {
        if (botPreviewTimer != null) {
            botPreviewTimer.stop();
            botPreviewTimer = null;
        }
        if (botSubmitTimer != null) {
            botSubmitTimer.stop();
            botSubmitTimer = null;
        }
    }

    private void setAnswerInputEnabled(boolean enabled) {
        textInput.setEnabled(enabled);
        textInput.setEditable(enabled);
        orderingInput.setEnabled(enabled);
        orderingInput.setEditable(enabled);

        for (JToggleButton tb : choiceButtons) {
            tb.setEnabled(enabled);
        }
    }

    private String resolveBotAnswer(BotStrategy strat, Question q) {
        String raw = strat.getAnswer(q);
        if (q.getType() == QuestionType.MULTIPLE_CHOICE && q.getChoices() != null) {
            if (raw != null && raw.length() == 1
                    && Character.isLetter(raw.charAt(0))) {
                return raw.toUpperCase();
            }
            int idx = (raw == null) ? -1 : q.getChoices().indexOf(raw);
            if (idx < 0)
                idx = 0;
            return String.valueOf((char) ('A' + idx));
        }
        if (q.getType() == QuestionType.TRUE_FALSE) {
            if (raw == null)
                return "T";
            return raw.trim().toUpperCase().startsWith("T") ? "T" : "F";
        }
        return raw == null ? "" : raw;
    }

    public void startTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
        }

        soundManager.stopTimer();
        soundManager.startTimer();

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
                soundManager.stopTimer();
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
        JLabel[][] labels = new JLabel[size][size];
        char[][] owners = new char[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                char owner = model.getMap().getOwner(r, c);
                owners[r][c] = owner;
                JLabel cell = buildCell(owner);
                labels[r][c] = cell;
                gridPanel.add(cell);

                // Animate cells that changed owner since the last render (#90).
                if (previousOwners != null
                        && previousOwners.length == size
                        && previousOwners[r][c] != owner
                        && owner != '.') {
                    animateOwnershipChange(cell, previousOwners[r][c], owner);
                }
            }
        }
        // Pulse the attack target in invasion battle mode so it's clear
        // which cell is in play.
        if (invasionMode && model.getAttackToRow() >= 0 && model.getAttackToCol() >= 0) {
            int ar = model.getAttackToRow();
            int ac = model.getAttackToCol();
            if (ar < size && ac < size) {
                AnimationHelper.pulseBorder(labels[ar][ac], MindWarsTheme.WRONG_RED, 3, 140);
            }
        }
        this.previousOwners = owners;
    }

    private JLabel buildCell(char owner) {
        JLabel cell = new JLabel("", SwingConstants.CENTER);
        cell.setOpaque(true);
        cell.setFont(MindWarsTheme.BODY_BOLD);
        cell.setPreferredSize(new Dimension(44, 44));
        cell.setBorder(BorderFactory.createLineBorder(MindWarsTheme.DARK_BORDER));
        switch (owner) {
            case 'X' -> {
                cell.setBackground(MindWarsTheme.PLAYER_X);
                cell.setForeground(MindWarsTheme.WHITE);
                cell.setText("X");
            }
            case 'O' -> {
                cell.setBackground(MindWarsTheme.PLAYER_O);
                cell.setForeground(MindWarsTheme.WHITE);
                cell.setText("O");
            }
            case 'A' -> {
                cell.setBackground(new Color(0, 200, 100));
                cell.setForeground(MindWarsTheme.WHITE);
                cell.setText("A");
            }
            case 'B' -> {
                cell.setBackground(new Color(255, 180, 0));
                cell.setForeground(MindWarsTheme.WHITE);
                cell.setText("B");
            }
            default -> cell.setBackground(MindWarsTheme.DARK_CARD);
        }
        return cell;
    }

    private void animateOwnershipChange(JLabel cell, char from, char to) {
        Color flash = (from == '.') ? MindWarsTheme.CORRECT_GREEN : MindWarsTheme.WRONG_RED;
        Color finalColor = switch (to) {
            case 'X' -> MindWarsTheme.PLAYER_X;
            case 'O' -> MindWarsTheme.PLAYER_O;
            case 'A' -> new Color(0, 200, 100);
            case 'B' -> new Color(255, 180, 0);
            default -> MindWarsTheme.DARK_CARD;
        };
        AnimationHelper.flashBackground(cell, flash, finalColor, 8, 70);
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
        choiceGroup.clearSelection();

        QuestionType type = q.getType();
        isTextQuestion = (type == QuestionType.NUMERIC || type == QuestionType.OPEN_ENDED
                || type == QuestionType.ORDERING);
        undoButton.setText(isTextQuestion ? "Clear" : "Undo");

        if (type == QuestionType.MULTIPLE_CHOICE && q.getChoices() != null) {
            answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_CHOICES_HEIGHT));
            char label = 'A';
            for (String choice : q.getChoices()) {
                JToggleButton tb = new JToggleButton(label + ") " + choice);
                styleToggleButton(tb);
                choiceGroup.add(tb);
                choiceButtons.add(tb);
                choicesPanel.add(tb);
                choicesPanel.add(Box.createVerticalStrut(6));
                label++;
            }
            cl.show(answerPanel, "choices");

        } else if (type == QuestionType.TRUE_FALSE) {
            answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_CHOICES_HEIGHT));
            for (String s : new String[] { "True", "False" }) {
                JToggleButton tb = new JToggleButton(s);
                styleToggleButton(tb);
                choiceGroup.add(tb);
                choiceButtons.add(tb);
                choicesPanel.add(tb);
                choicesPanel.add(Box.createVerticalStrut(6));
            }
            cl.show(answerPanel, "choices");

        } else if (type == QuestionType.ORDERING) {
            answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_CHOICES_HEIGHT));
            choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));
            // show ordering options
            List<String> items = q.getChoices();

            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    JLabel itemLabel = new JLabel((i + 1) + ". " + items.get(i));
                    itemLabel.setForeground(MindWarsTheme.WHITE);
                    itemLabel.setFont(MindWarsTheme.BODY_FONT);
                    itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    choicesPanel.add(itemLabel);
                    choicesPanel.add(Box.createVerticalStrut(5));
                }
            }
            choicesPanel.add(Box.createVerticalStrut(10));
            orderingInput.setText("");
            orderingInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            choicesPanel.add(orderingInput);
            cl.show(answerPanel, "choices");

        } else if (type == QuestionType.NUMERIC) {
            answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_TEXT_HEIGHT));
            textInput.setText("");
            textInput.setEnabled(true);
            textInput.setEditable(true);
            cl.show(answerPanel, "text");
            SwingUtilities.invokeLater(() -> textInput.requestFocusInWindow());

        } else {
            // OPEN_ENDED
            answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ANSWER_PANEL_TEXT_HEIGHT));
            textInput.setText("");
            textInput.setEnabled(true);
            textInput.setEditable(true);
            cl.show(answerPanel, "text");
            SwingUtilities.invokeLater(() -> textInput.requestFocusInWindow());
        }
        choicesPanel.revalidate();
        choicesPanel.repaint();
        answerPanel.revalidate();
        answerPanel.repaint();
    }

    // helper method for style
    private void styleToggleButton(JToggleButton tb) {
        tb.setFont(MindWarsTheme.BODY_FONT);
        tb.setFocusPainted(false);
        tb.setOpaque(true);
        tb.setContentAreaFilled(true);
        tb.setBorderPainted(true);
        tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        tb.setBackground(MindWarsTheme.DARK_CARD);
        tb.setForeground(Color.BLACK);
        tb.setAlignmentX(Component.LEFT_ALIGNMENT);
        tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        tb.addItemListener(e -> {
            if (tb.isSelected()) {
                tb.setBackground(MindWarsTheme.PINK);
            } else {
                tb.setBackground(MindWarsTheme.DARK_CARD);
            }
            tb.setForeground(Color.BLACK);
            tb.repaint();
        });
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
        Question q = controller.getModel().getCurrentQuestion();
        JTextField activeField = (q != null && q.getType() == QuestionType.ORDERING)
                ? orderingInput
                : textInput;
        String t = activeField.getText();
        return t == null || t.isBlank() ? null : t.trim();
    }

    // ── Actions ──

    private void onSubmit(ActionEvent e) {
        if (!submitButton.isEnabled())
            return;

        String answer = readAnswer();
        if (answer == null) {
            return;
        }

        if (swingTimer != null)
            swingTimer.stop();
        soundManager.stopTimer();

        long elapsed = System.currentTimeMillis() - timerStartMs;

        if (invasionMode) {
            handleInvasionSubmit(answer, elapsed);
            return;
        }

        Question q = controller.getModel().getCurrentQuestion();
        if (q.getType() == QuestionType.NUMERIC) {
            controller.onAnswerSubmitted(answer, elapsed);
            submitButton.setEnabled(false);
            controller.onAnswerAcknowledged();
            return;
        }

        AnswerResult result = controller.onAnswerSubmitted(answer, elapsed);
        showFeedback(result);
        submitButton.setEnabled(false);
        undoButton.setEnabled(false);

        schedulePendingAck();
    }

    private void onTimeout() {
        if (!submitButton.isEnabled())
            return;
        soundManager.stopTimer();
        long elapsed = GameModel.TIME_LIMIT_MS;

        if (invasionMode) {
            handleInvasionSubmit(null, elapsed);
            return;
        }

        AnswerResult result = controller.onAnswerSubmitted(null, elapsed);
        showFeedback(result);
        submitButton.setEnabled(false);
        undoButton.setEnabled(false);

        schedulePendingAck();
    }

    private void schedulePendingAck() {
        if (pendingAck != null)
            pendingAck.stop();
        stopBotAnswerTimers();
        pendingAck = new Timer(1600, ev -> {
            pendingAck = null;
            controller.onAnswerAcknowledged();
        });
        pendingAck.setRepeats(false);
        pendingAck.start();
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
        if (isTextQuestion) {
            Question q = controller.getModel().getCurrentQuestion();
            JTextField activeField = (q != null && q.getType() == QuestionType.ORDERING)
                    ? orderingInput
                    : textInput;
            activeField.setText("");
            activeField.requestFocusInWindow();
            return;
        }
        choiceGroup.clearSelection();
        for (JToggleButton tb : choiceButtons) {
            tb.setSelected(false);
        }
        choicesPanel.repaint();
    }

    private void showFeedback(AnswerResult result) {
        Question q = controller.getModel().getCurrentQuestion();

        // using a spare string if q disapears from model
        String correctAnswerText = formatCorrectAnswer(q, result);

        String text;
        Color bg;

        if (result.timedOut) {
            soundManager.play(SoundManager.INCORRECT);
        } else if (result.correct) {
            soundManager.play(SoundManager.CORRECT);
        } else {
            soundManager.play(SoundManager.INCORRECT);
        }

        if (result.timedOut) {
            text = "Time's up! Answer: " + correctAnswerText;
            bg = MindWarsTheme.WRONG_RED;
        } else if (result.correct) {
            text = "Correct! +" + result.pointsDelta;
            bg = MindWarsTheme.CORRECT_GREEN;
        } else {
            text = "Wrong — " + correctAnswerText;
            bg = MindWarsTheme.WRONG_RED;
        }
        // ui updates
        feedbackLabel.setText(text);
        feedbackLabel.setForeground(MindWarsTheme.WHITE);
        feedbackLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        feedbackLabel.setVisible(true);

        // Flash the background from a brighter tone down to the final color
        // and pulse the font so the feedback has more presence (#90).
        Color flashStart = bg.brighter();
        AnimationHelper.flashBackground(feedbackLabel, flashStart, bg, 10, 50);
        AnimationHelper.pulseFont(feedbackLabel, MindWarsTheme.HEADING_FONT, 8, 10, 50);

        feedbackLabel.revalidate();
        feedbackLabel.repaint();
    }

    private String formatCorrectAnswer(Question q, AnswerResult result) {
        if (q == null)
            return (result.correctAnswer != null) ? result.correctAnswer : "";

        switch (q.getType()) {
            case NUMERIC:
                return String.valueOf((int) q.getNumericAnswer());

            case ORDERING:
                if (q.getOrderingAnswer() != null) {
                    return String.join(" -> ", q.getOrderingAnswer());
                }
                return "";

            case MULTIPLE_CHOICE:
                String letter = result.correctAnswer;
                if (letter != null && letter.length() == 1) {
                    int idx = letter.toUpperCase().charAt(0) - 'A';
                    if (q.getChoices() != null && idx >= 0 && idx < q.getChoices().size()) {
                        return letter + ") " + q.getChoices().get(idx);
                    }
                }
                return (letter != null) ? letter : "";

            default:
                // true/false / open-ended
                return (q.getAnswer() != null) ? q.getAnswer() : result.correctAnswer;
        }
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void stopAllRoundAudio() {

        if (swingTimer != null) {
            swingTimer.stop();
        }

        stopBotAnswerTimers();

        if (pendingAck != null) {
            pendingAck.stop();
            pendingAck = null;
        }

        soundManager.stopTimer();

    }

}
