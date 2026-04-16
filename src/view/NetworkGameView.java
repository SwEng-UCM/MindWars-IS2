package view;

import controller.NavigationController;
import network.NetworkMessage;
import network.NetworkSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side gameplay screen for networked games (#85). Does not own a
 * {@link GameModel} — every update arrives as a {@link NetworkMessage}
 * from the {@link NetworkSession}. Turns are driven by the server, so the
 * "Ready" and "Submit" controls are only enabled when the server says
 * it's this client's turn.
 */
public class NetworkGameView extends JPanel {

    private final NavigationController nav;
    private final NetworkSession session;

    private final JLabel roundLabel;
    private final JLabel turnLabel;
    private final JLabel scoreLabel;
    private final JLabel promptLabel;
    private final JPanel choicesPanel;
    private final JTextField textInput;
    private final ButtonGroup choiceGroup = new ButtonGroup();
    private final List<JToggleButton> choiceButtons = new ArrayList<>();
    private final JButton readyButton;
    private final JButton submitButton;
    private final JLabel feedbackLabel;

    // Timer for the 15-second countdown, started on every QUESTION broadcast.
    private final JProgressBar timerBar;
    private Timer swingTimer;
    private long questionStartMs;

    private String currentPhase = "";
    private String currentQuestionType = "";
    private List<String> lastChoices = new ArrayList<>();
    private Integer currentPlayer;
    private String[] playerNames = new String[] { "Player 1", "Player 2" };

    public NetworkGameView(NavigationController nav, NetworkSession session) {
        this.nav = nav;
        this.session = session;

        setLayout(new BorderLayout());
        setBackground(MindWarsTheme.DARK_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Header ──
        JPanel header = new JPanel(new GridLayout(1, 3, 8, 0));
        header.setOpaque(false);
        roundLabel = headerLabel("Round -");
        roundLabel.setHorizontalAlignment(SwingConstants.LEFT);
        turnLabel = headerLabel("Waiting...");
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel = headerLabel("0 — 0");
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(roundLabel);
        header.add(turnLabel);
        header.add(scoreLabel);

        timerBar = MindWarsTheme.createTimerBar();

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(timerBar, BorderLayout.SOUTH);

        // ── Question card ──
        JPanel qCard = MindWarsTheme.createDarkCard();
        qCard.setLayout(new BoxLayout(qCard, BoxLayout.Y_AXIS));
        promptLabel = new JLabel("<html>Connecting...</html>");
        promptLabel.setForeground(MindWarsTheme.WHITE);
        promptLabel.setFont(MindWarsTheme.HEADING_FONT);
        promptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        choicesPanel = new JPanel();
        choicesPanel.setOpaque(false);
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));
        choicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textInput = MindWarsTheme.createTextField("Your answer");
        textInput.addActionListener(this::onSubmit);
        textInput.setVisible(false);

        qCard.add(promptLabel);
        qCard.add(Box.createVerticalStrut(12));
        qCard.add(choicesPanel);
        qCard.add(textInput);

        // ── Buttons ──
        readyButton = MindWarsTheme.createGradientButton("Ready");
        readyButton.addActionListener(e -> onReady());
        readyButton.setEnabled(false);

        submitButton = MindWarsTheme.createGradientButton("Submit");
        submitButton.addActionListener(this::onSubmit);
        submitButton.setEnabled(false);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttons.add(readyButton);
        buttons.add(submitButton);

        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(MindWarsTheme.HEADING_FONT);
        feedbackLabel.setOpaque(true);
        feedbackLabel.setVisible(false);
        feedbackLabel.setForeground(MindWarsTheme.WHITE);
        feedbackLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(qCard, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(bottom, BorderLayout.CENTER);
        center.add(feedbackLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        session.addMessageListener(this::onServerMessage);
    }

    // ── Server → view dispatch ──

    private void onServerMessage(NetworkMessage msg) {
        SwingUtilities.invokeLater(() -> handleMessage(msg));
    }

    private void handleMessage(NetworkMessage msg) {
        switch (msg.type) {
            case PHASE -> onPhase(msg);
            case QUESTION -> onQuestion(msg);
            case RESULT -> onResult(msg);
            case SCORES -> onScores(msg);
            case GAME_OVER -> onGameOver(msg);
            case ERROR -> feedback("Server: " + msg.errorMessage, MindWarsTheme.WRONG_RED);
            default -> {
                // LOBBY/TURN/WELCOME already handled elsewhere.
            }
        }
    }

    private void onPhase(NetworkMessage msg) {
        currentPhase = msg.phase == null ? "" : msg.phase;
        currentPlayer = msg.currentPlayer;
        if (msg.round != null && msg.totalRounds != null) {
            roundLabel.setText("Round " + msg.round + " / " + msg.totalRounds);
        }
        stopTimer();
        feedbackLabel.setVisible(false);

        boolean myTurn = isMyTurn();

        switch (currentPhase) {
            case "HOT_SEAT_PASS" -> {
                promptLabel.setText("<html>Your turn — press Ready.</html>");
                choicesPanel.removeAll();
                textInput.setVisible(false);
                readyButton.setEnabled(myTurn);
                submitButton.setEnabled(false);
                turnLabel.setText(myTurn
                        ? "Your turn"
                        : "Waiting for " + nameOf(currentPlayer) + "...");
            }
            case "QUESTION", "INVASION_BATTLE" -> {
                readyButton.setEnabled(false);
                submitButton.setEnabled(myTurn);
                turnLabel.setText(myTurn
                        ? "Your turn — answer now"
                        : nameOf(currentPlayer) + " is answering...");
            }
            case "TERRITORY_CLAIM", "INVASION_PASS", "INVASION_SELECT" -> {
                readyButton.setEnabled(false);
                submitButton.setEnabled(false);
                promptLabel.setText("<html>Phase: " + humanPhase(currentPhase) + "</html>");
                choicesPanel.removeAll();
                textInput.setVisible(false);
                turnLabel.setText(humanPhase(currentPhase));
            }
            case "GAME_OVER" -> {
                readyButton.setEnabled(false);
                submitButton.setEnabled(false);
                turnLabel.setText("Game over");
            }
            default -> {
                // Unknown phases are no-ops.
            }
        }
        revalidate();
        repaint();
    }

    private void onQuestion(NetworkMessage msg) {
        String prompt = msg.prompt == null ? "" : msg.prompt;
        currentQuestionType = msg.questionType == null ? "" : msg.questionType;
        lastChoices = msg.choices == null ? new ArrayList<>() : msg.choices;
        String header = (msg.category == null ? "" : msg.category.toUpperCase())
                + (msg.difficulty == null ? "" : " • " + msg.difficulty);
        promptLabel.setText("<html><body style='width: 360px'><small>" + escape(header)
                + "</small><br><br>" + escape(prompt) + "</body></html>");

        choicesPanel.removeAll();
        choiceButtons.clear();
        for (java.util.Enumeration<AbstractButton> en = choiceGroup.getElements(); en.hasMoreElements();) {
            choiceGroup.remove(en.nextElement());
        }

        if ("MULTIPLE_CHOICE".equals(currentQuestionType) && !lastChoices.isEmpty()) {
            textInput.setVisible(false);
            char label = 'A';
            for (String choice : lastChoices) {
                JToggleButton tb = new JToggleButton(label + ") " + choice);
                styleToggle(tb);
                choiceGroup.add(tb);
                choiceButtons.add(tb);
                choicesPanel.add(tb);
                choicesPanel.add(Box.createVerticalStrut(6));
                label++;
            }
        } else if ("TRUE_FALSE".equals(currentQuestionType)) {
            textInput.setVisible(false);
            for (String s : new String[] { "True", "False" }) {
                JToggleButton tb = new JToggleButton(s);
                styleToggle(tb);
                choiceGroup.add(tb);
                choiceButtons.add(tb);
                choicesPanel.add(tb);
                choicesPanel.add(Box.createVerticalStrut(6));
            }
        } else {
            textInput.setText("");
            textInput.setVisible(true);
            SwingUtilities.invokeLater(textInput::requestFocusInWindow);
        }

        feedbackLabel.setVisible(false);
        submitButton.setEnabled(isMyTurn());
        startTimer();
        revalidate();
        repaint();
    }

    private void onResult(NetworkMessage msg) {
        stopTimer();
        String text;
        Color bg;
        if (Boolean.TRUE.equals(msg.timedOut)) {
            text = "Time's up! Answer: " + (msg.correctAnswer == null ? "" : msg.correctAnswer);
            bg = MindWarsTheme.WRONG_RED;
        } else if (Boolean.TRUE.equals(msg.correct)) {
            text = "Correct! +" + (msg.pointsDelta == null ? 0 : msg.pointsDelta);
            bg = MindWarsTheme.CORRECT_GREEN;
        } else {
            text = "Wrong — " + (msg.correctAnswer == null ? "" : msg.correctAnswer);
            bg = MindWarsTheme.WRONG_RED;
        }
        feedback(text, bg);
        submitButton.setEnabled(false);
    }

    private void onScores(NetworkMessage msg) {
        if (msg.scores == null || msg.scores.size() < 2) return;
        if (msg.playerNames != null && msg.playerNames.size() >= 2) {
            playerNames[0] = msg.playerNames.get(0);
            playerNames[1] = msg.playerNames.get(1);
        }
        scoreLabel.setText(playerNames[0] + ": " + msg.scores.get(0)
                + "   " + playerNames[1] + ": " + msg.scores.get(1));
    }

    private void onGameOver(NetworkMessage msg) {
        stopTimer();
        submitButton.setEnabled(false);
        String winner = (msg.winnerIndex == null) ? "It's a draw!" : nameOf(msg.winnerIndex) + " wins!";
        feedback("Game over — " + winner, MindWarsTheme.PINK);
        readyButton.setText("Back to Menu");
        readyButton.setEnabled(true);
        for (var al : readyButton.getActionListeners()) readyButton.removeActionListener(al);
        readyButton.addActionListener(e -> {
            session.disconnect();
            nav.showMainMenu();
        });
    }

    // ── View → server ──

    private void onReady() {
        if (!isMyTurn() || !session.isConnected()) return;
        session.getClient().sendReady();
        readyButton.setEnabled(false);
    }

    private void onSubmit(ActionEvent e) {
        if (!isMyTurn() || !session.isConnected()) return;
        String answer = readAnswer();
        if (answer == null) return;
        long elapsed = System.currentTimeMillis() - questionStartMs;
        session.getClient().sendAnswer(answer, elapsed);
        submitButton.setEnabled(false);
        stopTimer();
    }

    // ── Helpers ──

    private boolean isMyTurn() {
        Integer me = session.getMyPlayerIndex();
        return me != null && currentPlayer != null && me.equals(currentPlayer);
    }

    private String nameOf(Integer index) {
        if (index == null) return "opponent";
        if (index < 0 || index >= playerNames.length) return "Player " + (index + 1);
        return playerNames[index];
    }

    private String readAnswer() {
        if (!choiceButtons.isEmpty()) {
            for (int i = 0; i < choiceButtons.size(); i++) {
                if (choiceButtons.get(i).isSelected()) {
                    if ("MULTIPLE_CHOICE".equals(currentQuestionType)) {
                        return String.valueOf((char) ('A' + i));
                    }
                    return choiceButtons.get(i).getText();
                }
            }
            return null;
        }
        String t = textInput.getText();
        return t == null || t.isBlank() ? null : t.trim();
    }

    private void startTimer() {
        stopTimer();
        questionStartMs = System.currentTimeMillis();
        timerBar.setValue(100);
        swingTimer = new Timer(100, e -> {
            long elapsed = System.currentTimeMillis() - questionStartMs;
            long remaining = Math.max(0, model.GameModel.TIME_LIMIT_MS - elapsed);
            int pct = (int) ((remaining * 100L) / model.GameModel.TIME_LIMIT_MS);
            timerBar.setValue(pct);
            if (remaining <= 0) {
                swingTimer.stop();
            }
        });
        swingTimer.start();
    }

    private void stopTimer() {
        if (swingTimer != null) swingTimer.stop();
    }

    private void feedback(String text, Color bg) {
        feedbackLabel.setText(text);
        feedbackLabel.setVisible(true);
        AnimationHelper.flashBackground(feedbackLabel, bg.brighter(), bg, 10, 50);
        AnimationHelper.pulseFont(feedbackLabel, MindWarsTheme.HEADING_FONT, 6, 10, 50);
        feedbackLabel.revalidate();
        feedbackLabel.repaint();
    }

    private JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MindWarsTheme.WHITE);
        l.setFont(MindWarsTheme.BODY_BOLD);
        return l;
    }

    private void styleToggle(JToggleButton tb) {
        tb.setFont(MindWarsTheme.BODY_FONT);
        tb.setFocusPainted(false);
        tb.setBackground(MindWarsTheme.DARK_CARD);
        tb.setForeground(MindWarsTheme.WHITE);
        tb.setAlignmentX(Component.LEFT_ALIGNMENT);
        tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private static String humanPhase(String phase) {
        return switch (phase) {
            case "TERRITORY_CLAIM" -> "Territory claim";
            case "INVASION_PASS" -> "Invasion — pass device";
            case "INVASION_SELECT" -> "Invasion — pick target";
            default -> phase;
        };
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
