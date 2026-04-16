package view;

import controller.NavigationController;
import network.NetworkSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Lightweight lobby that shows connected players after the networking
 * handshake has succeeded (#85). Host mode shows the bound port and a
 * note that the game will start once the second player arrives; join
 * mode shows the peer list and a disconnect button.
 */
public class NetworkLobbyView extends JPanel {

    private final NavigationController nav;
    private final NetworkSession session;
    private final JLabel headerLabel;
    private final JTextArea playersArea;
    private final JLabel statusLabel;

    public NetworkLobbyView(NavigationController nav, NetworkSession session) {
        this.nav = nav;
        this.session = session;

        setLayout(new BorderLayout());

        JPanel bg = MindWarsTheme.createGradientPanel();
        bg.setLayout(new GridBagLayout());

        JPanel card = MindWarsTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 480));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        headerLabel = MindWarsTheme.centeredLabel("Lobby",
                MindWarsTheme.HEADING_FONT, MindWarsTheme.PINK);
        card.add(headerLabel);
        card.add(Box.createVerticalStrut(14));

        playersArea = new JTextArea(6, 20);
        playersArea.setEditable(false);
        playersArea.setFont(MindWarsTheme.BODY_FONT);
        playersArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MindWarsTheme.GRAY_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JScrollPane scroll = new JScrollPane(playersArea);
        scroll.setBorder(null);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(scroll);
        card.add(Box.createVerticalStrut(16));

        statusLabel = MindWarsTheme.centeredLabel("Waiting for connections...",
                MindWarsTheme.SMALL_FONT, Color.DARK_GRAY);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(16));

        JButton disconnect = MindWarsTheme.createPinkButton("Disconnect");
        disconnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        disconnect.addActionListener(e -> onDisconnect());
        card.add(disconnect);

        bg.add(card);
        add(bg, BorderLayout.CENTER);

        session.addLobbyListener(this::updatePlayers);
    }

    public void refresh() {
        if (session.isHost() && session.getServer() != null) {
            headerLabel.setText("Lobby (Host, port " + session.getServer().getBoundPort() + ")");
            statusLabel.setText("Waiting for an opponent to join...");
        } else {
            headerLabel.setText("Lobby");
            statusLabel.setText(session.isConnected()
                    ? "Connected — waiting for host to start."
                    : "Disconnected.");
        }
        playersArea.setText(session.isConnected() ? "(you)\n" : "");
    }

    private void updatePlayers(java.util.List<String> names) {
        if (names == null || names.isEmpty()) {
            playersArea.setText("(nobody yet)");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            sb.append((i + 1)).append(". ").append(names.get(i)).append('\n');
        }
        playersArea.setText(sb.toString());
        if (names.size() >= 2) {
            statusLabel.setText("Both players connected — the host will start the match.");
        }
    }

    private void onDisconnect() {
        session.disconnect();
        nav.showMainMenu();
    }
}
