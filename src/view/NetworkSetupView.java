package view;

import controller.NavigationController;
import model.GameModel;
import model.GameSettings;
import network.GameClient;
import network.GameServer;
import network.NetworkAddress;
import network.NetworkSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Multiplayer setup screen (#85). Lets the player choose between hosting
 * a local game server and joining an existing one, enter an IP/port, and
 * pick a display name. On success the active {@link GameServer} and/or
 * {@link GameClient} are exposed to the rest of the UI via
 * {@link NetworkSession}, and the view hands control to the lobby screen.
 */
public class NetworkSetupView extends JPanel {

    private final NavigationController nav;
    private final GameModel model;
    private final NetworkSession session;

    private final JToggleButton hostToggle;
    private final JToggleButton joinToggle;
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField nameField;
    private final JButton actionButton;
    private final JLabel statusLabel;

    private boolean hostMode = true;

    public NetworkSetupView(NavigationController nav, GameModel model, NetworkSession session) {
        this.nav = nav;
        this.model = model;
        this.session = session;

        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 560));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        card.add(MindWarsTheme.centeredLabel("Multiplayer",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK));
        card.add(Box.createVerticalStrut(18));

        // ── Host vs Join selector ──
        JPanel modeRow = new JPanel(new GridLayout(1, 2, 10, 0));
        modeRow.setOpaque(false);
        modeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        hostToggle = optionToggle("Host", true);
        joinToggle = optionToggle("Join", false);
        hostToggle.addActionListener(e -> setMode(true));
        joinToggle.addActionListener(e -> setMode(false));
        modeRow.add(hostToggle);
        modeRow.add(joinToggle);
        card.add(modeRow);
        card.add(Box.createVerticalStrut(18));

        // ── Name ──
        card.add(sectionLabel("Your name"));
        card.add(Box.createVerticalStrut(6));
        nameField = MindWarsTheme.createTextField("Player");
        nameField.setText("Player");
        card.add(nameField);
        card.add(Box.createVerticalStrut(14));

        // ── IP ──
        card.add(sectionLabel("Server IP"));
        card.add(Box.createVerticalStrut(6));
        hostField = MindWarsTheme.createTextField("127.0.0.1");
        hostField.setText("127.0.0.1");
        card.add(hostField);
        card.add(Box.createVerticalStrut(14));

        // ── Port ──
        card.add(sectionLabel("Port"));
        card.add(Box.createVerticalStrut(6));
        portField = MindWarsTheme.createTextField(String.valueOf(GameServer.DEFAULT_PORT));
        portField.setText(String.valueOf(GameServer.DEFAULT_PORT));
        card.add(portField);
        card.add(Box.createVerticalStrut(18));

        // ── Action button ──
        actionButton = MindWarsTheme.createGradientButton("Start Server");
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.addActionListener(e -> onAction());
        card.add(actionButton);
        card.add(Box.createVerticalStrut(10));

        statusLabel = MindWarsTheme.centeredLabel("", MindWarsTheme.SMALL_FONT, Color.DARK_GRAY);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(10));

        JButton back = MindWarsTheme.createPinkButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> nav.showMainMenu());
        card.add(back);

        bg.add(card);
        add(bg, BorderLayout.CENTER);

        setMode(true);
    }

    private void setMode(boolean host) {
        this.hostMode = host;
        restyleToggle(hostToggle, host);
        restyleToggle(joinToggle, !host);
        hostField.setEnabled(!host);
        actionButton.setText(host ? "Start Server" : "Connect");
        if (host) {
            String ip = NetworkAddress.getLanAddress();
            statusLabel.setText(ip == null
                    ? "Will bind on all interfaces."
                    : "This machine will be reachable at " + ip);
        } else {
            statusLabel.setText("");
        }
    }

    private void onAction() {
        int port = parsePort();
        if (port < 0) {
            statusLabel.setText("Invalid port (1–65535).");
            return;
        }
        String name = nameField.getText().isBlank() ? "Player" : nameField.getText().trim();

        if (hostMode) {
            startHosting(port, name);
        } else {
            String host = hostField.getText().isBlank() ? "127.0.0.1" : hostField.getText().trim();
            joinRemote(host, port, name);
        }
    }

    private void startHosting(int port, String name) {
        try {
            // A freshly hosted game re-uses the setup's defaults until a
            // proper pre-lobby screen exists (#88 territory). That keeps the
            // server wiring self-contained here for #85.
            GameSettings defaults = new GameSettings(
                    3, false, name, "Player 2", "", "", true, null, null, 2);
            GameModel hostedModel = new GameModel(model.getQuestionBank());
            GameServer server = new GameServer(port, defaults, hostedModel);
            server.start();
            session.attachServer(server);

            GameClient localClient = new GameClient();
            session.attachClient(localClient);
            localClient.connect("127.0.0.1", server.getBoundPort(), name);

            statusLabel.setText("Hosting on port " + server.getBoundPort()
                    + " — waiting for opponent.");
            nav.showMultiplayerLobby();
        } catch (IOException ex) {
            statusLabel.setText("Could not start server: " + ex.getMessage());
        }
    }

    private void joinRemote(String host, int port, String name) {
        GameClient client = new GameClient();
        session.attachClient(client);
        try {
            client.connect(host, port, name);
            statusLabel.setText("Connected to " + host + ":" + port);
            nav.showMultiplayerLobby();
        } catch (IOException ex) {
            statusLabel.setText("Connection failed: " + ex.getMessage());
        }
    }

    private int parsePort() {
        try {
            int p = Integer.parseInt(portField.getText().trim());
            if (p < 1 || p > 65535)
                return -1;
            return p;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ── Styling helpers (mirror GameSetupView for visual consistency) ──

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
}
