package view;

import controller.NavigationController;
import model.GameModel;
import network.NetworkMessage;
import network.NetworkSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side gameplay screen for networked games (#85).
 *
 * Territory claim additions:
 * - Handles MAP_UPDATE messages: renders the grid and enables cell buttons
 * only for the player whose turn it is to claim.
 * - Sends CLAIM_CELL to the server when a cell button is clicked.
 * - Shows an instruction label and a "done" notice when all picks are done.
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

    private final JProgressBar timerBar;
    private Timer swingTimer;
    private long questionStartMs;

    private String currentPhase = "";
    private String currentQuestionType = "";
    private List<String> lastChoices = new ArrayList<>();
    private Integer currentPlayer;
    private String[] playerNames = new String[] { "Player 1", "Player 2", "Player 3", "Player 4" };

    private final JPanel claimPanel;
    private final JLabel claimInstructionLabel;
    private final JPanel claimGridPanel;
    /** Cached grid state for building buttons. */
    private String cachedGridSnapshot = null;
    private int cachedMapSize = 0;
    /** Index of the player who should be claiming right now (-1 = done). */
    private int claimingPlayer = -1;

    // ── Chat ─────────────────────────────────────────────────────────────
    private JTextArea chatArea;
    private JTextField chatInputField;

    public NetworkGameView(NavigationController nav, NetworkSession session) {
        this.nav = nav;
        this.session = session;

        setLayout(new BorderLayout(10, 0));
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

        // ── Territory claim panel (NEW) ──────────────────────────────────
        claimPanel = new JPanel(new BorderLayout(0, 10));
        claimPanel.setOpaque(false);
        claimPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        claimInstructionLabel = new JLabel("", SwingConstants.CENTER);
        claimInstructionLabel.setFont(MindWarsTheme.BODY_BOLD);
        claimInstructionLabel.setForeground(MindWarsTheme.GRAY_LIGHT);
        claimPanel.add(claimInstructionLabel, BorderLayout.NORTH);

        claimGridPanel = new JPanel();
        claimGridPanel.setOpaque(false);
        claimPanel.add(claimGridPanel, BorderLayout.CENTER);

        // ── Center card layout: question vs claim ──
        JPanel centerCard = new JPanel(new CardLayout());
        centerCard.setOpaque(false);
        centerCard.add(bottom, "question");
        centerCard.add(claimPanel, "claim");

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(centerCard, BorderLayout.CENTER);
        center.add(feedbackLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        setupChatPanel();

        session.addMessageListener(this::onServerMessage);
    }

    // ── Chat ─────────────────────────────────────────────────────────────

    private void setupChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setOpaque(false);
        chatPanel.setPreferredSize(new Dimension(280, 0));
        chatPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MindWarsTheme.PINK), "BATTLE CHAT",
                0, 0, MindWarsTheme.SMALL_FONT, MindWarsTheme.PINK));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(20, 20, 30));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);

        chatInputField = MindWarsTheme.createTextField("Press Enter to send...");
        chatInputField.addActionListener(e -> sendChatMessage());

        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(chatInputField, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.EAST);
    }

    private void sendChatMessage() {
        String text = chatInputField.getText().trim();
        if (!text.isEmpty() && session.isConnected()) {
            NetworkMessage msg = new NetworkMessage();
            msg.type = NetworkMessage.Type.CHAT;
            msg.text = text;
            session.getClient().send(msg);
            chatInputField.setText("");
        }
    }

    public void appendChat(String message) {
        chatArea.append(" " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // ── Server → view dispatch ────────────────────────────────────────────

    private void onServerMessage(NetworkMessage msg) {
        SwingUtilities.invokeLater(() -> handleMessage(msg));
    }

    private void handleMessage(NetworkMessage msg) {
        switch (msg.type) {
            case CHAT -> {
                int senderIdx = msg.senderIndex != null ? msg.senderIndex : 0;
                String displayName;
                if (session.getMyPlayerIndex() != null && session.getMyPlayerIndex() == senderIdx) {
                    displayName = "Me";
                } else {
                    displayName = (msg.name != null) ? msg.name : nameOf(senderIdx);
                }
                appendChat(displayName + ": " + msg.text);
            }
            case PHASE -> onPhase(msg);
            case QUESTION -> onQuestion(msg);
            case RESULT -> onResult(msg);
            case SCORES -> onScores(msg);
            case GAME_OVER -> onGameOver(msg);
            case MAP_UPDATE -> onMapUpdate(msg); // NEW
            case ERROR -> {
                String errorMsg = msg.errorMessage != null ? msg.errorMessage : "Unknown error";
                if (errorMsg != null && errorMsg.toLowerCase().contains("full")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                errorMsg + "\nMaximum number of players reached. Contact the host.",
                                "Connection Denied",
                                JOptionPane.ERROR_MESSAGE);

                        session.disconnect();
                        nav.showMainMenu();
                    });
                } else {
                    feedback("Server Error: " + errorMsg, MindWarsTheme.WRONG_RED);
                }
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
                showQuestionPanel();
                choicesPanel.removeAll();
                textInput.setVisible(false);
                readyButton.setEnabled(myTurn);
                submitButton.setEnabled(false);
                if (myTurn) {
                    promptLabel
                            .setText("<html><b>It's your turn!</b><br>Press Ready when you're ready to answer.</html>");
                    turnLabel.setText("Your turn — press Ready");
                } else {
                    promptLabel.setText(
                            "<html>Waiting for <b>" + escape(nameOf(currentPlayer)) + "</b> to press Ready...</html>");
                    turnLabel.setText("Waiting for " + nameOf(currentPlayer) + "...");
                }

            }
            case "QUESTION", "INVASION_BATTLE" -> {
                showQuestionPanel();
                readyButton.setEnabled(false);
                submitButton.setEnabled(myTurn);
                turnLabel.setText(myTurn
                        ? "Your turn — answer now"
                        : nameOf(currentPlayer) + " is answering...");
            }
            case "TERRITORY_CLAIM" -> {
                // Switch to claim panel; MAP_UPDATE will fill in the grid
                showClaimPanel();
                readyButton.setEnabled(false);
                submitButton.setEnabled(false);
                turnLabel.setText("Territory Claim");
                claimInstructionLabel.setText("Waiting for map…");
            }
            case "INVASION_PASS" -> {
                showQuestionPanel();
                choicesPanel.removeAll();
                textInput.setVisible(false);
                readyButton.setEnabled(myTurn);
                submitButton.setEnabled(false);
                if (myTurn) {
                    promptLabel.setText(
                            "<html><b>Invasion phase — it's your turn to attack!</b><br>Press Ready when you're ready.</html>");
                    turnLabel.setText("Your turn — press Ready");
                } else {
                    promptLabel.setText("<html>Invasion phase — waiting for <b>" + escape(nameOf(currentPlayer))
                            + "</b>...</html>");
                    turnLabel.setText("Waiting for " + nameOf(currentPlayer) + "...");
                }
            }
            case "INVASION_SELECT" -> {
                showQuestionPanel();
                readyButton.setEnabled(false);
                submitButton.setEnabled(false);
                promptLabel.setText("<html>Phase: " + humanPhase(currentPhase) + "</html>");
                choicesPanel.removeAll();
                textInput.setVisible(false);
                turnLabel.setText(humanPhase(currentPhase));
            }
            case "GAME_OVER" -> {
                showQuestionPanel();
                readyButton.setEnabled(false);
                submitButton.setEnabled(false);
                turnLabel.setText("Game over");
            }
            default -> {
            }
        }

        revalidate();

        repaint();

    }

    /**
     * Receives the authoritative map state from the server and rebuilds the
     * claim grid. Enables cell buttons only for the player whose turn it is.
     */
    private void onMapUpdate(NetworkMessage msg) {
        if (msg.gridSnapshot == null || msg.mapSize == null)
            return;

        cachedGridSnapshot = msg.gridSnapshot;
        cachedMapSize = msg.mapSize;
        claimingPlayer = msg.claimingPlayer != null ? msg.claimingPlayer : -1;

        // Update instruction label
        String instruction = msg.claimInstruction != null ? msg.claimInstruction : "";
        claimInstructionLabel.setText(instruction);

        Integer me = session.getMyPlayerIndex();
        boolean isMyClaimTurn = (me != null && me == claimingPlayer);

        // Color the instruction to show whose turn it is
        if (claimingPlayer == -1) {
            claimInstructionLabel.setForeground(MindWarsTheme.PINK);
        } else if (isMyClaimTurn) {
            Color myColor = switch (claimingPlayer) {
                case 0 -> MindWarsTheme.PLAYER_X;
                case 1 -> MindWarsTheme.PLAYER_O;
                case 2 -> Color.CYAN;
                case 3 -> Color.YELLOW;
                default -> MindWarsTheme.WHITE;
            };
            claimInstructionLabel.setForeground(myColor);
        } else {
            claimInstructionLabel.setForeground(MindWarsTheme.GRAY_LIGHT);
        }

        rebuildClaimGrid(isMyClaimTurn);

        // Make sure the claim panel is visible
        showClaimPanel();
        revalidate();
        repaint();
    }

    private void rebuildClaimGrid(boolean enableEmpty) {
        claimGridPanel.removeAll();
        if (cachedGridSnapshot == null || cachedMapSize <= 0)
            return;

        int size = cachedMapSize;
        claimGridPanel.setLayout(new GridLayout(size, size, 6, 6));

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                char owner = cachedGridSnapshot.charAt(r * size + c);
                JButton btn = buildClaimButton(owner, r, c, enableEmpty);
                claimGridPanel.add(btn);
            }
        }
        claimGridPanel.revalidate();
        claimGridPanel.repaint();
    }

    private JButton buildClaimButton(char owner, int row, int col, boolean enableEmpty) {
        final boolean isEmpty = (owner == '.');

        // Determine fill color and label
        final Color fillColor;
        final String labelText;
        switch (owner) {
            case 'X' -> {
                fillColor = new Color(233, 30, 140);
                labelText = "X";
            }
            case 'O' -> {
                fillColor = new Color(200, 113, 55);
                labelText = "O";
            }
            case 'A' -> {
                fillColor = new Color(0, 200, 100);
                labelText = "A";
            }
            case 'B' -> {
                fillColor = new Color(255, 180, 0);
                labelText = "B";
            }
            default -> {
                fillColor = new Color(42, 42, 52);
                labelText = "";
            }
        }
        final Color hoverColor = new Color(60, 60, 72);
        final Color border = new Color(55, 55, 65);

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean hovered = Boolean.TRUE.equals(getClientProperty("hovered"));
                g2.setColor((isEmpty && hovered && isEnabled()) ? hoverColor : fillColor);
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (!labelText.isEmpty()) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(labelText)) / 2;
                    int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(labelText, tx, ty);
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
        btn.setBorder(BorderFactory.createLineBorder(border, 2));
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
            btn.addActionListener(e -> onClaimButtonClicked(fr, fc, btn));
        } else {
            btn.setEnabled(false);
        }

        return btn;
    }

    private void onClaimButtonClicked(int row, int col, JButton btn) {
        if (!session.isConnected())
            return;
        // Optimistic: disable button immediately to prevent double-click
        btn.setEnabled(false);
        session.getClient().sendClaimCell(row, col);
        // The server will reply with MAP_UPDATE which will rebuild the grid
    }

    // ── Card panel switching ──────────────────────────────────────────────

    private void showQuestionPanel() {
        JPanel centerCard = getCenterCard();
        if (centerCard != null) {
            ((CardLayout) centerCard.getLayout()).show(centerCard, "question");
        }
    }

    private void showClaimPanel() {
        JPanel centerCard = getCenterCard();
        if (centerCard != null) {
            ((CardLayout) centerCard.getLayout()).show(centerCard, "claim");
        }
    }

    /** Walk the component tree to find the CardLayout panel. */
    private JPanel getCenterCard() {
        // center is BorderLayout.CENTER of this; it holds the CardLayout panel
        Component center = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center instanceof JPanel cp) {
            for (Component child : cp.getComponents()) {
                if (child instanceof JPanel p && p.getLayout() instanceof CardLayout) {
                    return p;
                }
            }
        }
        return null;
    }

    // ── Q&A handlers ──────────────────────────────────────────

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
        if (msg.scores == null || msg.scores.isEmpty())
            return;
        if (msg.playerNames != null) {
            for (int i = 0; i < msg.playerNames.size() && i < playerNames.length; i++) {
                playerNames[i] = msg.playerNames.get(i);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msg.scores.size(); i++) {
            if (i > 0)
                sb.append("   ");
            sb.append(playerNames[i]).append(": ").append(msg.scores.get(i));
        }
        scoreLabel.setText(sb.toString());
    }

    private void onGameOver(NetworkMessage msg) {
        stopTimer();
        submitButton.setEnabled(false);
        String winner = (msg.winnerIndex == null) ? "It's a draw!" : nameOf(msg.winnerIndex) + " wins!";
        feedback("Game over — " + winner, MindWarsTheme.PINK);
        readyButton.setText("Back to Menu");
        readyButton.setEnabled(true);
        for (var al : readyButton.getActionListeners())
            readyButton.removeActionListener(al);
        readyButton.addActionListener(e -> {
            session.disconnect();
            nav.showMainMenu();
        });
    }

    // ── View → server ─────────────────────────────────────────────────────

    private void onReady() {
        if (!isMyTurn() || !session.isConnected())
            return;
        session.getClient().sendReady();
        readyButton.setEnabled(false);
    }

    private void onSubmit(ActionEvent e) {
        if (!isMyTurn() || !session.isConnected())
            return;
        String answer = readAnswer();
        if (answer == null)
            return;
        long elapsed = System.currentTimeMillis() - questionStartMs;
        session.getClient().sendAnswer(answer, elapsed);
        submitButton.setEnabled(false);
        stopTimer();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean isMyTurn() {
        Integer me = session.getMyPlayerIndex();
        return me != null && currentPlayer != null && me.equals(currentPlayer);
    }

    private String nameOf(Integer index) {
        if (index == null)
            return "opponent";
        if (index < 0 || index >= playerNames.length)
            return "Player " + (index + 1);
        return playerNames[index];
    }

    private String readAnswer() {
        if (!choiceButtons.isEmpty()) {
            for (int i = 0; i < choiceButtons.size(); i++) {
                if (choiceButtons.get(i).isSelected()) {
                    if ("MULTIPLE_CHOICE".equals(currentQuestionType))
                        return String.valueOf((char) ('A' + i));
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
            if (remaining <= 0)
                swingTimer.stop();
        });
        swingTimer.start();
    }

    private void stopTimer() {
        if (swingTimer != null)
            swingTimer.stop();
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
        tb.setBackground(Color.WHITE);
        tb.setForeground(Color.BLACK);
        tb.setOpaque(true);
        tb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MindWarsTheme.DARK_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tb.setAlignmentX(Component.LEFT_ALIGNMENT);
        tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        tb.addItemListener(e -> {
            if (tb.isSelected()) {
                tb.setBackground(MindWarsTheme.PINK_LIGHT);
                tb.setForeground(Color.BLACK);
                tb.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(MindWarsTheme.PINK, 2, true),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            } else {
                tb.setBackground(Color.WHITE);
                tb.setForeground(Color.BLACK);
                tb.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(MindWarsTheme.DARK_BORDER, 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
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
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}