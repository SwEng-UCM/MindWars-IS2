package network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Holds the active {@link GameServer} / {@link GameClient} for the current
 * process and dispatches inbound messages to any UI observers (#85).
 *
 * <p>
 * Views register through {@link #addMessageListener(Consumer)} and each
 * gets every server broadcast on the EDT-agnostic reader thread. They are
 * expected to marshal back to the EDT themselves when touching Swing.
 * </p>
 */
public class NetworkSession {

    private GameServer server;
    private GameClient client;
    private Integer myPlayerIndex;

    private final List<Consumer<List<String>>> lobbyListeners = new ArrayList<>();
    private final List<Consumer<NetworkMessage>> messageListeners = new ArrayList<>();

    public void attachServer(GameServer server) {
        this.server = server;
    }

    public void attachClient(GameClient client) {
        this.client = client;
        client.setListener(this::onMessage);
    }

    public GameServer getServer() {
        return server;
    }

    public GameClient getClient() {
        return client;
    }

    public boolean isHost() {
        return server != null;
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public Integer getMyPlayerIndex() {
        return myPlayerIndex;
    }

    public void addLobbyListener(Consumer<List<String>> listener) {
        lobbyListeners.add(listener);
    }

    public void removeLobbyListener(Consumer<List<String>> listener) {
        lobbyListeners.remove(listener);
    }

    public void addMessageListener(Consumer<NetworkMessage> listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Consumer<NetworkMessage> listener) {
        messageListeners.remove(listener);
    }

    public void disconnect() {
        if (client != null)
            client.close();
        if (server != null)
            server.stop();
        client = null;
        server = null;
        myPlayerIndex = null;
    }

    private void onMessage(NetworkMessage msg) {
        if (msg == null || msg.type == null)
            return;

        // Track our seat so downstream views know whose turn it is.
        if (msg.type == NetworkMessage.Type.WELCOME && msg.playerIndex != null) {
            myPlayerIndex = msg.playerIndex;
        }
        if (msg.type == NetworkMessage.Type.LOBBY) {
            List<String> names = msg.playerNames == null ? new ArrayList<>() : msg.playerNames;
            for (Consumer<List<String>> l : lobbyListeners)
                l.accept(names);
        }
        for (Consumer<NetworkMessage> l : messageListeners)
            l.accept(msg);
    }
}
