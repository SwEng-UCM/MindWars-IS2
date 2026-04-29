package view;

import controller.NavigationController;
import network.NetworkAddress;
import network.NetworkMessage;
import network.NetworkSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    private final JPanel playersPanel;
    private final JLabel statusLabel;
    private final JButton startButton;

    private List<String> lastNames = new ArrayList<>();

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

        JLabel playersTitle = new JLabel("Players in lobby");
        playersTitle.setFont(MindWarsTheme.BODY_BOLD);
        playersTitle.setForeground(Color.DARK_GRAY);
        playersTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(playersTitle);
        card.add(Box.createVerticalStrut(8));

        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setOpaque(false);
        playersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MindWarsTheme.GRAY_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JScrollPane scroll = new JScrollPane(playersPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(360, 180));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
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

        startButton = MindWarsTheme.createGradientButton("Start Match");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            NetworkMessage msg = new NetworkMessage(NetworkMessage.Type.START_GAME);
            session.getClient().send(msg);
        });
        card.add(Box.createVerticalStrut(10));
        card.add(startButton);

        bg.add(card);
        add(bg, BorderLayout.CENTER);

        // listeners

        // lobby
        session.addLobbyListener(names -> SwingUtilities.invokeLater(() -> {
            lastNames = names == null ? new ArrayList<>() : new ArrayList<>(names);
            renderPlayerList(lastNames, null);
            updateStatusAndStart(lastNames.size());
        }));

        // player left
        session.addPlayerLeftListener(msg -> SwingUtilities.invokeLater(() -> {
            String who = msg.disconnectedPlayerName != null
                    ? msg.disconnectedPlayerName
                    : ("Player " + ((msg.disconnectedPlayerIndex != null ? msg.disconnectedPlayerIndex : 0) + 1));
            showDisconnectToast(who);
            if (!lastNames.isEmpty()) {
                renderPlayerList(lastNames, who + " has disconnected");
                updateStatusAndStart(lastNames.size());
            }
        }));

        // ERROR: server full
        session.addMessageListener(msg -> {
            if (msg.type == NetworkMessage.Type.ERROR) {
                String err = msg.errorMessage != null ? msg.errorMessage : "Unknown error";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            this,
                            err + "\nMaximum number of players reached. Contact the host.",
                            "Connection Denied",
                            JOptionPane.ERROR_MESSAGE);
                    session.disconnect();
                    nav.showMainMenu();
                });
            }
        });

        session.addMessageListener(msg -> {
            if (msg.type == NetworkMessage.Type.PHASE && msg.phase != null
                    && !"SETUP".equals(msg.phase)) {
                SwingUtilities.invokeLater(nav::showMultiplayerGame);
            }
        });
    }

    public void refresh() {
        boolean isHost = session.isHost();
        if (isHost && session.getServer() != null) {
            int port = session.getServer().getBoundPort();
            String ip = NetworkAddress.getLanAddress();
            String ipText = ip == null ? "(unknown — check wifi)" : ip;
            headerLabel.setText("<html><center>Lobby — Host<br>"
                    + "<span style='color:#d53a89'>" + ipText + " : " + port + "</span>"
                    + "</center></html>");
            statusLabel.setText("Share the address above with the other player.");
        } else {
            headerLabel.setText("Lobby");
            statusLabel.setText(session.isConnected()
                    ? "Connected — waiting for host to start."
                    : "Disconnected.");
        }

        startButton.setVisible(isHost);
        startButton.setEnabled(false);

        List<String> initial = new ArrayList<>();
        if (session.isConnected()) {
            initial.add("(connecting...)");
        }
        renderPlayerList(initial, null);
    }

    private void renderPlayerList(List<String> names, String disconnectMsg) {
        playersPanel.removeAll();

        Integer myIdx = session.getMyPlayerIndex();

        if (names == null || names.isEmpty()) {
            JLabel nobody = new JLabel("(nobody yet)");
            nobody.setFont(MindWarsTheme.BODY_FONT);
            nobody.setForeground(Color.GRAY);
            nobody.setAlignmentX(Component.CENTER_ALIGNMENT);
            playersPanel.add(nobody);
        } else {
            for (int i = 0; i < names.size(); i++) {
                boolean isMe = myIdx != null && myIdx == i;
                String displayName = (i + 1) + ".  " + names.get(i);

                JLabel lbl = new JLabel(displayName);
                lbl.setFont(isMe
                        ? MindWarsTheme.BODY_BOLD
                        : MindWarsTheme.BODY_FONT);
                lbl.setForeground(isMe ? MindWarsTheme.PINK : Color.DARK_GRAY);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

                if (isMe) {
                    // Add a "(you)" tag next to the player's name
                    lbl.setText("<html>" + (i + 1) + ".  <b>" + names.get(i)
                            + "</b> <span style='color:#d53a89'>(you)</span></html>");
                }

                playersPanel.add(lbl);
                playersPanel.add(Box.createVerticalStrut(6));
            }
        }

        // display disconnect message
        if (disconnectMsg != null) {
            playersPanel.add(Box.createVerticalStrut(4));
            JLabel dcLabel = new JLabel("<html><i>" + disconnectMsg + "</i></html>");
            dcLabel.setFont(MindWarsTheme.SMALL_FONT);
            dcLabel.setForeground(new Color(180, 60, 60));
            dcLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            playersPanel.add(dcLabel);

            new Timer(4000, e -> {
                renderPlayerList(lastNames, null);
                playersPanel.revalidate();
                playersPanel.repaint();
            }) {
                {
                    setRepeats(false);
                    start();
                }
            };
        }

        playersPanel.revalidate();
        playersPanel.repaint();
    }

    private void updateStatusAndStart(int playerCount) {
        boolean ready = playerCount >= 2;
        if (ready) {
            statusLabel.setText("Players ready — you can start the match!");
        } else {
            statusLabel.setText(session.isHost()
                    ? "Waiting for other players to join..."
                    : "Connected — waiting for host to start.");
        }
        if (session.isHost()) {
            startButton.setEnabled(ready);
        }
    }

    private void showDisconnectToast(String playerName) {
        JWindow toast = new JWindow(SwingUtilities.getWindowAncestor(this));
        JLabel lbl = new JLabel("  " + playerName + " has left the lobby  ");
        lbl.setFont(MindWarsTheme.BODY_BOLD);
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(new Color(190, 50, 50));
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(10, 16, 10, 16));
        toast.add(lbl);
        toast.pack();
        toast.setLocationRelativeTo(this);
        toast.setVisible(true);
        new Timer(3000, e -> toast.dispose()) {
            {
                setRepeats(false);
                start();
            }
        };
    }

    private void onDisconnect() {
        session.disconnect();
        nav.showMainMenu();
    }
}
